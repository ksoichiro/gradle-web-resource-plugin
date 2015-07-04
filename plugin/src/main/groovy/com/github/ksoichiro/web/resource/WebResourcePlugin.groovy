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
        if (!node.npmVersion) {
            node.version = '0.11.13'
            node.npmVersion = '1.4.16'
        }
        node.download = true
        node.workDir = project.file("${project.buildDir}/${WebResourceExtension.NAME}/nodejs")
        node.nodeModulesDir = project.file("${project.buildDir}/${WebResourceExtension.NAME}")

        project.extensions.create(WebResourceExtension.NAME, WebResourceExtension, project)
        project.task(BowerInstallTask.NAME, type: BowerInstallTask)
        project.task(InstallBowerTask.NAME, type: InstallBowerTask)
        project.task(NpmInstallWrapperTask.NAME, type: NpmInstallWrapperTask)
        project.task(CompileTask.NAME, type: CompileTask)
    }
}
