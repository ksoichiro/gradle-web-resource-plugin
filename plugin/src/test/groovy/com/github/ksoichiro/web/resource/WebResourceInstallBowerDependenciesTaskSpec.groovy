package com.github.ksoichiro.web.resource

import com.github.ksoichiro.web.resource.extension.WebResourceExtension
import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import spock.lang.Specification

class WebResourceInstallBowerDependenciesTaskSpec extends Specification {
    @Rule
    TemporaryFolder temporaryFolder

    def exec() {
        setup:
        Project project = ProjectBuilder.builder().withProjectDir(temporaryFolder.root).build()
        project.apply plugin: "com.github.ksoichiro.web.resource"
        def extension = project.extensions.webResource as WebResourceExtension
        extension.bower.dependencies {
            install name: "jquery", version: "1.11.2", filter: ["dist/*.min.*"]
        }
        project.evaluate()
        project.tasks.webResourceSetupNodeDependencies.execute()

        when:
        project.tasks.webResourceInstallBowerDependencies.execute()

        then:
        notThrown(Exception)
    }
}
