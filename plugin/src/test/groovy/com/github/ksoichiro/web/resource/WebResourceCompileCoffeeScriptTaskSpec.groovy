package com.github.ksoichiro.web.resource

import com.github.ksoichiro.web.resource.extension.WebResourceExtension
import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import spock.lang.Specification

class WebResourceCompileCoffeeScriptTaskSpec extends Specification {
    @Rule
    TemporaryFolder temporaryFolder

    def exec() {
        setup:
        File root = temporaryFolder.root
        Project project = ProjectBuilder.builder().withProjectDir(root).build()
        File srcDir = new File(root, "src/main/coffee")
        srcDir.mkdirs()
        new File(srcDir, "app.coffee").text = """\
            |console log 'Hello, world!'
            |""".stripMargin().stripIndent()
        project.apply plugin: "com.github.ksoichiro.web.resource"
        project.evaluate()
        project.tasks.webResourceSetupNodeDependencies.execute()
        project.tasks.webResourceInstallBowerDependencies.execute()

        when:
        project.tasks.webResourceCompileCoffeeScript.execute()

        then:
        notThrown(Exception)
        new File("${root}/build/webResource/outputs/js/app.js").exists()
    }

    def disabled() {
        setup:
        File root = temporaryFolder.root
        Project project = ProjectBuilder.builder().withProjectDir(root).build()
        File srcDir = new File(root, "src/main/coffee")
        srcDir.mkdirs()
        new File(srcDir, "app.coffee").text = """\
            |console log 'Hello, world!'
            |""".stripMargin().stripIndent()
        project.apply plugin: "com.github.ksoichiro.web.resource"
        def extension = project.extensions.webResource as WebResourceExtension
        extension.coffeeScript.enabled = false
        project.evaluate()
        project.tasks.webResourceSetupNodeDependencies.execute()
        project.tasks.webResourceInstallBowerDependencies.execute()

        when:
        project.tasks.webResourceCompileCoffeeScript.execute()

        then:
        notThrown(Exception)
        !new File("${root}/build/webResource/outputs/js/app.js").exists()
    }
}
