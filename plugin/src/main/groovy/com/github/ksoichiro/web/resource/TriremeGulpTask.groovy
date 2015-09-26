package com.github.ksoichiro.web.resource

import org.gradle.api.tasks.TaskAction

class TriremeGulpTask extends TriremeNodeTask {
    String gulpCommand = 'default'

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
