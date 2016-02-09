var fs = require('fs');
var path = require('path');
var less = require('less');
var Q = require('q');
var common = require('./common.js');

var lessSrcSet = JSON.parse(fs.readFileSync(process.argv[2], 'utf-8'));
var minify = process.argv[3] === 'true';
var parallelize = process.argv[4] === 'true';
var logLevel = parseInt(process.argv[5]);

var TAG = 'LESS';

var exitCode = 0;
common.handleExit(logLevel, TAG, function() { return exitCode; }, null);

if (parallelize) {
  lessSrcSet.forEach(function(item) {
    lessConvertItem(item, null);
  });
} else {
  common.installSequentially(lessSrcSet, lessConvertItem, function() { return exitCode === 0; });
}

function lessConvertItem(item, cb) {
  common.logI(logLevel, TAG, 'Started: ' + item.name);
  lessConvert(item.path, item.name, [path.dirname(item.path)], path.join(item.destDir, item.name.replace(/\.less/, '.css')),
    function() {
      common.logI(logLevel, TAG, 'Finished: ' + item.name);
      if (cb) {
        cb();
      }
    });
}

function lessConvert(filepath, filename, searchPaths, outputPath, cb) {
  (function() {
    var deferred = Q.defer();
    var lessString = fs.readFileSync(filepath, 'utf8');
    less.render(lessString,
      {
        paths: searchPaths,
        filename: filename,
        compress: minify
      },
      function (e, output) {
        if (e) {
          common.logE(logLevel, TAG, 'Compilation failed: ' + e);
          deferred.reject(e);
        } else {
          deferred.resolve(output);
        }
      });
    return deferred.promise;
  })()
  .then(function(output) {
    var deferred = Q.defer();
    common.mkdirsIfNotExistSync(path.dirname(outputPath));
    fs.writeFile(outputPath, output.css, function(err) {
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
