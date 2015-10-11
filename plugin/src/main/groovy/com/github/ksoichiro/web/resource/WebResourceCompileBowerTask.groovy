package com.github.ksoichiro.web.resource

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

class WebResourceCompileBowerTask extends DefaultTask {
    static String NAME = "webResourceCompileBower"
    WebResourceExtension extension
    PathResolver pathResolver

    WebResourceCompileBowerTask() {
        dependsOn([WebResourceInstallBowerDependenciesTask.NAME])
        project.afterEvaluate {
            extension = project.extensions.webResource
            pathResolver = new PathResolver(project, extension)
            getInputs()
                .property('bower', extension.bower)
                .property('version', WebResourceExtension.VERSION)
            getOutputs().files(pathResolver.retrieveValidPaths(pathResolver.getDestLib()))
        }
    }

    @TaskAction
    void exec() {
        project.copy {
            from project.fileTree("${extension.workDir}/bower_components").matching {
                if (!extension.bower.dependencies.isEmpty()) {
                    extension.bower.dependencies.each { dependency ->
                        String[] expr = dependency.filter
                        if (expr) {
                            expr.each { e -> it.include("${dependency.name}/${e}") }
                        } else {
                            it.include("${dependency.name}/**/*")
                        }
                    }
                } else {
                    it.include("**/*")
                }
            }
            into "${extension.base.dest}/${extension.lib.dest}"
        }
    }
}
