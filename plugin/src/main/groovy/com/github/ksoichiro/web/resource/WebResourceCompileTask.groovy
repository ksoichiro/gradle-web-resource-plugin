package com.github.ksoichiro.web.resource

import com.moowork.gradle.node.task.NodeTask
import groovy.json.JsonOutput
import groovy.text.SimpleTemplateEngine
import org.gradle.api.tasks.TaskAction

class WebResourceCompileTask extends NodeTask {
    static final String NAME = "webResourceCompile"
    WebResourceExtension extension

    WebResourceCompileTask() {
        dependsOn([WebResourceInstallBowerDependenciesTask.NAME])
        this.project.afterEvaluate {
            extension = this.project.extensions.webResource
            getInputs().files(getSrcCoffee(), getSrcLess())
            setWorkingDir(extension.workDir)
        }
    }

    @TaskAction
    void exec() {
        def gulp = this.project.file(new File(extension.workDir, "node_modules/gulp/bin/gulp.js"))
        setScript(gulp)
        setArgs(['default'])
        def bindings = [
                srcLess   : getSrcLess(),
                destLess  : getDestLess(),
                filterLess: extension.less.filter ? JsonOutput.toJson(extension.less.filter).toString() : "['*', '!**/_*.less']",
                srcCoffee : getSrcCoffee(),
                destCoffee: getDestCoffee(),
                destLib   : resolveDestPath(extension.lib?.dest),
                workDir   : "../../${extension.workDir.absolutePath.replace(this.project.projectDir.absolutePath, "").replaceAll("^/", "")}"
        ]
        new File(extension.workDir, "gulpfile.js").text =
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
            project.file(dest).mkdirs()
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
}
