package com.github.ksoichiro.web.resource

import com.moowork.gradle.node.task.NodeTask
import org.gradle.api.tasks.TaskAction

class WebResourceCompileTask extends NodeTask {
    static final String NAME = "webResourceCompile"
    String srcCoffee
    String destCoffee
    String srcLess
    String destLess
    String destLib

    WebResourceCompileTask() {
        dependsOn([WebResourceInstallBowerDependenciesTask.NAME])
        this.project.afterEvaluate {
            WebResourceExtension extension = this.project.extensions.webResource
            this.setDirs(extension)
            getInputs().files(srcCoffee, srcLess)
            setWorkingDir(extension.workDir)
        }
    }

    @TaskAction
    void exec() {
        def extension = this.project.webResource as WebResourceExtension
        def gulp = this.project.file(new File(extension.workDir, "node_modules/gulp/bin/gulp.js"))
        setScript(gulp)
        setArgs(['default'])
        new File(extension.workDir, "gulpfile.js").text = """\
var uglify = require('gulp-uglify');
var mainBowerFiles = require('main-bower-files');
var coffee = require('gulp-coffee');
var less = require('gulp-less');
var cssmin = require('gulp-minify-css');
var gulp = require('gulp');

gulp.task('less', function() {
    return gulp.src('${srcLess}/*.less')
        .pipe(less())
        .pipe(cssmin({root: '${srcLess}'}))
        .pipe(gulp.dest('${destLess}'));
});

gulp.task('coffee', function() {
    gulp.src('${srcCoffee}/*.coffee')
        .pipe(coffee())
        .pipe(uglify())
        .pipe(gulp.dest('${destCoffee}'));
});

gulp.task('bower-files', function() {
    gulp.src(mainBowerFiles(), { base: '${extension.workDir}/bower_components' })
        .pipe(gulp.dest('${destLib}'));
});

gulp.task('default', ['less', 'coffee', 'bower-files'], function() {
});
"""
        super.exec()
    }

    def setDirs(WebResourceExtension extension) {
        srcCoffee = "../../"
        destCoffee = ""
        if (extension.coffeeScript) {
            srcCoffee += extension.base?.src ? "${extension.base?.src}/" : ""
            srcCoffee += extension.coffeeScript.src
            destCoffee = extension.base?.dest ? "${extension.base?.dest}/" : ""
            destCoffee += extension.coffeeScript.dest
            project.file(destCoffee).mkdirs()
            destCoffee = "../../" + destCoffee
        }
        srcLess = "../../"
        destLess = ""
        if (extension.less) {
            srcLess += extension.base?.src ? "${extension.base?.src}/" : ""
            srcLess += extension.less.src
            destLess = extension.base?.dest ? "${extension.base?.dest}/" : ""
            destLess += extension.less.dest
            project.file(destLess).mkdirs()
            destLess = "../../" + destLess
        }
        destLib = ""
        if (extension.lib) {
            destLib = extension.base?.dest ? "${extension.base?.dest}/" : ""
            destLib += extension.lib.dest
            project.file(destLib).mkdirs()
            destLib = "../../" + destLib
        }
    }
}
