package com.github.ksoichiro.web.resource

import com.github.ksoichiro.web.resource.extension.WebResourceExtension
import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import org.junit.Rule
import org.junit.rules.TemporaryFolder

class WebResourceCompileCoffeeScriptTaskSpec extends BaseSpec {
    @Rule
    TemporaryFolder temporaryFolder

    def exec() {
        setup:
        File root = temporaryFolder.root
        Project project = ProjectBuilder.builder().withProjectDir(root).build()
        File srcDir = new File(root, "src/main/coffee")
        srcDir.mkdirs()
        new File(srcDir, "app.coffee").text = """\
            |console.log 'Hello, world!'
            |""".stripMargin().stripIndent()
        project.apply plugin: PLUGIN_ID
        project.evaluate()
        project.tasks.webResourceSetupNodeDependencies.execute()
        project.tasks.webResourceInstallBowerDependencies.execute()

        when:
        project.tasks.webResourceCompileCoffeeScript.execute()
        def compiled = new File("${root}/build/webResource/outputs/js/app.js")

        then:
        notThrown(Exception)
        compiled.exists()
        compiled.text == "!function(){console.log(\"Hello, world!\")}.call(this);"
    }

    def disabled() {
        setup:
        File root = temporaryFolder.root
        Project project = ProjectBuilder.builder().withProjectDir(root).build()
        File srcDir = new File(root, "src/main/coffee")
        srcDir.mkdirs()
        new File(srcDir, "app.coffee").text = """\
            |console.log 'Hello, world!'
            |""".stripMargin().stripIndent()
        project.apply plugin: PLUGIN_ID
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
