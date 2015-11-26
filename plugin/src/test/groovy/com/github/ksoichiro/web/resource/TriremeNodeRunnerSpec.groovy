package com.github.ksoichiro.web.resource

import com.github.ksoichiro.web.resource.node.TriremeNodeRunner
import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder

class TriremeNodeRunnerSpec extends BaseSpec {
    def exec() {
        setup:
        Project project = ProjectBuilder.builder().withProjectDir(new File("src/test/projects/node")).build()
        def runner = new TriremeNodeRunner(
            scriptName: "test.js",
            workingDir: project.rootDir,
            args: [])

        when:
        runner.exec()

        then:
        notThrown(Exception)
    }
}
