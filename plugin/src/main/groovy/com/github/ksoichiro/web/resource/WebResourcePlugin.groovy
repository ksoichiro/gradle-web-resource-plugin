package com.github.ksoichiro.web.resource

import org.gradle.api.Plugin
import org.gradle.api.Project

class WebResourcePlugin implements Plugin<Project> {
    @Override
    void apply(Project project) {
        project.extensions.create(WebResourceExtension.NAME, WebResourceExtension, project)
        project.task(BowerInstallTask.NAME, type: BowerInstallTask)
        project.task(InstallBowerTask.NAME, type: InstallBowerTask)
        project.task(NpmInstallWrapperTask.NAME, type: NpmInstallWrapperTask)
        project.task(CompileTask.NAME, type: CompileTask)
    }
}
