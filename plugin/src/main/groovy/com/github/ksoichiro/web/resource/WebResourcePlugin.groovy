package com.github.ksoichiro.web.resource

import com.moowork.gradle.node.NodeExtension
import com.moowork.gradle.node.NodePlugin
import org.gradle.api.Plugin
import org.gradle.api.Project

class WebResourcePlugin implements Plugin<Project> {
    @Override
    void apply(Project project) {
        project.plugins.apply(NodePlugin.class)
        def node = project.node as NodeExtension
        node.with {
            version = '0.12.7'
            download = true
            workDir = project.file("${project.buildDir}/${WebResourceExtension.NAME}/nodejs")
            nodeModulesDir = project.file("${project.buildDir}/${WebResourceExtension.NAME}")
        }

        project.extensions.create(WebResourceExtension.NAME, WebResourceExtension, project)
        project.task(WebResourceInstallBowerDependenciesTask.NAME, type: WebResourceInstallBowerDependenciesTask)
        project.task(WebResourceInstallDependenciesTask.NAME, type: WebResourceInstallDependenciesTask)
        project.task(WebResourceCompileCoffeeScriptTask.NAME, type: WebResourceCompileCoffeeScriptTask)
        project.task(WebResourceCompileLessTask.NAME, type: WebResourceCompileLessTask)
        project.task(WebResourceCompileBowerTask.NAME, type: WebResourceCompileBowerTask)
        project.task(WebResourceCompileTask.NAME, type: WebResourceCompileTask)
        project.task(WebResourceWatchTask.NAME, type: WebResourceWatchTask)
        project.task(TriremeLessTask.NAME, type: TriremeLessTask)
    }
}
