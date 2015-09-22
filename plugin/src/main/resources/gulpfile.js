(function() {
  var fs = require('fs-extra');
  var gulp = require('gulp');
  var gulpFilter = ${lessEnabled} && ${coffeeEnabled} ? require('gulp-filter') : 0;

  if (${lessEnabled}) {
    var less = require('gulp-less');
    var cssmin = require('gulp-minify-css');
    gulp.task('less', function() {
      if (fs.existsSync('${srcLess}')) {
        if (!fs.existsSync('${destLess}')) {
          fs.mkdirsSync('${destLess}');
        }
        var g = gulp.src('${srcLess}/**/*.less')
          .pipe(gulpFilter(${filterLess}))
          .pipe(less());
        if (${minifyLess}) {
          g = g.pipe(cssmin({root: '${srcLess}'}));
        }
        g.pipe(gulp.dest('${destLess}'));
      }
    });
  }

  if (${coffeeEnabled}) {
    var coffee = require('gulp-coffee');
    var uglify = require('gulp-uglify');
    var include = require("gulp-include");
    gulp.task('coffee', function() {
      if (fs.existsSync('${srcCoffee}')) {
        if (!fs.existsSync('${destCoffee}')) {
          fs.mkdirsSync('${destCoffee}');
        }
        var g = gulp.src('${srcCoffee}/**/*.coffee')
          .pipe(include())
          .pipe(gulpFilter(${filterCoffee}))
          .pipe(coffee())
        if (${minifyCoffee}) {
          g = g.pipe(uglify())
        }
        g.pipe(gulp.dest('${destCoffee}'));
      }
    });
  }

  if (${bowerEnabled}) {
    var mainBowerFiles = require('main-bower-files');
    gulp.task('bower-files', function() {
      if (fs.existsSync('bower.json')) {
        gulp.src(mainBowerFiles(), { base: '${workDir}/bower_components' })
          .pipe(gulp.dest('${destLib}'));
      }
    });
  }

  gulp.task('watch', function() {
    if (fs.existsSync('${srcLess}')) {
      gulp.watch('${srcLess}/**/*.less', ['less']);
    }
    if (fs.existsSync('${srcCoffee}')) {
      gulp.watch('${srcCoffee}/**/*.coffee', ['coffee']);
    }
  });
})();
