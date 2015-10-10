package com.github.ksoichiro.web.resource

import org.gradle.api.Plugin
import org.gradle.api.Project

class WebResourcePlugin implements Plugin<Project> {
    @Override
    void apply(Project project) {
        project.extensions.create(WebResourceExtension.NAME, WebResourceExtension, project)
        project.task(WebResourceInstallBowerDependenciesTask.NAME, type: WebResourceInstallBowerDependenciesTask)
        project.task(WebResourceInstallDependenciesTask.NAME, type: WebResourceInstallDependenciesTask)
        project.task(WebResourceCompileCoffeeScriptTask.NAME, type: WebResourceCompileCoffeeScriptTask)
        project.task(WebResourceCompileLessTask.NAME, type: WebResourceCompileLessTask)
        project.task(WebResourceCompileBowerTask.NAME, type: WebResourceCompileBowerTask)
        project.task(WebResourceCompileTask.NAME, type: WebResourceCompileTask)
    }
}
