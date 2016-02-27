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
            getOutputs().files(pathResolver.retrieveValidDestLibPaths())
            onlyIf {
                extension.bower.dependencies.size()
            }
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
        if (extension.lib.cleanOnUpdate) {
            File targetDir = project.file("${extension.base.dest}/${extension.lib.dest}")
            if (extension.lib.excludeFromClean && !extension.lib.excludeFromClean.isEmpty()) {
                // Delete all files except files that are listed in excludeFromClean
                targetDir.eachFile {
                    if (!extension.lib.excludeFromClean.contains(it.name)) {
                        project.delete(it.absolutePath)
                    }
                }
            } else {
                // Delete all
                project.delete(targetDir.absolutePath)
            }
        }
    }

    boolean hasBowerDependencies() {
        !extension.bower.dependencies.isEmpty()
    }

    void copyDependencies() {
        if (extension.bower.copyAll) {
            project.copy {
                from project.fileTree("${extension.workDir}/bower_components")
                into "${extension.base.dest}/${extension.lib.dest}"
            }
        } else {
            project.copy {
                from project.fileTree("${extension.workDir}/bower_components").matching {
                    extension.bower.dependencies.each { dependency ->
                        String[] expr = dependency.filter
                        // Try copying both the name and the cache name
                        // not to drop some dependencies accidentally
                        if (expr) {
                            expr.each { e ->
                                it.include("${dependency.getCacheName()}/${e}")
                                it.include("${dependency.getName()}/${e}")
                            }
                        } else {
                            it.include("${dependency.getCacheName()}/**/*")
                            it.include("${dependency.getName()}/**/*")
                        }
                    }
                }
                into "${extension.base.dest}/${extension.lib.dest}"
            }
        }

        // Force directory name to dependency.name
        project.file("${extension.base.dest}/${extension.lib.dest}").eachDir { dir ->
            extension.bower.dependencies.each { dependency ->
                if (dir.name == dependency.cacheName && dir.name != dependency.name) {
                    dir.renameTo(project.file("${extension.base.dest}/${extension.lib.dest}/${dependency.name}"))
                }
            }
        }
    }

    void renameDependencyDirIfRequired(BowerDependency dependency) {
        // Rename directory name from dependency.name to dependency.outputName if it's specified
        def dependencyDir = project.file("${extension.base.dest}/${extension.lib.dest}/${dependency.getName()}")
        if (dependencyDir.exists()) {
            dependencyDir.renameTo(project.file("${extension.base.dest}/${extension.lib.dest}/${dependency.getOutputName()}"))
        }
    }
}
