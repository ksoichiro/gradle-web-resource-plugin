package com.github.ksoichiro.web.resource

import com.github.ksoichiro.web.resource.extension.WebResourceExtension
import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import org.junit.Rule
import org.junit.rules.TemporaryFolder

class WebResourceCompileLessTaskSpec extends BaseSpec {
    @Rule
    TemporaryFolder temporaryFolder

    def exec() {
        setup:
        File root = temporaryFolder.root
        Project project = ProjectBuilder.builder().withProjectDir(root).build()
        File srcDir = new File(root, "src/main/less")
        srcDir.mkdirs()
        new File(srcDir, "app.less").text = """\
            |.foo {
            |  .bar {
            |    color: #f00;
            |  }
            |}
            |""".stripMargin().stripIndent()
        project.apply plugin: PLUGIN_ID
        project.evaluate()
        project.tasks.webResourceSetupNodeDependencies.execute()
        project.tasks.webResourceInstallBowerDependencies.execute()

        when:
        project.tasks.webResourceCompileLess.execute()

        then:
        notThrown(Exception)
        new File("${root}/build/webResource/outputs/css/app.css").exists()
    }

    def disabled() {
        setup:
        File root = temporaryFolder.root
        Project project = ProjectBuilder.builder().withProjectDir(root).build()
        File srcDir = new File(root, "src/main/less")
        srcDir.mkdirs()
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
        project.tasks.webResourceSetupNodeDependencies.execute()
        project.tasks.webResourceInstallBowerDependencies.execute()

        when:
        project.tasks.webResourceCompileLess.execute()

        then:
        notThrown(Exception)
        !new File("${root}/build/webResource/outputs/css/app.css").exists()
    }

    def filters() {
        setup:
        File root = temporaryFolder.root
        Project project = ProjectBuilder.builder().withProjectDir(root).build()
        File srcDir = new File(root, "src/main/less")
        srcDir.mkdirs()
        ["a", "b", "c"].each {
            new File(srcDir, "${it}.less").text = """\
                |.${it} {
                |  color: #f00;
                |}
                |""".stripMargin().stripIndent()
        }
        project.apply plugin: PLUGIN_ID
        def extension = project.extensions.webResource as WebResourceExtension
        extension.less.filters {
            exclude '**/*.less'
            include '**/b.less'
        }
        project.evaluate()
        project.tasks.webResourceSetupNodeDependencies.execute()
        project.tasks.webResourceInstallBowerDependencies.execute()

        when:
        project.tasks.webResourceCompileLess.execute()

        then:
        notThrown(Exception)
        !new File("${root}/build/webResource/outputs/css/a.css").exists()
        new File("${root}/build/webResource/outputs/css/b.css").exists()
        !new File("${root}/build/webResource/outputs/css/c.css").exists()
    }
}
