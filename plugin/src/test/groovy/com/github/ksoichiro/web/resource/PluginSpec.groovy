package com.github.ksoichiro.web.resource

import com.github.ksoichiro.web.resource.extension.WebResourceExtension
import com.github.ksoichiro.web.resource.task.WebResourceCompileCoffeeScriptTask
import com.github.ksoichiro.web.resource.task.WebResourceCompileLessTask
import com.github.ksoichiro.web.resource.task.WebResourceCompileTask
import com.github.ksoichiro.web.resource.task.WebResourceCopyBowerDependenciesTask
import com.github.ksoichiro.web.resource.task.WebResourceInstallBowerDependenciesTask
import com.github.ksoichiro.web.resource.task.WebResourceSetupNodeDependenciesTask
import org.gradle.api.Project
import org.gradle.api.artifacts.dsl.RepositoryHandler
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
        project.tasks.webResourceSetupNodeDependencies instanceof WebResourceSetupNodeDependenciesTask
        project.tasks.webResourceCompileCoffeeScript instanceof WebResourceCompileCoffeeScriptTask
        project.tasks.webResourceCompileLess instanceof WebResourceCompileLessTask
        project.tasks.webResourceCopyBowerDependencies instanceof WebResourceCopyBowerDependenciesTask
        project.tasks.webResourceCompile instanceof WebResourceCompileTask
        project.extensions.webResource instanceof WebResourceExtension
    }

    def "evaluate1"() {
        when:
        Project project = ProjectBuilder.builder().withProjectDir(new File("src/test/projects/compile")).build().with { project ->
            apply plugin: PLUGIN_ID
            webResource {
                bower {
                    dependencies {
                        install name: 'jquery', version: "1.11.2"
                        install name: 'bootstrap', version: "3.3.4"
                    }
                }
            }
            evaluate()
            project
        }

        then:
        project
    }

    def "evaluate2"() {
        when:
        Project project = ProjectBuilder.builder().withProjectDir(new File("src/test/projects/compile")).build().with { project ->
            apply plugin: PLUGIN_ID
            webResource {
                less {
                    src = 'stylesheets'
                    dest = 'stylesheets'
                    filter = ['*.less']
                    minify = false
                }
                coffeeScript {
                    src = 'stylesheets'
                    dest = 'stylesheets'
                    filter = ['*.coffee']
                    minify = false
                }
                bower {
                    dependencies {
                        install name: 'jquery', version: "1.11.2"
                        install name: 'bootstrap', version: "3.3.4"
                    }
                }
            }
            evaluate()
            project
        }

        then:
        project
    }


    def "evaluate3"() {
        when:
        Project project = ProjectBuilder.builder().withProjectDir(new File("src/test/projects/compile")).build().with { project ->
            apply plugin: PLUGIN_ID
            webResource {
                less {
                    src 'stylesheets'
                    dest 'stylesheets'
                }
                coffeeScript {
                    src 'stylesheets'
                    dest 'stylesheets'
                }
            }
            evaluate()
            project
        }

        then:
        project
    }

    void deleteOutputs(Project project) {
        ['.gradle', 'build'].each {
            if (project.file(it).exists()) {
                project.delete(it)
            }
        }
    }

    void setupRepositories(Project project) {
        project.repositories { RepositoryHandler it ->
            it.mavenCentral()
        }
    }
}
