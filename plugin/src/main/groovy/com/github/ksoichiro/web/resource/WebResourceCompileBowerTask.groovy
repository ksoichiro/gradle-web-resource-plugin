package com.github.ksoichiro.web.resource

class WebResourceCompileBowerTask extends WebResourceCompileBaseTask {
    static String NAME = "webResourceCompileBower"

    WebResourceCompileBowerTask() {
        dependsOn([WebResourceInstallBowerDependenciesTask.NAME])
        gulpCommand = 'bower-files'
        project.afterEvaluate {
            extension = project.extensions.webResource
            gulpEnabled = extension.bower && !extension.bower.isEmpty()
            getInputs()
                    .property('bower', extension.bower)
                    .property('version', WebResourceExtension.VERSION)
            getOutputs().files(retrieveValidPaths(getDestLib()))
        }
    }
}
