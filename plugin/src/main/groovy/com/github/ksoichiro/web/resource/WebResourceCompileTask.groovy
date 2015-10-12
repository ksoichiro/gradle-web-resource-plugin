package com.github.ksoichiro.web.resource

import org.gradle.api.DefaultTask

class WebResourceCompileTask extends DefaultTask {
    static String NAME = "webResourceCompile"

    WebResourceCompileTask() {
        dependsOn([WebResourceCompileCoffeeScriptTask.NAME, WebResourceCompileLessTask.NAME, WebResourceCopyBowerDependenciesTask.NAME])
    }
}
