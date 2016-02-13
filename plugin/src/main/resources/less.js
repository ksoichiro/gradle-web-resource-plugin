var fs = require('fs');
var path = require('path');
var less = require('less');
var Q = require('q');
var common = require('./common');
var Logger = require('./logger');

var lessSrcSet = JSON.parse(fs.readFileSync(process.argv[2], 'utf-8'));
var minify = process.argv[3] === 'true';
var parallelize = process.argv[4] === 'true';
var logLevel = parseInt(process.argv[5]);

var log = new Logger(logLevel, 'LESS');

common.handleExit();

if (parallelize) {
  lessSrcSet.forEach(function(item) {
    lessConvertItem(item, null);
  });
} else {
  common.installSequentially(lessSrcSet, lessConvertItem);
}

function lessConvertItem(item, cb) {
  log.d('Started: ' + item.name);
  lessConvert(item.path, item.name, [path.dirname(item.path)], path.join(item.destDir, item.name.replace(/\.less/, '.css')),
    function() {
      log.d('Finished: ' + item.name);
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
      function (err, output) {
        if (err) {
          log.e('Compilation failed: ' + err);
          deferred.reject();
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
        log.e('Saving file failed: ' + err);
        deferred.reject();
      } else {
        log.i('Compiled: ' + filepath);
        deferred.resolve();
      }
    });
    return deferred.promise;
  })
  .catch(function(err) {
    if (err) {
      log.e('Compilation failed: ' + filename + ': ' + err);
      if (err.stack) {
        log.w(err.stack);
      }
    }
    common.setExitCode(1);
  })
  .done(function() {
    if (cb) {
      cb();
    }
  });
}
