package com.github.ksoichiro.web.resource

import com.moowork.gradle.node.task.NodeTask
import groovy.json.JsonOutput
import groovy.text.SimpleTemplateEngine

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
            getOutputs().files(new File(extension.workDir, 'bower_components'), getBowerScript())
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
        setScript(getBowerScript())
        setWorkingDir(extension.workDir)

        def bowerConfig = extension.bower.clone() as Map
        def dependencies = bowerConfig.containsKey('dependencies') ?
            JsonOutput.prettyPrint(JsonOutput.toJson(bowerConfig.dependencies)) : '[]'
        def bindings = [
            dependencies: dependencies
        ]
        getBowerScript().text =
            new SimpleTemplateEngine()
                .createTemplate(getClass().getResourceAsStream('/bower.js').text)
                .make(bindings)
        super.exec()
    }

    File getBowerScript() {
        new File(extension.workDir, 'bower.js')
    }
}
