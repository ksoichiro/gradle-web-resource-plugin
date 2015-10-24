package com.github.ksoichiro.web.resource.task

import org.gradle.api.DefaultTask

/**
 * Main task of this plugin.
 * This will trigger other tasks such as compiling CoffeeScript, compiling LESS, etc.
 *
 * @author Soichiro Kashima
 */
class WebResourceCompileTask extends DefaultTask {
    static String NAME = "webResourceCompile"

    WebResourceCompileTask() {
        dependsOn([WebResourceCompileCoffeeScriptTask.NAME, WebResourceCompileLessTask.NAME, WebResourceCopyBowerDependenciesTask.NAME])
    }
}
