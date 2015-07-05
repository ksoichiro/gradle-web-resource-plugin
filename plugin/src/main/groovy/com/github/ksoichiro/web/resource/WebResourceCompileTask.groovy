package com.github.ksoichiro.web.resource

import com.moowork.gradle.node.task.NodeTask
import groovy.json.JsonOutput
import groovy.text.SimpleTemplateEngine
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
        def bindings = [
                srcLess   : srcLess,
                destLess  : destLess,
                filterLess: extension.less.filter ? JsonOutput.toJson(extension.less.filter).toString() : "['*', '!**/_*.less']",
                srcCoffee : srcCoffee,
                destCoffee: destCoffee,
                destLib   : destLib,
                workDir   : "../../${extension.workDir.absolutePath.replace(this.project.projectDir.absolutePath, "").replaceAll("^/", "")}"
        ]
        new File(extension.workDir, "gulpfile.js").text =
                new SimpleTemplateEngine()
                        .createTemplate(getClass().getResourceAsStream('/gulpfile.js').text)
                        .make(bindings)
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
