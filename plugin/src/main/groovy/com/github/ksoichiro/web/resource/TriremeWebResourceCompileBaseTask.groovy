package com.github.ksoichiro.web.resource

import org.gradle.api.tasks.TaskAction

class TriremeWebResourceCompileBaseTask extends TriremeNodeTask {
    WebResourceExtension extension
    String gulpCommand = 'default'
    boolean gulpEnabled = true

    TriremeWebResourceCompileBaseTask() {
        dependsOn([TriremeWebResourceInstallBowerDependenciesTask.NAME])
        project.afterEvaluate {
            extension = project.extensions.webResource
            setWorkingDir(extension.workDir)
        }
    }

    @TaskAction
    void exec() {
        setScriptName('node_modules/gulp/bin/gulp.js')
        this.project.afterEvaluate {
            def extension = project.webResource as WebResourceExtension
            setWorkingDir(extension.workDir)
            setArgs(extension.workDir.absolutePath, gulpCommand)
        }
    }
}
