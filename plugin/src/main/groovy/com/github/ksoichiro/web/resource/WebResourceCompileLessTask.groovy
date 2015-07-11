package com.github.ksoichiro.web.resource

class WebResourceCompileLessTask extends WebResourceCompileBaseTask {
    static String NAME = "webResourceCompileLess"

    WebResourceCompileLessTask() {
        dependsOn([WebResourceInstallBowerDependenciesTask.NAME])
        gulpCommand = 'less'
        this.project.afterEvaluate {
            extension = this.project.extensions.webResource
            getInputs().files(retrieveValidPaths(getSrcLess()))
            getOutputs().files(retrieveValidPaths(getDestLess()), getGulpfile())
        }
    }
}
