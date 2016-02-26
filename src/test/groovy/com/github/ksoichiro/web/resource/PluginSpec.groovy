package com.github.ksoichiro.web.resource

import com.github.ksoichiro.web.resource.extension.WebResourceExtension
import com.github.ksoichiro.web.resource.task.*
import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import org.junit.Rule
import org.junit.rules.TemporaryFolder

class PluginSpec extends BaseSpec {
    @Rule
    TemporaryFolder temporaryFolder

    def "apply"() {
        setup:
        Project project = ProjectBuilder.builder().build()

        when:
        project.plugins.apply PLUGIN_ID

        then:
        project.tasks.webResourceInstallBowerDependencies instanceof WebResourceInstallBowerDependenciesTask
        project.tasks.webResourceCompileCoffeeScript instanceof WebResourceCompileCoffeeScriptTask
        project.tasks.webResourceCompileLess instanceof WebResourceCompileLessTask
        project.tasks.webResourceCopyBowerDependencies instanceof WebResourceCopyBowerDependenciesTask
        project.tasks.webResourceCompile instanceof WebResourceCompileTask
        project.extensions.webResource instanceof WebResourceExtension
    }

    def "evaluate1"() {
        when:
        Project project = ProjectBuilder.builder().withProjectDir(temporaryFolder.root).build().with { project ->
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
        Project project = ProjectBuilder.builder().withProjectDir(temporaryFolder.root).build().with { project ->
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
        Project project = ProjectBuilder.builder().withProjectDir(temporaryFolder.root).build().with { project ->
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
}
