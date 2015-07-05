package com.github.ksoichiro.web.resource

import com.moowork.gradle.node.task.NpmSetupTask
import com.moowork.gradle.node.task.NpmTask
import groovy.json.JsonOutput

class WebResourceInstallDependenciesTask extends NpmTask {
    static final String NAME = 'webResourceInstallDependencies'

    WebResourceInstallDependenciesTask() {
        setNpmCommand('install')
        dependsOn([NpmSetupTask.NAME])

        this.project.afterEvaluate {
            def extension = this.project.webResource as WebResourceExtension
            getInputs().property('npm', extension.npm)
            getOutputs().dir(new File(extension.workDir, 'node_modules'))
            setWorkingDir(extension.workDir)
        }
    }

    @Override
    void exec() {
        def extension = this.project.webResource as WebResourceExtension
        def workDir = new File(extension.workDir.toURI())
        if (!workDir.exists()) {
            workDir.mkdirs()
        }
        def npmConfig = extension.npm ? extension.npm.clone() as Map : [:]
        if (!npmConfig.containsKey('devDependencies')) {
            npmConfig['devDependencies'] = [:] as Map
        }
        // Suppress warning: npm WARN package.json @ No description
        if (!npmConfig.containsKey('description')) {
            npmConfig['description'] = "Auto-configured dependencies with gradle-web-resource plugin"
        }
        // Suppress warning: npm WARN package.json @ No repository field.
        if (!npmConfig.containsKey('repository')) {
            npmConfig['repository'] = [type: "git", url: "https://github.com/ksoichiro/gradle-web-resource-plugin"]
        }

        [
                "bower"           : "1.3.12",
                "gulp"            : "3.8.11",
                "coffee-script"   : "1.9.1",
                "main-bower-files": "2.7.0",
                "gulp-less"       : "3.0.2",
                "gulp-minify-css" : "1.0.0",
                "gulp-coffee"     : "2.3.1",
                "gulp-filter"     : "2.0.2",
                "gulp-uglify"     : "0.2.1",
                "fs-extra"        : "0.21.0"
        ].each { name, version ->
            if (!npmConfig.devDependencies.containsKey(name)) {
                npmConfig.devDependencies[name] = version
            }
        }

        new File(extension.workDir, 'package.json').text = JsonOutput.prettyPrint(JsonOutput.toJson(npmConfig))
        // Suppress warning: npm WARN package.json @ No README data
        new File(extension.workDir, 'README.md').text = "WebResource npm packages"
        super.exec()
    }
}
