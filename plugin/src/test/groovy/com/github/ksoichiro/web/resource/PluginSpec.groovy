package com.github.ksoichiro.web.resource

import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import spock.lang.Specification

class PluginSpec extends Specification {
    static final String PLUGIN_ID = 'com.github.ksoichiro.web.resource'

    def "apply"() {
        setup:
        Project project = ProjectBuilder.builder().build()

        when:
        project.plugins.apply PLUGIN_ID

        then:
        project.tasks.webResourceInstallBowerDependencies instanceof WebResourceInstallBowerDependenciesTask
        project.tasks.webResourceInstallDependencies instanceof WebResourceInstallDependenciesTask
        project.tasks.webResourceCompileCoffeeScript instanceof WebResourceCompileCoffeeScriptTask
        project.tasks.webResourceCompileLess instanceof WebResourceCompileLessTask
        project.tasks.webResourceCompileBower instanceof WebResourceCompileBowerTask
        project.tasks.webResourceCompile instanceof WebResourceCompileTask
        project.tasks.webResourceWatch instanceof WebResourceWatchTask
        project.extensions.webResource instanceof WebResourceExtension
    }
}