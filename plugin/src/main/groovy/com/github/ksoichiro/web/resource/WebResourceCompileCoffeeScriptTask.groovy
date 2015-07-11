package com.github.ksoichiro.web.resource

class WebResourceCompileCoffeeScriptTask extends WebResourceCompileBaseTask {
    static String NAME = "webResourceCompileCoffeeScript"

    WebResourceCompileCoffeeScriptTask() {
        dependsOn([WebResourceInstallBowerDependenciesTask.NAME])
        gulpCommand = 'coffee'
        project.afterEvaluate {
            extension = project.extensions.webResource
            getInputs()
                    .files(retrieveValidPaths(getSrcCoffee()))
                    .property('coffeeScript.minify', extension.coffeeScript?.minify)
                    .property('version', WebResourceExtension.VERSION)
            getOutputs().files(retrieveValidPaths(getDestCoffee(), getDestLib()), getGulpfile())
        }
    }
}
