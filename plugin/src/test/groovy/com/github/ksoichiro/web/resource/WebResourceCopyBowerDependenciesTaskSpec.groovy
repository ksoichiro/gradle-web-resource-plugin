package com.github.ksoichiro.web.resource

import com.github.ksoichiro.web.resource.extension.WebResourceExtension
import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import org.junit.Rule
import org.junit.rules.TemporaryFolder

class WebResourceCopyBowerDependenciesTaskSpec extends BaseSpec {
    @Rule
    TemporaryFolder temporaryFolder

    def exec() {
        setup:
        File root = temporaryFolder.root
        File externalDependency = new File("${root}/build/webResource/outputs/lib/foo")
        externalDependency.mkdirs()
        Project project = ProjectBuilder.builder().withProjectDir(root).build()
        project.apply plugin: PLUGIN_ID
        def extension = project.extensions.webResource as WebResourceExtension
        extension.bower.dependencies {
            install name: "jquery", version: "1.11.2", filter: ["dist/*.min.*"]
        }
        project.evaluate()
        project.tasks.webResourceSetupNodeDependencies.execute()
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
        project.apply plugin: PLUGIN_ID
        def extension = project.extensions.webResource as WebResourceExtension
        extension.bower.dependencies {
            install name: "jquery", version: "1.11.2"
        }
        project.evaluate()
        project.tasks.webResourceSetupNodeDependencies.execute()
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
        File externalDependency = new File("${root}/build/webResource/outputs/lib/foo")
        externalDependency.mkdirs()
        Project project = ProjectBuilder.builder().withProjectDir(root).build()
        project.apply plugin: PLUGIN_ID
        def extension = project.extensions.webResource as WebResourceExtension
        extension.lib.cleanOnUpdate = false
        extension.bower.dependencies {
            install name: "jquery", version: "1.11.2", filter: ["dist/*.min.*"]
        }
        project.evaluate()
        project.tasks.webResourceSetupNodeDependencies.execute()
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
        // Will be kept
        File externalDependency1 = new File("${root}/build/webResource/outputs/lib/foo")
        externalDependency1.mkdirs()
        // Will be removed on update
        File externalDependency2 = new File("${root}/build/webResource/outputs/lib/bar")
        externalDependency2.mkdirs()
        Project project = ProjectBuilder.builder().withProjectDir(root).build()
        project.apply plugin: PLUGIN_ID
        def extension = project.extensions.webResource as WebResourceExtension
        extension.lib.excludeFromClean = ["foo"]
        extension.bower.dependencies {
            install name: "jquery", version: "1.11.2", filter: ["dist/*.min.*"]
        }
        project.evaluate()
        project.tasks.webResourceSetupNodeDependencies.execute()
        project.tasks.webResourceInstallBowerDependencies.execute()

        when:
        project.tasks.webResourceCopyBowerDependencies.execute()

        then:
        notThrown(Exception)
        new File("${root}/build/webResource/outputs/lib/jquery").exists()
        externalDependency1.exists()
        !externalDependency2.exists()
    }
}
