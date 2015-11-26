package com.github.ksoichiro.web.resource

import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import org.junit.Rule
import org.junit.rules.TemporaryFolder

class WebResourceSetupNodeDependenciesTaskSpec extends BaseSpec {
    @Rule
    TemporaryFolder temporaryFolder

    def exec() {
        setup:
        Project project = ProjectBuilder.builder().withProjectDir(temporaryFolder.root).build()

        when:
        project.apply plugin: PLUGIN_ID
        project.evaluate()
        project.tasks.webResourceSetupNodeDependencies.execute()

        then:
        notThrown(Exception)
    }
}
