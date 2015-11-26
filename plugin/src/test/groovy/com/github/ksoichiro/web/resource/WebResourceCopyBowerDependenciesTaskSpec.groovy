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
}
