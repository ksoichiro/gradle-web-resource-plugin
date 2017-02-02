package com.github.ksoichiro.web.resource

import com.github.ksoichiro.web.resource.extension.WebResourceExtension
import com.github.ksoichiro.web.resource.task.*
import org.gradle.api.Plugin
import org.gradle.api.Project

class WebResourcePlugin implements Plugin<Project> {
    @Override
    void apply(Project project) {
        project.extensions.create(WebResourceExtension.NAME, WebResourceExtension, project)
        project.task(WebResourceInstallBowerDependenciesTask.NAME, type: WebResourceInstallBowerDependenciesTask)
        project.task(WebResourceCompileCoffeeScriptTask.NAME, type: WebResourceCompileCoffeeScriptTask)
        project.task(WebResourceCompileLessTask.NAME, type: WebResourceCompileLessTask)
        project.task(WebResourceCopyBowerDependenciesTask.NAME, type: WebResourceCopyBowerDependenciesTask)
        project.task(WebResourceCompileTask.NAME, type: WebResourceCompileTask)
        project.task(WebResourceTestCoffeeScriptTask.NAME, type: WebResourceTestCoffeeScriptTask)
    }
}
