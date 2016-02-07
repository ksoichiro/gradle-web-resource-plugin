package com.github.ksoichiro.web.resource

import com.github.ksoichiro.web.resource.extension.WebResourceExtension
import org.gradle.api.Project
import org.gradle.api.logging.LogLevel
import org.gradle.testfixtures.ProjectBuilder
import org.junit.ClassRule
import org.junit.rules.TemporaryFolder
import spock.lang.Shared

/**
 * Tests for tasks that depends on webResourceInstallBowerDependencies
 */
class WebResourceCompileSpec extends BaseSpec {
    @ClassRule
    @Shared
    TemporaryFolder temporaryFolder = new TemporaryFolder()

    def setupSpec() {
        Project project = ProjectBuilder.builder().withProjectDir(temporaryFolder.root).build()
        project.apply plugin: PLUGIN_ID
        project.evaluate()
        project.tasks.webResourceSetupNodeDependencies.execute()
        project.tasks.webResourceInstallBowerDependencies.execute()
    }

    def coffeeScriptExec() {
        setup:
        File root = temporaryFolder.root
        Project project = ProjectBuilder.builder().withProjectDir(root).build()
        setupProject(root, project)
        File srcDir = new File(root, "src/main/coffee")
        new File(srcDir, "app.coffee").text = """\
            |console.log 'Hello, world!'
            |""".stripMargin().stripIndent()
        project.apply plugin: PLUGIN_ID
        def extension = project.extensions.webResource as WebResourceExtension
        extension.coffeeScript.logLevel = LogLevel.INFO
        project.evaluate()

        when:
        project.tasks.webResourceCompileCoffeeScript.execute()
        def compiled = new File("${root}/build/webResource/outputs/js/app.js")

        then:
        notThrown(Exception)
        compiled.exists()
        compiled.text == "!function(){console.log(\"Hello, world!\")}.call(this);"
    }

    def coffeeScriptDisabled() {
        setup:
        File root = temporaryFolder.root
        Project project = ProjectBuilder.builder().withProjectDir(root).build()
        setupProject(root, project)
        File srcDir = new File(root, "src/main/coffee")
        new File(srcDir, "app.coffee").text = """\
            |console.log 'Hello, world!'
            |""".stripMargin().stripIndent()
        project.apply plugin: PLUGIN_ID
        def extension = project.extensions.webResource as WebResourceExtension
        extension.coffeeScript.enabled = false
        project.evaluate()

        when:
        project.tasks.webResourceCompileCoffeeScript.execute()

        then:
        notThrown(Exception)
        !new File("${root}/build/webResource/outputs/js/app.js").exists()
    }

    def lessExec() {
        setup:
        File root = temporaryFolder.root
        Project project = ProjectBuilder.builder().withProjectDir(root).build()
        setupProject(root, project)
        File srcDir = new File(root, "src/main/less")
        new File(srcDir, "app.less").text = """\
            |.foo {
            |  .bar {
            |    color: #f00;
            |  }
            |}
            |""".stripMargin().stripIndent()
        project.apply plugin: PLUGIN_ID
        def extension = project.extensions.webResource as WebResourceExtension
        extension.less.logLevel = LogLevel.INFO
        project.evaluate()

        when:
        project.tasks.webResourceCompileLess.execute()
        def compiled = new File("${root}/build/webResource/outputs/css/app.css")

        then:
        notThrown(Exception)
        compiled.exists()
        compiled.text == ".foo .bar{color:#f00}"
    }

    def lessDisabled() {
        setup:
        File root = temporaryFolder.root
        Project project = ProjectBuilder.builder().withProjectDir(root).build()
        setupProject(root, project)
        File srcDir = new File(root, "src/main/less")
        new File(srcDir, "app.less").text = """\
            |.foo {
            |  .bar {
            |    color: #f00;
            |  }
            |}
            |""".stripMargin().stripIndent()
        project.apply plugin: PLUGIN_ID
        def extension = project.extensions.webResource as WebResourceExtension
        extension.less.enabled = false
        project.evaluate()

        when:
        project.tasks.webResourceCompileLess.execute()

        then:
        notThrown(Exception)
        !new File("${root}/build/webResource/outputs/css/app.css").exists()
    }

    def lessFilters() {
        setup:
        File root = temporaryFolder.root
        Project project = ProjectBuilder.builder().withProjectDir(root).build()
        setupProject(root, project)
        File srcDir = new File(root, "src/main/less")
        ["a", "b", "c"].each {
            new File(srcDir, "${it}.less").text = """\
                |.${it} {
                |  color: #f00;
                |}
                |""".stripMargin().stripIndent()
        }
        project.apply plugin: PLUGIN_ID
        def extension = project.extensions.webResource as WebResourceExtension
        extension.less.logLevel = LogLevel.INFO
        extension.less.filters {
            exclude '**/*.less'
            include '**/b.less'
        }
        project.evaluate()

        when:
        project.tasks.webResourceCompileLess.execute()

        then:
        notThrown(Exception)
        !new File("${root}/build/webResource/outputs/css/a.css").exists()
        new File("${root}/build/webResource/outputs/css/b.css").exists()
        !new File("${root}/build/webResource/outputs/css/c.css").exists()
    }

    void setupProject(File root, Project project) {
        File srcDir = new File(root, "src/main/less")
        project.delete(srcDir)
        srcDir.mkdirs()

        srcDir = new File(root, "src/main/coffee")
        project.delete(srcDir)
        srcDir.mkdirs()

        File outputDir = new File(root, "build/webResource/outputs")
        project.delete(outputDir)
    }
}
