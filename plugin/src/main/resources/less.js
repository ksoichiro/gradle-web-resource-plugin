var fs = require('fs');
var path = require('path');
var less = require('less');
var async = require('async');
var common = require('./common.js');

var lessSrcPath = process.argv[2];
var lessSrcName = process.argv[3];
var lessDestDir = process.argv[4];
var minify = process.argv[5] === 'true';
var logLevel = parseInt(process.argv[6]);

// Calling exit from async function does not work,
// so hook exiting event and exit again.
var exitCode = 0;
process.on('exit', function() {
  process.reallyExit(exitCode);
});

common.mkdirsIfNotExistSync(lessDestDir);
lessConvert(lessSrcPath, lessSrcName, [path.dirname(lessSrcPath)], path.join(lessDestDir, lessSrcName.replace(/\.less/, '.css')));

function lessConvert(filepath, filename, searchPaths, outputPath) {
  var lessString = fs.readFileSync(filepath, 'utf8');
  async.series([
    function(callback) {
      less.render(lessString,
        {
          paths: searchPaths,
          filename: filename,
          compress: minify
        },
        function (e, output) {
          if (e) {
            common.logE(logLevel, 'LESS: compilation failed: ' + e);
            callback(e, "render");
            return;
          }
          common.mkdirsIfNotExistSync(path.dirname(outputPath));
          fs.writeFile(outputPath, output.css, function(err) {
            if (err) {
              common.logE(logLevel, 'LESS: saving file failed: ' + err);
              callback(err, "render");
            } else {
              callback(null, "render");
            }
          });
        });
    }
  ], function(err, results) {
    if (err) {
      exitCode = 1;
    } else {
      common.logI(logLevel, 'LESS: processed ' + filepath);
    }
  });
}
