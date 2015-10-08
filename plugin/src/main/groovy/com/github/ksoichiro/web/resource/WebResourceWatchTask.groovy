package com.github.ksoichiro.web.resource

class WebResourceWatchTask extends WebResourceCompileBaseTask {
    static final String NAME = "webResourceWatch"
    WebResourceWatchTask() {
        dependsOn([WebResourceCompileTask.NAME])
        gulpCommand = 'watch'
        project.afterEvaluate {
            extension = project.extensions.webResource
            pathResolver = new PathResolver(project, extension)
            getInputs().files(pathResolver.retrieveValidPaths(pathResolver.getSrcCoffee(), pathResolver.getSrcLess()))
            getOutputs().files(pathResolver.retrieveValidPaths(pathResolver.getDestCoffee(), pathResolver.getDestLess()))
        }
    }
}
