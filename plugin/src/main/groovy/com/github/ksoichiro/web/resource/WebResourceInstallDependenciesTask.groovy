package com.github.ksoichiro.web.resource

import com.moowork.gradle.node.task.SetupTask
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

class WebResourceInstallDependenciesTask extends DefaultTask {
    static final String NAME = 'webResourceInstallDependencies'
    static final String PRE_INSTALLED_NODE_MODULES_DIR = "node_modules"
    WebResourceExtension extension

    WebResourceInstallDependenciesTask() {
        dependsOn([SetupTask.NAME])

        this.project.afterEvaluate {
            extension = project.webResource
            getInputs()
                    .property('less', extension.less && extension.less.enabled)
                    .property('coffeeScript', extension.coffeeScript && extension.coffeeScript.enabled)
                    .property('bower', extension.bower && !extension.bower.isEmpty())
                    .property('version', WebResourceExtension.VERSION)
            getOutputs().files(new File(extension.workDir, 'node_modules'))
        }
    }

    @TaskAction
    void exec() {
        def workDir = extension.workDir
        if (!workDir.exists()) {
            workDir.mkdirs()
        }
        [
            "bower",
            "gulp",
            "gulp-coffee",
            "gulp-filter",
            "gulp-include",
            "gulp-less",
            "gulp-minify-css",
            "gulp-uglify",
            "main-bower-files",
        ].each {
            copyDependencies(it)
        }
    }

    void copyDependencies(String name) {
        URL url = getClass().getResource("/${PRE_INSTALLED_NODE_MODULES_DIR}/${name}")
        String jarPath = url.toString().replaceAll("jar:file:", "").replaceAll("!.*\$", "")
        String installPath = "${extension.workDir}"
        File installDir = new File(installPath)
        if (!installDir.exists()) {
            installDir.mkdirs()
        }
        project.copy {
            from project.zipTree(new File(jarPath)).matching { it.include("${PRE_INSTALLED_NODE_MODULES_DIR}/${name}/**") }
            into installDir
        }
    }
}
