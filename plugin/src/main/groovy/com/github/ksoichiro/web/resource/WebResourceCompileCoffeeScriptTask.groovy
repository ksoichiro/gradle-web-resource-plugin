package com.github.ksoichiro.web.resource

class WebResourceCompileCoffeeScriptTask extends WebResourceCompileBaseTask {
    static String NAME = "webResourceCompileCoffeeScript"

    WebResourceCompileCoffeeScriptTask() {
        dependsOn([WebResourceInstallBowerDependenciesTask.NAME])
        gulpCommand = 'coffee'
        project.afterEvaluate {
            extension = project.extensions.webResource
            pathResolver = new PathResolver(project, extension)
            gulpEnabled = extension.coffeeScript.enabled
            getInputs()
                    .files(pathResolver.retrieveValidPaths(pathResolver.getSrcCoffee()))
                    .property('coffeeScript.minify', extension.coffeeScript?.minify)
                    .property('version', WebResourceExtension.VERSION)
            getOutputs().files(pathResolver.retrieveValidPaths(pathResolver.getDestCoffee()))
        }
    }
}
