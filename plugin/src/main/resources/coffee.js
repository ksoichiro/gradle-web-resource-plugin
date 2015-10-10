var fs = require('fs');
var path = require('path');
var coffee = require('coffee-script');
var UglifyJS = require('uglify-js');

var coffeeSrcPath = process.argv[2];
var coffeeSrcName = process.argv[3];
var coffeeDestDir = process.argv[4];
var minify = process.argv[5] === 'true';

var mkdirsSync = function(dir) {
  var dirs = dir.split('/');
  var s = '';
  for (var i = 0; i < dirs.length; i++) {
    s += (s.length == 0 ? '' : '/') + dirs[i];
    if (!fs.existsSync(s)) {
      fs.mkdirSync(s);
    }
  }
}

if (!fs.existsSync(coffeeDestDir)) {
  mkdirsSync(coffeeDestDir);
}
coffeeConvert(coffeeSrcPath, coffeeSrcName, [path.dirname(coffeeSrcPath)], path.join(coffeeDestDir, coffeeSrcName.replace(/\.coffee/, '.js')));

function coffeeConvert(filepath, filename, searchPaths, outputPath) {
  var coffeeString = fs.readFileSync(filepath, 'utf8');

  var js = coffee.compile(coffeeString, {});
  if (minify) {
    var minified = UglifyJS.minify(js, {fromString: true, compress: {evaluate: false}});
    js = minified.code;
  }

  if (!fs.existsSync(path.dirname(outputPath))) {
    mkdirsSync(path.dirname(outputPath));
  }

  fs.writeFile(outputPath, js, function(err) {
    if (err) {
      console.log(err);
    }
    console.log('CoffeeScript: processed ' + filepath);
  });
}
