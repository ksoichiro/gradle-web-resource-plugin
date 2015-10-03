package com.github.ksoichiro.web.resource

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

class WebResourceCompileBowerTask extends DefaultTask {
    static String NAME = "webResourceCompileBower"
    WebResourceExtension extension

    WebResourceCompileBowerTask() {
        dependsOn([WebResourceInstallBowerDependenciesTask.NAME])
        project.afterEvaluate {
            extension = project.extensions.webResource
            getInputs()
                .property('bower', extension.bower)
                .property('version', WebResourceExtension.VERSION)
            getOutputs().files(retrieveValidPaths(getDestLib()))
        }
    }

    @TaskAction
    void exec() {
        project.copy {
            from project.fileTree("${extension.workDir}/bower_components").matching {
                if (extension.bower.overrides) {
                    extension.bower.overrides.each { k, v ->
                        String expr = v.main
                        if (v.containsKey('main')) {
                            it.include("${k}/${expr}")
                        } else {
                            it.include("${k}/**/*")
                        }
                    }
                } else {
                    it.include("**/*")
                }
            }
            into "${extension.base.dest}/${extension.lib.dest}"
        }
    }

    List retrieveValidPaths(String... paths) {
        List result = []
        paths.findAll { project.file("${extension.workDir}/${it}") }.each {
            result += "${extension.workDir}/${it}"
        }
        result
    }

    String getDestLib() {
        resolveDestPath(extension.lib?.dest)
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
}
