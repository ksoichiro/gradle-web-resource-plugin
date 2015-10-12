var fs = require('fs');
var path = require('path');
var less = require('less');
var common = require('./common.js');

var lessSrcPath = process.argv[2];
var lessSrcName = process.argv[3];
var lessDestDir = process.argv[4];
var minify = process.argv[5] === 'true';
var logLevel = parseInt(process.argv[6]);

common.mkdirsIfNotExistSync(lessDestDir);
lessConvert(lessSrcPath, lessSrcName, [path.dirname(lessSrcPath)], path.join(lessDestDir, lessSrcName.replace(/\.less/, '.css')));

function lessConvert(filepath, filename, searchPaths, outputPath) {
  var lessString = fs.readFileSync(filepath, 'utf8');
  less.render(lessString,
    {
      paths: searchPaths,
      filename: filename,
      compress: minify
    },
    function (e, output) {
      common.mkdirsIfNotExistSync(path.dirname(outputPath));
      fs.writeFile(outputPath, output.css, function(err) {
        if (err) {
          common.logE(logLevel, err);
          process.exit(1);
        }
        common.logI(logLevel, 'LESS: processed ' + filepath);
      });
    });
}
