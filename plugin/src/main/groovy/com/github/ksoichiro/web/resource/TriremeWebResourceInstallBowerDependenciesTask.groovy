package com.github.ksoichiro.web.resource

import groovy.json.JsonOutput
import org.gradle.api.tasks.TaskAction

class TriremeWebResourceInstallBowerDependenciesTask extends TriremeNodeTask {
    static final String NAME = "triremeWebResourceInstallBowerDependencies"
    WebResourceExtension extension
    String gulpCommand = 'default'

    TriremeWebResourceInstallBowerDependenciesTask() {
        setScriptName('node_modules/bower/bin/bower')
        this.project.afterEvaluate {
            extension = project.webResource
            setWorkingDir(extension.workDir)
            setArgs(['install', '--config.interactive=false'] as String[])
        }
    }

    @TaskAction
    void exec() {
        def bowerConfig = extension.bower.clone() as Map
        if (!bowerConfig.containsKey('name')) {
            bowerConfig['name'] = 'webResource'
        }
        getBowerJson().text = JsonOutput.prettyPrint(JsonOutput.toJson(bowerConfig))
        File bowerComponentsDir = new File(extension.getWorkDir(), "bower_components")
        if (!bowerComponentsDir.exists()) {
            bowerComponentsDir.mkdirs()
        }
        super.exec()
    }

    File getBowerJson() {
        new File(extension.workDir, 'bower.json')
    }
}
