package com.github.ksoichiro.web.resource

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
        project.tasks.webResourceInstallDependencies instanceof WebResourceInstallDependenciesTask
        project.tasks.webResourceCompileCoffeeScript instanceof WebResourceCompileCoffeeScriptTask
        project.tasks.webResourceCompileLess instanceof WebResourceCompileLessTask
        project.tasks.webResourceCompileBower instanceof WebResourceCompileBowerTask
        project.tasks.webResourceCompile instanceof WebResourceCompileTask
        project.tasks.webResourceWatch instanceof WebResourceWatchTask
        project.extensions.webResource instanceof WebResourceExtension
    }

    def "evaluate1"() {
        when:
        Project project = ProjectBuilder.builder().withProjectDir(new File("src/test/projects/compile")).build().with { project ->
            apply plugin: PLUGIN_ID
            webResource {
                bower = [
                        dependencies: [
                                jquery   : "1.11.2",
                                bootstrap: "3.3.4",
                        ]
                ]
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
                npm = [
                        devDependencies: [
                                "gulp"            : "3.9.0",
                                "bower"           : "1.4.1",
                                "main-bower-files": "2.9.0",
                                "gulp-less"       : "3.0.3",
                                "gulp-minify-css" : "1.2.0",
                                "gulp-coffee"     : "2.3.1",
                                "gulp-filter"     : "2.0.2",
                                "gulp-uglify"     : "1.2.0",
                                "gulp-include"    : "2.0.2",
                                "fs-extra"        : "0.22.1"
                        ]
                ]
                bower = [
                        dependencies: [
                                jquery   : "1.11.2",
                                bootstrap: "3.3.4",
                        ]
                ]
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

    def "gulp tasks"() {
        setup:
        Project project = ProjectBuilder.builder().withProjectDir(new File("src/test/projects/compile")).build().with { project ->
            apply plugin: PLUGIN_ID
            evaluate()
            project
        }

        expect:
        project.tasks.webResourceCompileCoffeeScript.gulpCommand == 'coffee'
        project.tasks.webResourceCompileLess.gulpCommand == 'less'
        project.tasks.webResourceCompileBower.gulpCommand == 'bower-files'
        project.tasks.webResourceWatch.gulpCommand == 'watch'
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