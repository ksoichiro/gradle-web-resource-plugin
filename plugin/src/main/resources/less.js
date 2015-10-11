var fs = require('fs');
var path = require('path');
var less = require('less');

var lessSrcPath = process.argv[2];
var lessSrcName = process.argv[3];
var lessDestDir = process.argv[4];
var minify = process.argv[5] === 'true';

var mkdirsSync = function(dir) {
  var dirs = dir.split('/');
  var s = '';
  for (var i = 0; i < dirs.length; i++) {
    s += (s.length == 0 ? '' : '/') + dirs[i];
    if (!fs.existsSync(s)) {
      try {
        fs.mkdirSync(s);
      } catch (err) {
        if (err.code == 'EEXIST') {
          // Other thread might create this directory at the same time, so ignore it
        }
      }
    }
  }
}

if (!fs.existsSync(lessDestDir)) {
  mkdirsSync(lessDestDir);
}
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
      if (!fs.existsSync(path.dirname(outputPath))) {
        mkdirsSync(path.dirname(outputPath));
      }
      fs.writeFile(outputPath, output.css, function(err) {
        if (err) {
          console.log(err);
        }
        console.log('LESS: processed ' + filepath);
      });
    });
}
