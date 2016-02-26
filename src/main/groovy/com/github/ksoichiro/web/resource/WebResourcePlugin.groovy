package com.github.ksoichiro.web.resource

import com.github.ksoichiro.web.resource.extension.WebResourceExtension
import com.github.ksoichiro.web.resource.task.*
import org.fusesource.jansi.Ansi
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

        // fgBright is not available in the last release, so add it dynamically
        Ansi.metaClass.fgBright { Ansi.Color color ->
            delegate.attributeOptions.add(Integer.valueOf(color.fgBright()))
            delegate
        }
    }
}
