package com.github.ksoichiro.web.resource

class WebResourceCompileCoffeeScriptTask extends WebResourceCompileBaseTask {
    static String NAME = "webResourceCompileCoffeeScript"

    WebResourceCompileCoffeeScriptTask() {
        dependsOn([WebResourceInstallBowerDependenciesTask.NAME])
        gulpCommand = 'coffee'
        this.project.afterEvaluate {
            extension = this.project.extensions.webResource
            getInputs()
                    .files(retrieveValidPaths(getSrcCoffee()))
                    .property('version', WebResourceExtension.VERSION)
            getOutputs().files(retrieveValidPaths(getDestCoffee(), getDestLib()), getGulpfile())
        }
    }
}
