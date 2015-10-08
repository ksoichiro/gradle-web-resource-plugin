package com.github.ksoichiro.web.resource

class WebResourceCompileLessTask extends WebResourceCompileBaseTask {
    static String NAME = "webResourceCompileLess"

    WebResourceCompileLessTask() {
        dependsOn([WebResourceInstallBowerDependenciesTask.NAME])
        gulpCommand = 'less'
        project.afterEvaluate {
            extension = project.extensions.webResource
            pathResolver = new PathResolver(project, extension)
            gulpEnabled = extension.less.enabled
            getInputs()
                    .files(pathResolver.retrieveValidPaths(pathResolver.getSrcLess()))
                    .property('less.minify', extension.less?.minify)
                    .property('version', WebResourceExtension.VERSION)
            getOutputs().files(pathResolver.retrieveValidPaths(pathResolver.getDestLess()))
        }
    }
}
