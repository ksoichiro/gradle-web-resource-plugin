package com.github.ksoichiro.web.resource

import com.moowork.gradle.node.task.NodeTask
import groovy.json.JsonOutput

class BowerInstallTask extends NodeTask {
    static final String NAME = "bowerInstall"
    private WebResourceExtension extension

    BowerInstallTask() {
        dependsOn([NpmInstallWrapperTask.NAME])
        this.project.afterEvaluate {
            WebResourceExtension extension = this.project.webResource
            getInputs().property('bower', extension.bower)
            getOutputs().dir(new File(extension.workDir, 'bower_components'))
            setWorkingDir(extension.workDir)
        }
    }

    @Override
    void exec() {
        extension = project.webResource
        if (!extension.bower) {
            println "No bower config"
            return
        }
        def bower = this.project.file(new File(extension.workDir, "node_modules/bower/bin/bower"))
        setScript(bower)
        setWorkingDir(extension.workDir)

        def bowerConfig = extension.bower.clone() as Map
        if (!bowerConfig.containsKey('name')) {
            println "Added name property to bower"
            bowerConfig['name'] = 'webResource'
        }
        new File(extension.workDir, 'bower.json').text = JsonOutput.prettyPrint(JsonOutput.toJson(bowerConfig))
        setArgs(['install', '--config.interactive=false'])

        super.exec()
    }
}
