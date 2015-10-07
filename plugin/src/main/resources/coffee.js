var fs = require('fs');
var path = require('path');
var coffee = require('coffee-script');
var UglifyJS = require("uglify-js");

var coffeeSrcPath = process.argv[2];
var coffeeSrcName = process.argv[3];
var coffeeDestDir = process.argv[4];
console.log('coffeeSrcPath: ' + coffeeSrcPath);
console.log('coffeeSrcName: ' + coffeeSrcName);
console.log('coffeeDestDir: ' + coffeeDestDir);

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
  console.log(js);
//  var minified = UglifyJS.minify(js, {fromString: true, spidermonkey: true});
//  console.log(minified);

  if (!fs.existsSync(path.dirname(outputPath))) {
    mkdirsSync(path.dirname(outputPath));
  }

  fs.writeFile(outputPath, js, function(err) {
//  fs.writeFile(outputPath, minified.code, function(err) {
    if (err) {
      console.log(err);
    }
    console.log('CoffeeScript: processed ' + filepath);
  });
}
