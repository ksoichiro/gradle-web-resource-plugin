package com.github.ksoichiro.web.resource

import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import spock.lang.Specification

class WebResourceCompileTaskSpec extends Specification {
    @Rule
    TemporaryFolder temporaryFolder

    def exec() {
        setup:
        Project project = ProjectBuilder.builder().withProjectDir(temporaryFolder.root).build()

        when:
        project.apply plugin: "com.github.ksoichiro.web.resource"
        project.evaluate()
        project.tasks.webResourceCompile.execute()

        then:
        notThrown(Exception)
    }
}
