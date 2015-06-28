package com.github.ksoichiro.web.resource

import com.moowork.gradle.node.NodeExtension
import com.moowork.gradle.node.task.NpmSetupTask
import com.moowork.gradle.node.task.NpmTask
import org.gradle.api.tasks.TaskAction

class InstallBowerTask extends NpmTask {
    static final String NAME = "installBower"
    static final String DEFAULT_BOWER_VERSION = "1.3.12"

    InstallBowerTask() {
        dependsOn([NpmSetupTask.NAME])
        setArgs(["install", "bower@${DEFAULT_BOWER_VERSION}"])
        this.project.afterEvaluate {
            def node = this.project.node as NodeExtension
            setWorkingDir(node.nodeModulesDir)
            getOutputs().dir(new File(node.nodeModulesDir, 'node_modules/bower'))
        }
    }

    @TaskAction
    void exec() {
        def extension = project.webResource as WebResourceExtension
        if ((extension.npm?.get('devDependencies') as Map)?.containsKey('bower')) {
            // Bower will be installed by npmInstall task
            return
        }
        // Continue installing bower with this task
        super.exec()
    }
}
