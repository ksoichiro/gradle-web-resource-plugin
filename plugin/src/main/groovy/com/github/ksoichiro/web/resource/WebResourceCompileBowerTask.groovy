package com.github.ksoichiro.web.resource

class WebResourceCompileBowerTask extends WebResourceCompileBaseTask {
    static String NAME = "webResourceCompileBower"

    WebResourceCompileBowerTask() {
        dependsOn([WebResourceInstallBowerDependenciesTask.NAME])
        gulpCommand = 'bower-files'
        this.project.afterEvaluate {
            extension = this.project.extensions.webResource
            getInputs().property('bower', extension.bower)
            getOutputs().files(retrieveValidPaths(getDestLib()), getGulpfile())
        }
    }
}
