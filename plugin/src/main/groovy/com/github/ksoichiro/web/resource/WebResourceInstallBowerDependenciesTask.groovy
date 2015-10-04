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
        if (!extension.bower) {
            return
        }
        setScript(getBowerScript())
        setWorkingDir(extension.workDir)

        List bowerConfig = []
        extension.bower.dependencies.each {
            Map dependency = [name: it.name, version: it.version]
            if (it.cacheName) {
                dependency['cacheName'] = it.cacheName
            }
            bowerConfig.add(dependency)
        }
        def dependencies = bowerConfig.isEmpty() ? '[]'
            : JsonOutput.prettyPrint(JsonOutput.toJson(bowerConfig))
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
