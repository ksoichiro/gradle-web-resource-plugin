package com.github.ksoichiro.web.resource

import com.moowork.gradle.node.task.NodeTask
import groovy.json.JsonOutput

class WebResourceInstallBowerDependenciesTask extends NodeTask {
    static final String NAME = "webResourceInstallBowerDependencies"

    WebResourceInstallBowerDependenciesTask() {
        dependsOn([WebResourceInstallDependenciesTask.NAME])
        this.project.afterEvaluate {
            WebResourceExtension extension = this.project.webResource
            getInputs().property('bower', extension.bower)
            getOutputs().dir(new File(extension.workDir, 'bower_components'))
            setWorkingDir(extension.workDir)
        }
    }

    @Override
    void exec() {
        def extension = project.webResource as WebResourceExtension
        if (!extension.bower || !extension.bower.containsKey('dependencies')) {
            println "No bower config"
            return
        }
        def bower = this.project.file(new File(extension.workDir, "node_modules/bower/bin/bower"))
        setScript(bower)
        setWorkingDir(extension.workDir)

        def bowerConfig = extension.bower.clone() as Map
        if (!bowerConfig.containsKey('name')) {
            bowerConfig['name'] = 'webResource'
        }
        new File(extension.workDir, 'bower.json').text = JsonOutput.prettyPrint(JsonOutput.toJson(bowerConfig))
        setArgs(['install', '--config.interactive=false'])

        super.exec()
    }
}
