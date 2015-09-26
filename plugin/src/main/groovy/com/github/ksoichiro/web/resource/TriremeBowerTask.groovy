package com.github.ksoichiro.web.resource

import groovy.json.JsonOutput
import org.gradle.api.tasks.TaskAction

class TriremeBowerTask extends TriremeNodeTask {
    static final String NAME = "triremeWebResourceInstallBowerDependencies"
    WebResourceExtension extension
    String gulpCommand = 'default'

    TriremeBowerTask() {
        setScriptName('node_modules/bower/bin/bower')
        this.project.afterEvaluate {
            extension = project.webResource
            setWorkingDir(extension.workDir)
            setArgs(extension.workDir.absolutePath, 'install', '--config.interactive=false')
        }
    }

    @TaskAction
    void exec() {
        def bowerConfig = extension.bower.clone() as Map
        if (!bowerConfig.containsKey('name')) {
            bowerConfig['name'] = 'webResource'
        }
        getBowerJson().text = JsonOutput.prettyPrint(JsonOutput.toJson(bowerConfig))
        super.exec()
    }

    File getBowerJson() {
        new File(extension.workDir, 'bower.json')
    }
}
