package com.github.ksoichiro.web.resource.task

import com.github.ksoichiro.web.resource.extension.BowerDependency
import com.github.ksoichiro.web.resource.util.PathResolver
import com.github.ksoichiro.web.resource.extension.WebResourceExtension
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

class WebResourceCopyBowerDependenciesTask extends DefaultTask {
    static String NAME = "webResourceCopyBowerDependencies"
    WebResourceExtension extension
    PathResolver pathResolver

    WebResourceCopyBowerDependenciesTask() {
        dependsOn([WebResourceInstallBowerDependenciesTask.NAME])
        project.afterEvaluate {
            extension = project.extensions.webResource
            pathResolver = new PathResolver(project, extension)
            getInputs()
                .dir(new File(extension.workDir, WebResourceInstallBowerDependenciesTask.BOWER_COMPONENTS_DIR))
                .property('bower', extension.bower.toString())
                .property('version', WebResourceExtension.VERSION)
            getOutputs().files(pathResolver.retrieveValidPaths(pathResolver.getDestLib()))
        }
    }

    @TaskAction
    void exec() {
        removeOldFiles()
        if (hasBowerDependencies()) {
            copyDependencies()
            extension.bower.dependencies.each { dependency ->
                renameDependencyDirIfRequired(dependency)
            }
        }
    }

    void removeOldFiles() {
        project.delete(project.file("${extension.base.dest}/${extension.lib.dest}").absolutePath)
    }

    boolean hasBowerDependencies() {
        !extension.bower.dependencies.isEmpty()
    }

    void copyDependencies() {
        project.copy {
            from project.fileTree("${extension.workDir}/bower_components").matching {
                extension.bower.dependencies.each { dependency ->
                    String[] expr = dependency.filter
                    if (expr) {
                        expr.each { e -> it.include("${dependency.getCacheName()}/${e}") }
                    } else {
                        it.include("${dependency.getCacheName()}/**/*")
                    }
                }
            }
            into "${extension.base.dest}/${extension.lib.dest}"
        }
    }

    void renameDependencyDirIfRequired(BowerDependency dependency) {
        def dependencyDir = project.file("${extension.base.dest}/${extension.lib.dest}/${dependency.getCacheName()}")
        if (dependencyDir.exists()) {
            dependencyDir.renameTo(project.file("${extension.base.dest}/${extension.lib.dest}/${dependency.getOutputName()}"))
        }
    }
}
