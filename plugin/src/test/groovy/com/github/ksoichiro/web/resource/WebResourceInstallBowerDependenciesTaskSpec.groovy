package com.github.ksoichiro.web.resource

import com.github.ksoichiro.web.resource.extension.WebResourceExtension
import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import org.junit.ClassRule
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import spock.lang.Shared

class WebResourceInstallBowerDependenciesTaskSpec extends BaseSpec {
    @ClassRule
    @Shared
    TemporaryFolder temporaryFolder = new TemporaryFolder()

    def setupSpec() {
        Project project = ProjectBuilder.builder().withProjectDir(temporaryFolder.root).build()
        project.apply plugin: PLUGIN_ID
        project.evaluate()
        project.tasks.webResourceSetupNodeDependencies.execute()
    }

    def exec() {
        setup:
        File root = temporaryFolder.root
        Project project = ProjectBuilder.builder().withProjectDir(root).build()
        setupProject(root, project)
        project.apply plugin: PLUGIN_ID
        def extension = project.extensions.webResource as WebResourceExtension
        extension.bower.dependencies {
            install name: "jquery", version: "1.11.2", filter: ["dist/*.min.*"]
        }
        project.evaluate()

        when:
        project.tasks.webResourceInstallBowerDependencies.execute()

        then:
        notThrown(Exception)
    }

    def disabled() {
        setup:
        File root = temporaryFolder.root
        Project project = ProjectBuilder.builder().withProjectDir(root).build()
        setupProject(root, project)
        project.apply plugin: PLUGIN_ID
        project.evaluate()

        when:
        project.tasks.webResourceInstallBowerDependencies.execute()

        then:
        notThrown(Exception)
        !new File("${temporaryFolder.root}/build/webResource/bower_components").exists()
    }

    def removeExistentBowerJsonBeforeInstall() {
        setup:
        File root = temporaryFolder.root
        Project project = ProjectBuilder.builder().withProjectDir(root).build()
        setupProject(root, project)
        project.apply plugin: PLUGIN_ID
        def extension = project.extensions.webResource as WebResourceExtension
        extension.bower.dependencies {
            install name: "jquery", version: "1.11.2", filter: ["dist/*.min.*"]
        }
        def workDir = new File("${temporaryFolder.root}/build/webResource")
        workDir.mkdirs()
        new File("${workDir}/bower.json").text = """\
            |{
            |  "dependencies": {
            |    "bootstrap": "3.3.4"
            |  }
            |}
            |""".stripMargin().stripIndent()
        project.evaluate()

        when:
        project.tasks.webResourceInstallBowerDependencies.execute()

        then:
        notThrown(Exception)
        !new File("${temporaryFolder.root}/build/webResource/bower_components/bootstrap").exists()
    }

    def update() {
        setup:
        File root = temporaryFolder.root
        Project project = ProjectBuilder.builder().withProjectDir(root).build()
        setupProject(root, project)
        project.apply plugin: PLUGIN_ID
        def extension = project.extensions.webResource as WebResourceExtension
        extension.bower.dependencies {
            install name: "jquery", version: "1.11.2", filter: ["src/**/*"]
        }
        project.evaluate()
        project.tasks.webResourceInstallBowerDependencies.execute()

        when:
        Project project2 = ProjectBuilder.builder().withProjectDir(root).build()
        project2.apply plugin: PLUGIN_ID
        def extension2 = project2.extensions.webResource as WebResourceExtension
        extension2.bower.dependencies {
            install name: "jquery", version: "2.1.4", filter: ["src/**"]
        }
        project2.evaluate()
        project2.tasks.webResourceInstallBowerDependencies.execute()

        then:
        notThrown(Exception)
        !new File("${root}/build/webResource/bower_components/jquery/src/support.js").exists()
        new File("${root}/build/webResource/bower_components/jquery/src/var/arr.js").exists()
    }

    void setupProject(File root, Project project) {
        File outputDir = new File(root, "build/webResource/outputs")
        project.delete(outputDir)

        outputDir = new File(root, "build/webResource/bower_components")
        project.delete(outputDir)
    }
}
