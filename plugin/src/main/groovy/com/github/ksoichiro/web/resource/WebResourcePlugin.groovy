package com.github.ksoichiro.web.resource

import com.moowork.gradle.node.NodeExtension
import com.moowork.gradle.node.NodePlugin
import org.gradle.api.Plugin
import org.gradle.api.Project

class WebResourcePlugin implements Plugin<Project> {
    static final String DEFAULT_NODE_VERSION = '0.11.13'
    static final String DEFAULT_NPM_VERSION = '1.4.16'

    @Override
    void apply(Project project) {
        project.plugins.apply(NodePlugin.class)
        def node = project.node as NodeExtension
        if (!node.npmVersion) {
            node.version = DEFAULT_NODE_VERSION
            node.npmVersion = DEFAULT_NPM_VERSION
        }
        node.download = true
        node.workDir = project.file("${project.buildDir}/${WebResourceExtension.NAME}/nodejs")
        node.nodeModulesDir = project.file("${project.buildDir}/${WebResourceExtension.NAME}")

        project.extensions.create(WebResourceExtension.NAME, WebResourceExtension, project)
        project.task(WebResourceInstallBowerDependenciesTask.NAME, type: WebResourceInstallBowerDependenciesTask)
        project.task(WebResourceInstallDependenciesTask.NAME, type: WebResourceInstallDependenciesTask)
        project.task(WebResourceCompileTask.NAME, type: WebResourceCompileTask)
        project.task(WebResourceWatchTask.NAME, type: WebResourceWatchTask)
    }
}
