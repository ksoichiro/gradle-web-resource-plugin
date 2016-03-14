package com.github.ksoichiro.web.resource

import com.github.ksoichiro.web.resource.extension.WebResourceExtension
import org.gradle.api.Project
import org.gradle.api.logging.LogLevel
import org.gradle.testfixtures.ProjectBuilder
import org.junit.ClassRule
import org.junit.rules.TemporaryFolder
import spock.lang.Shared

class WebResourceTestCoffeeScriptTaskSpec extends BaseSpec {
    @ClassRule
    @Shared
    TemporaryFolder temporaryFolder = new TemporaryFolder()

    def setupSpec() {
        Project project = ProjectBuilder.builder().withProjectDir(temporaryFolder.root).build()
        project.apply plugin: PLUGIN_ID
        project.evaluate()
        project.tasks.webResourceInstallBowerDependencies.execute()
    }

    def testExec() {
        setup:
        File root = temporaryFolder.root
        Project project = ProjectBuilder.builder().withProjectDir(root).build()
        setupProject(root, project)
        generateCode(root)
        project.apply plugin: PLUGIN_ID
        def extension = project.extensions.webResource as WebResourceExtension
        extension.coffeeScript.logLevel = LogLevel.INFO
        extension.testCoffeeScript.logLevel = LogLevel.INFO
        project.evaluate()
        project.tasks.webResourceCompileCoffeeScript.execute()

        when:
        project.tasks.webResourceTestCoffeeScript.execute()
        def compiledSrcJs = new File("${root}/build/webResource/outputs/test/app.js")
        def compiledTestJs = new File("${root}/build/webResource/outputs/test/app_test.js")

        then:
        notThrown(Exception)
        compiledSrcJs.exists()
        compiledTestJs.exists()
    }

    def coffeeScriptIsDisabled() {
        setup:
        File root = temporaryFolder.root
        Project project = ProjectBuilder.builder().withProjectDir(root).build()
        setupProject(root, project)
        generateCode(root)
        project.apply plugin: PLUGIN_ID
        def extension = project.extensions.webResource as WebResourceExtension
        extension.coffeeScript.enabled = false
        extension.coffeeScript.logLevel = LogLevel.INFO
        extension.testCoffeeScript.logLevel = LogLevel.INFO
        project.evaluate()
        project.tasks.webResourceCompileCoffeeScript.execute()

        when:
        project.tasks.webResourceTestCoffeeScript.execute()
        def compiledSrcJs = new File("${root}/build/webResource/outputs/test/app.js")
        def compiledTestJs = new File("${root}/build/webResource/outputs/test/app_test.js")

        then:
        notThrown(Exception)
        !compiledSrcJs.exists()
        !compiledTestJs.exists()
    }

    def testCoffeeScriptIsDisabled() {
        setup:
        File root = temporaryFolder.root
        Project project = ProjectBuilder.builder().withProjectDir(root).build()
        setupProject(root, project)
        generateCode(root)
        project.apply plugin: PLUGIN_ID
        def extension = project.extensions.webResource as WebResourceExtension
        extension.testCoffeeScript.enabled = false
        extension.coffeeScript.logLevel = LogLevel.INFO
        extension.testCoffeeScript.logLevel = LogLevel.INFO
        project.evaluate()
        project.tasks.webResourceCompileCoffeeScript.execute()

        when:
        project.tasks.webResourceTestCoffeeScript.execute()
        def compiledSrcJs = new File("${root}/build/webResource/outputs/test/app.js")
        def compiledTestJs = new File("${root}/build/webResource/outputs/test/app_test.js")

        then:
        notThrown(Exception)
        !compiledSrcJs.exists()
        !compiledTestJs.exists()
    }

    def bothAreDisabled() {
        setup:
        File root = temporaryFolder.root
        Project project = ProjectBuilder.builder().withProjectDir(root).build()
        setupProject(root, project)
        generateCode(root)
        project.apply plugin: PLUGIN_ID
        def extension = project.extensions.webResource as WebResourceExtension
        extension.coffeeScript.enabled = false
        extension.testCoffeeScript.enabled = false
        extension.coffeeScript.logLevel = LogLevel.INFO
        extension.testCoffeeScript.logLevel = LogLevel.INFO
        project.evaluate()
        project.tasks.webResourceCompileCoffeeScript.execute()

        when:
        project.tasks.webResourceTestCoffeeScript.execute()
        def compiledSrcJs = new File("${root}/build/webResource/outputs/test/app.js")
        def compiledTestJs = new File("${root}/build/webResource/outputs/test/app_test.js")

        then:
        notThrown(Exception)
        !compiledSrcJs.exists()
        !compiledTestJs.exists()
    }

    void setupProject(File root, Project project) {
        File srcDir = new File(root, "src/main/coffee")
        project.delete(srcDir)
        srcDir.mkdirs()

        srcDir = new File(root, "src/test/coffee")
        project.delete(srcDir)
        srcDir.mkdirs()

        File outputDir = new File(root, "build/webResource/outputs")
        project.delete(outputDir)
    }

    void generateCode(File root) {
        File srcDir = new File(root, "src/main/coffee")
        new File(srcDir, "app.coffee").text = """\
            |@func = (a) -> a + 1
            |""".stripMargin().stripIndent()
        File testSrcDir = new File(root, "src/test/coffee")
        new File(testSrcDir, "app_test.coffee").text = """\
            |app = require './app'
            |assert = require 'assert'
            |describe 'app', ->
            |  describe '#func', ->
            |    it 'should return 2 when 1 is given', ->
            |      assert app.func(1), 2
            |""".stripMargin().stripIndent()
    }
}
