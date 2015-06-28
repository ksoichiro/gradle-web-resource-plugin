package com.github.ksoichiro.web.resource

import com.moowork.gradle.node.task.NpmTask
import groovy.json.JsonOutput

class NpmInstallWrapperTask extends NpmTask {
    static final String NAME = 'npmInstallWrapper'

    NpmInstallWrapperTask() {
        setNpmCommand('install')
        dependsOn([InstallBowerTask.NAME])

        this.project.afterEvaluate {
            def extension = this.project.webResource as WebResourceExtension
            getInputs().property('npm', extension.npm)
            getOutputs().dir(new File(extension.workDir, 'node_modules'))
            setWorkingDir(extension.workDir)
        }
    }

    @Override
    void exec() {
        def extension = this.project.webResource as WebResourceExtension
        new File(extension.workDir, 'package.json').text = JsonOutput.prettyPrint(JsonOutput.toJson(extension.npm))
        super.exec()
    }
}
