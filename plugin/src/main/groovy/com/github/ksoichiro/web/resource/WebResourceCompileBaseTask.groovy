package com.github.ksoichiro.web.resource

import com.moowork.gradle.node.task.NodeTask
import groovy.json.JsonOutput
import groovy.text.SimpleTemplateEngine
import org.gradle.api.tasks.TaskAction

class WebResourceCompileBaseTask extends NodeTask {
    WebResourceExtension extension
    PathResolver pathResolver
    String gulpCommand = 'default'
    boolean gulpEnabled = true

    WebResourceCompileBaseTask() {
        dependsOn([WebResourceInstallBowerDependenciesTask.NAME])
        project.afterEvaluate {
            extension = project.extensions.webResource
            pathResolver = new PathResolver(project, extension)
            setWorkingDir(extension.workDir)
        }
    }

    @TaskAction
    void exec() {
        if (!gulpEnabled) {
            return
        }
        def gulp = project.file(new File(extension.workDir, "node_modules/gulp/bin/gulp.js"))
        setScript(gulp)
        setArgs([gulpCommand])
        def bindings = [
            srcLess      : pathResolver.getSrcLess(),
            destLess     : pathResolver.getDestLess(),
            lessEnabled  : extension.less.enabled,
            filterLess   : extension.less.filter ? JsonOutput.toJson(extension.less.filter).toString() : "['**/*', '!**/_*.less']",
            minifyLess   : extension.less.minify,
            srcCoffee    : pathResolver.getSrcCoffee(),
            destCoffee   : pathResolver.getDestCoffee(),
            coffeeEnabled: extension.coffeeScript.enabled,
            filterCoffee : extension.coffeeScript.filter ? JsonOutput.toJson(extension.coffeeScript.filter).toString() : "['**/*', '!**/_*.coffee']",
            minifyCoffee : extension.coffeeScript.minify,
            workDir      : "../../${extension.workDir.absolutePath.replace(project.projectDir.absolutePath, "").replaceAll("\\\\", "/").replaceAll("^/", "")}"
        ]
        getGulpfile().text =
            new SimpleTemplateEngine()
                .createTemplate(getClass().getResourceAsStream('/gulpfile.js').text)
                .make(bindings)
        super.exec()
    }

    File getGulpfile() {
        new File(extension.workDir, "gulpfile.js")
    }
}
