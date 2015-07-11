package com.github.ksoichiro.web.resource

class WebResourceWatchTask extends WebResourceCompileBaseTask {
    static final String NAME = "webResourceWatch"
    WebResourceWatchTask() {
        dependsOn([WebResourceCompileTask.NAME])
        gulpCommand = 'watch'
        this.project.afterEvaluate {
            extension = this.project.extensions.webResource
            getInputs().files(retrieveValidPaths(getSrcCoffee(), getSrcLess()))
            getOutputs().files(retrieveValidPaths(getDestCoffee(), getDestLess(), getDestLib()), getGulpfile())
        }
    }
}
