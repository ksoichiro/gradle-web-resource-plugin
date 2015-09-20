package com.github.ksoichiro.web.resource

import com.moowork.gradle.node.task.NodeTask
import groovy.json.JsonOutput
import groovy.text.SimpleTemplateEngine
import org.gradle.api.tasks.TaskAction

class WebResourceCompileBaseTask extends NodeTask {
    WebResourceExtension extension
    String gulpCommand = 'default'
    boolean gulpEnabled = true

    WebResourceCompileBaseTask() {
        dependsOn([WebResourceInstallBowerDependenciesTask.NAME])
        project.afterEvaluate {
            extension = project.extensions.webResource
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
            srcLess      : getSrcLess(),
            destLess     : getDestLess(),
            lessEnabled  : extension.less.enabled,
            filterLess   : extension.less.filter ? JsonOutput.toJson(extension.less.filter).toString() : "['**/*', '!**/_*.less']",
            minifyLess   : extension.less.minify,
            srcCoffee    : getSrcCoffee(),
            destCoffee   : getDestCoffee(),
            coffeeEnabled: extension.coffeeScript.enabled,
            filterCoffee : extension.coffeeScript.filter ? JsonOutput.toJson(extension.coffeeScript.filter).toString() : "['**/*', '!**/_*.coffee']",
            minifyCoffee : extension.coffeeScript.minify,
            bowerEnabled : extension.bower && !extension.bower.isEmpty(),
            destLib      : resolveDestPath(extension.lib?.dest),
            workDir      : "../../${extension.workDir.absolutePath.replace(project.projectDir.absolutePath, "").replaceAll("\\\\", "/").replaceAll("^/", "")}"
        ]
        getGulpfile().text =
            new SimpleTemplateEngine()
                .createTemplate(getClass().getResourceAsStream('/gulpfile.js').text)
                .make(bindings)
        super.exec()
    }

    String resolveSrcPath(def path) {
        String src = "../../"
        if (path) {
            src += extension.base?.src ? "${extension.base?.src}/" : ""
            src += path
        }
        src
    }

    String resolveDestPath(def path) {
        String dest = ""
        if (path) {
            dest = extension.base?.dest ? "${extension.base?.dest}/" : ""
            dest += path
            dest = "../../${dest}"
        }
        dest
    }

    String getSrcCoffee() {
        resolveSrcPath(extension.coffeeScript?.src)
    }

    String getSrcLess() {
        resolveSrcPath(extension.less?.src)
    }

    String getDestCoffee() {
        resolveDestPath(extension.coffeeScript?.dest)
    }

    String getDestLess() {
        resolveDestPath(extension.less?.dest)
    }

    String getDestLib() {
        resolveDestPath(extension.lib?.dest)
    }

    List retrieveValidPaths(String... paths) {
        List result = []
        paths.findAll { project.file("${extension.workDir}/${it}") }.each {
            result += "${extension.workDir}/${it}"
        }
        result
    }

    File getGulpfile() {
        new File(extension.workDir, "gulpfile.js")
    }
}
