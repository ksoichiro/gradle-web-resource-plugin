package com.github.ksoichiro.web.resource

import com.github.ksoichiro.web.resource.node.TriremeNodeRunner
import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import org.junit.Rule
import org.junit.rules.TemporaryFolder

class TriremeNodeRunnerSpec extends BaseSpec {
    @Rule
    TemporaryFolder temporaryFolder

    def exec() {
        setup:
        Project project = ProjectBuilder.builder().withProjectDir(temporaryFolder.root).build()
        temporaryFolder.newFile("test.js").text = """\
            |console.log('Hello, world!');
            |""".stripMargin().stripIndent()
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
