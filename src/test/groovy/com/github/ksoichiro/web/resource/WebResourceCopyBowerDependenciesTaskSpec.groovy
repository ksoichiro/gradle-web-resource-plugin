package com.github.ksoichiro.web.resource

import com.github.ksoichiro.web.resource.extension.WebResourceExtension
import org.gradle.api.Project
import org.gradle.api.logging.LogLevel
import org.gradle.testfixtures.ProjectBuilder
import org.junit.ClassRule
import org.junit.rules.TemporaryFolder
import spock.lang.Shared

class WebResourceCopyBowerDependenciesTaskSpec extends BaseSpec {
    @ClassRule
    @Shared
    TemporaryFolder temporaryFolder = new TemporaryFolder()

    def exec() {
        setup:
        File root = temporaryFolder.root
        Project project = ProjectBuilder.builder().withProjectDir(root).build()
        setupProject(root, project)
        File externalDependency = new File("${root}/build/webResource/outputs/lib/foo")
        externalDependency.mkdirs()
        project.apply plugin: PLUGIN_ID
        def extension = project.extensions.webResource as WebResourceExtension
        extension.bower.dependencies {
            install name: "jquery", version: "1.11.2", filter: ["dist/*.min.*"]
        }
        // The old registry bower.herokuapp.com is deprecated
        project.file(".bowerrc").text = "{ \"registry\": \"https://registry.bower.io\" }"
        project.evaluate()
        project.tasks.webResourceInstallBowerDependencies.execute()

        when:
        project.tasks.webResourceCopyBowerDependencies.execute()

        then:
        notThrown(Exception)
        new File("${root}/build/webResource/outputs/lib/jquery").exists()
        !externalDependency.exists()
    }

    def noFilter() {
        setup:
        File root = temporaryFolder.root
        Project project = ProjectBuilder.builder().withProjectDir(root).build()
        setupProject(root, project)
        project.apply plugin: PLUGIN_ID
        def extension = project.extensions.webResource as WebResourceExtension
        extension.bower.dependencies {
            install name: "jquery", version: "1.11.2"
        }
        // The old registry bower.herokuapp.com is deprecated
        project.file(".bowerrc").text = "{ \"registry\": \"https://registry.bower.io\" }"
        project.evaluate()
        project.tasks.webResourceInstallBowerDependencies.execute()

        when:
        project.tasks.webResourceCopyBowerDependencies.execute()

        then:
        notThrown(Exception)
        new File("${root}/build/webResource/outputs/lib/jquery/src/jquery.js").exists()
    }

    def doNotCleanOnUpdate() {
        setup:
        File root = temporaryFolder.root
        Project project = ProjectBuilder.builder().withProjectDir(root).build()
        setupProject(root, project)
        File externalDependency = new File("${root}/build/webResource/outputs/lib/foo")
        externalDependency.mkdirs()
        project.apply plugin: PLUGIN_ID
        def extension = project.extensions.webResource as WebResourceExtension
        extension.lib.cleanOnUpdate = false
        extension.bower.dependencies {
            install name: "jquery", version: "1.11.2", filter: ["dist/*.min.*"]
        }
        // The old registry bower.herokuapp.com is deprecated
        project.file(".bowerrc").text = "{ \"registry\": \"https://registry.bower.io\" }"
        project.evaluate()
        project.tasks.webResourceInstallBowerDependencies.execute()

        when:
        project.tasks.webResourceCopyBowerDependencies.execute()

        then:
        notThrown(Exception)
        new File("${root}/build/webResource/outputs/lib/jquery").exists()
        externalDependency.exists()
    }

    def excludeFromClean() {
        setup:
        File root = temporaryFolder.root
        Project project = ProjectBuilder.builder().withProjectDir(root).build()
        setupProject(root, project)
        // Will be kept
        File externalDependency1 = new File("${root}/build/webResource/outputs/lib/foo")
        externalDependency1.mkdirs()
        // Will be removed on update
        File externalDependency2 = new File("${root}/build/webResource/outputs/lib/bar")
        externalDependency2.mkdirs()
        project.apply plugin: PLUGIN_ID
        def extension = project.extensions.webResource as WebResourceExtension
        extension.lib.excludeFromClean = ["foo"]
        extension.bower.dependencies {
            install name: "jquery", version: "1.11.2", filter: ["dist/*.min.*"]
        }
        // The old registry bower.herokuapp.com is deprecated
        project.file(".bowerrc").text = "{ \"registry\": \"https://registry.bower.io\" }"
        project.evaluate()
        project.tasks.webResourceInstallBowerDependencies.execute()

        when:
        project.tasks.webResourceCopyBowerDependencies.execute()

        then:
        notThrown(Exception)
        new File("${root}/build/webResource/outputs/lib/jquery").exists()
        externalDependency1.exists()
        !externalDependency2.exists()
    }

    def copyAllTransitiveDependencies() {
        setup:
        File root = temporaryFolder.root
        Project project = ProjectBuilder.builder().withProjectDir(root).build()
        setupProject(root, project)
        project.apply plugin: PLUGIN_ID
        def extension = project.extensions.webResource as WebResourceExtension
        extension.bower.logLevel = LogLevel.INFO
        extension.bower.copyAll = true
        extension.bower.dependencies {
            install name: "bootstrap", version: "3.3.4", filter: ["dist/*.min.*"]
        }
        // The old registry bower.herokuapp.com is deprecated
        project.file(".bowerrc").text = "{ \"registry\": \"https://registry.bower.io\" }"
        project.evaluate()
        project.tasks.webResourceInstallBowerDependencies.execute()

        when:
        project.tasks.webResourceCopyBowerDependencies.execute()

        then:
        notThrown(Exception)
        new File("${root}/build/webResource/outputs/lib/bootstrap").exists()
        new File("${root}/build/webResource/outputs/lib/jquery").exists()
    }

    void setupProject(File root, Project project) {
        File outputDir = new File(root, "build/webResource/outputs")
        project.delete(outputDir)

        outputDir = new File(root, "build/webResource/bower_components")
        project.delete(outputDir)
    }
}
