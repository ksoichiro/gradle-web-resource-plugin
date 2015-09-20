(function() {
    var fs = require('fs-extra');
    var uglify = ${coffeeEnabled} ? require('gulp-uglify') : 0;
    if (${bowerEnabled}) var mainBowerFiles = require('main-bower-files');
    var coffee = ${coffeeEnabled} ? require('gulp-coffee') : 0;
    var less = ${lessEnabled} ? require('gulp-less') : 0;
    var cssmin = ${lessEnabled} ? require('gulp-minify-css') : 0;
    var gulpFilter = require('gulp-filter');
    var gulp = require('gulp');
    var include = ${coffeeEnabled} ? require("gulp-include") : 0;

    if (${lessEnabled}) {
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
