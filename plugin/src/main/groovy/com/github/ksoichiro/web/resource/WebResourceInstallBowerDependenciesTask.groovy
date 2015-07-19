package com.github.ksoichiro.web.resource

import com.moowork.gradle.node.task.NodeTask
import groovy.json.JsonOutput

class WebResourceInstallBowerDependenciesTask extends NodeTask {
    static final String NAME = "webResourceInstallBowerDependencies"
    WebResourceExtension extension

    WebResourceInstallBowerDependenciesTask() {
        dependsOn([WebResourceInstallDependenciesTask.NAME])
        project.afterEvaluate {
            extension = project.webResource
            getInputs()
                    .property('bower', extension.bower)
                    .property('version', WebResourceExtension.VERSION)
            getOutputs().files(new File(extension.workDir, 'bower_components'), getBowerJson())
            setWorkingDir(extension.workDir)
        }
    }

    @Override
    void exec() {
        extension = project.webResource
        if (!extension.bower || !extension.bower.containsKey('dependencies')) {
            logger.info "No bower config"
            return
        }
        def bower = project.file(new File(extension.workDir, "node_modules/bower/bin/bower"))
        setScript(bower)
        setWorkingDir(extension.workDir)

        def bowerConfig = extension.bower.clone() as Map
        if (!bowerConfig.containsKey('name')) {
            bowerConfig['name'] = 'webResource'
        }
        getBowerJson().text = JsonOutput.prettyPrint(JsonOutput.toJson(bowerConfig))
        setArgs(['install', '--config.interactive=false'])

        super.exec()
    }

    File getBowerJson() {
        new File(extension.workDir, 'bower.json')
    }
}
