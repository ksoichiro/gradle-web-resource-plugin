package com.github.ksoichiro.web.resource

import com.moowork.gradle.node.task.NodeTask
import groovy.json.JsonOutput
import groovy.json.JsonSlurper
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

        // Ensure bower.json does not exist since it affects bower's installation.
        def rootBowerJson = new File(extension.workDir, 'bower.json')
        if (rootBowerJson.exists()) {
            project.delete(rootBowerJson)
        }

        List bowerConfig = []
        extension.bower.dependencies.each {
            File bowerJson = new File(extension.workDir, "bower_components/${it.name}/bower.json")
            if (bowerJson.exists()) {
                // already installed
                def pkg = new JsonSlurper().parseText(bowerJson.text)
                if (pkg.version) {
                    if (pkg.version != it.version) {
                        // should be updated, so remove it before install
                        project.delete("${extension.workDir}/bower_components/${it.name}")
                    }
                }
            }
            Map dependency = [name: it.name, version: it.version]
            if (it.cacheName) {
                dependency['cacheName'] = it.cacheName
            }
            bowerConfig.add(dependency)
        }
        def dependencies = bowerConfig.isEmpty() ? '[]'
            : JsonOutput.prettyPrint(JsonOutput.toJson(bowerConfig))
        new File(extension.workDir, 'bowerPackages.json').text = dependencies
        getBowerScript().text = getClass().getResourceAsStream('/bower.js').text
        super.exec()
    }

    File getBowerScript() {
        new File(extension.workDir, 'bower.js')
    }
}
