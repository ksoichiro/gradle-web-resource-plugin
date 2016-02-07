var fs = require('fs');
var path = require('path');
var Q = require('q');
var coffee = require('coffee-script');
var common = require('./common.js');
var UglifyJS = require('uglify-js');
var glob = require('glob');

var coffeeSrcSet = JSON.parse(fs.readFileSync(process.argv[2], 'utf-8'));
var minify = process.argv[3] === 'true';
var parallelize = process.argv[4] === 'true';
var logLevel = parseInt(process.argv[5]);

var extensions = null;
var includedFiles = [];
var TAG = 'CoffeeScript';

// Calling exit from async function does not work,
// so hook exiting event and exit again.
var exitCode = 0;
process.on('exit', function() {
  process.reallyExit(exitCode);
});

if (parallelize) {
  coffeeSrcSet.forEach(function(item) {
    coffeeConvertItem(item, null);
  });
} else {
  coffeeConvertAt(0);
}

function coffeeConvertAt(idx) {
  if (coffeeSrcSet.length <= idx) {
    return;
  }
  var item = coffeeSrcSet[idx];
  coffeeConvertItem(item, function() {
    if (exitCode === 0) {
      coffeeConvertAt(idx + 1);
    }
  });
}

function coffeeConvertItem(item, cb) {
  common.logI(logLevel, TAG, 'Started: ' + item.name);
  coffeeConvert(item.path, item.name, [path.dirname(item.path)], path.join(item.destDir, item.name.replace(/\.coffee/, '.js')),
    function() {
      common.logI(logLevel, TAG, 'Finished: ' + item.name);
      if (cb) {
        cb();
      }
    });
}

function coffeeConvert(filepath, filename, searchPaths, outputPath, cb) {
  (function() {
    var deferred = Q.defer();
    var coffeeString = fs.readFileSync(filepath, 'utf8');
    try {
      coffeeString = processInclude(coffeeString, filepath)
      var js = coffee.compile(coffeeString, {});
      if (minify) {
        var minified = UglifyJS.minify(js, {fromString: true, compress: {evaluate: false}});
        js = minified.code;
      }
      deferred.resolve(js);
    } catch (e) {
      common.logE(logLevel, TAG, 'Compilation failed: ' + e);
      deferred.reject(e);
    }
    return deferred.promise;
  })()
  .then(function(js) {
    var deferred = Q.defer();
    common.mkdirsIfNotExistSync(path.dirname(outputPath));
    fs.writeFile(outputPath, js, function(err) {
      if (err) {
        common.logE(logLevel, TAG, 'Saving file failed: ' + err);
        deferred.reject(err);
      } else {
        common.logI(logLevel, TAG, 'Compiled: ' + filepath);
        deferred.resolve();
      }
    });
    return deferred.promise;
  })
  .catch(function(error) {
    exitCode = 1;
  })
  .done(function() {
    if (cb) {
      cb();
    }
  });
}

// Script below is a partial copy from wiledal/gulp-include to use include feature.
//
// Licence
//
// (MIT License)
//
// Copyright (c) 2014 Hugo Wiledal
//
// Permission is hereby granted, free of charge, to any person obtaining a copy
// of this software and associated documentation files (the "Software"), to deal
// in the Software without restriction, including without limitation the rights
// to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
// copies of the Software, and to permit persons to whom the Software is
// furnished to do so, subject to the following conditions:
//
// The above copyright notice and this permission notice shall be included in all
// copies or substantial portions of the Software.
//
// THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
// IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
// FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
// AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
// LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
// OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
// SOFTWARE.

function processInclude(content, filePath) {
  var matches = content.match(/^(\s+)?(\/\/|\/\*|\#)(\s+)?=(\s+)?(include|require)(.+$)/mg);
  var relativeBasePath = path.dirname(filePath);

  if (!matches) return content;

  for (var i = 0; i < matches.length; i++) {
    var leadingWhitespaceMatch = matches[i].match(/^(\s+)/);
    var leadingWhitespace = null;
    if (leadingWhitespaceMatch) {
      leadingWhitespace = leadingWhitespaceMatch[0];
      if (leadingWhitespaceMatch[0].indexOf("\n") > -1) leadingWhitespace = leadingWhitespaceMatch[0].split("\n")[1];
      leadingWhitespace = leadingWhitespace.replace("\n", "");
    }

    // Remove beginnings, endings and trim.
    var includeCommand = matches[i]
      .replace(/(\s+)/gi, " ")
      .replace(/(\/\/|\/\*)(\s+)?=(\s+)?/g, "")
      .replace(/(\*\/)$/gi, "")
      .replace(/['"]/g, "")
      .trim();
    var split = includeCommand.split(" ");

    // Split the directive and the path
    var includeType = split[0];
    var includePath = relativeBasePath + "/" + split[1];

    // Use glob for file searching
    var fileMatches = glob.sync(includePath, {mark: true});
    var replaceContent = null;
    for (var y = 0; y < fileMatches.length; y++) {
      var globbedFilePath = fileMatches[y];

      // If directive is of type "require" and file already included, skip to next.
      if (includeType == "require" && includedFiles.indexOf(globbedFilePath) > -1) continue;

      // If not in extensions, skip this file
      if (!inExtensions(globbedFilePath)) continue;

      // Get file contents and apply recursive include on result
      var fileContents = fs.readFileSync(globbedFilePath);
      if (!replaceContent) replaceContent = "";
      if (leadingWhitespace) fileContents = addLeadingWhitespace(leadingWhitespace, fileContents.toString());
      replaceContent += processInclude(fileContents.toString(), globbedFilePath);

      if (includedFiles.indexOf(globbedFilePath) == -1) includedFiles.push(globbedFilePath);

      // If the last file did not have a line break, and it is not the last file in the matched glob,
      // add a line break to the end
      if (!replaceContent.trim().match(/\n$/) && y != fileMatches.length-1) replaceContent += "\n";
    }

    // REPLACE
    if (replaceContent) {
      content = content.replace(matches[i], function(){return replaceContent});
    }
  }

  return content;
}

function addLeadingWhitespace(whitespace, string) {
  return string.split("\n").map(function(line) {
    return whitespace + line;
  }).join("\n");
}

function inExtensions(filePath) {
  if (!extensions) return true;
  for (var i = 0; i < extensions.length; i++) {
    var re = extensions[i] + "$";
    if (filePath.match(re)) return true;
  }
  return false;
}
