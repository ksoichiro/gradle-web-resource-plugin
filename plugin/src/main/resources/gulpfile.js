(function() {
  var fs = require('fs');
  var gulp = require('gulp');
  var gulpFilter = ${lessEnabled} && ${coffeeEnabled} ? require('gulp-filter') : 0;

  var mkdirsSync = function(dir) {
    var fs = require('fs');
    var dirs = dir.split('/');
    var s = '';
    for (var i = 0; i < dirs.length; i++) {
      s += (s.length == 0 ? '' : '/') + dirs[i];
      if (!fs.existsSync(s)) {
        fs.mkdirSync(s);
      }
    }
  }

  if (${lessEnabled}) {
    var less = require('gulp-less');
    var cssmin = require('gulp-minify-css');
    gulp.task('less', function() {
      if (fs.existsSync('${srcLess}')) {
        if (!fs.existsSync('${destLess}')) {
          mkdirsSync('${destLess}');
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
          mkdirsSync('${destCoffee}');
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

  gulp.task('watch', function() {
    if (fs.existsSync('${srcLess}')) {
      gulp.watch('${srcLess}/**/*.less', ['less']);
    }
    if (fs.existsSync('${srcCoffee}')) {
      gulp.watch('${srcCoffee}/**/*.coffee', ['coffee']);
    }
  });
})();
