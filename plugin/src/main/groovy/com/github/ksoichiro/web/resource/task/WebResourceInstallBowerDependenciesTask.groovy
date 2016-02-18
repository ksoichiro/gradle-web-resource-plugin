package com.github.ksoichiro.web.resource.task

import com.github.ksoichiro.web.resource.extension.BowerDependencyResolution
import com.github.ksoichiro.web.resource.extension.WebResourceExtension
import com.github.ksoichiro.web.resource.node.TriremeNodeRunner
import com.github.ksoichiro.web.resource.util.PathResolver
import groovy.json.JsonOutput
import groovy.json.JsonSlurper
import org.gradle.api.tasks.TaskAction

class WebResourceInstallBowerDependenciesTask extends TriremeBaseTask {
    static final String NAME = "webResourceInstallBowerDependencies"
    static final String BOWER_COMPONENTS_DIR = "bower_components"

    WebResourceInstallBowerDependenciesTask() {
        project.afterEvaluate {
            extension = project.extensions.webResource
            pathResolver = new PathResolver(project, extension)
            getInputs()
                .files(pathResolver.retrieveValidSrcLessPaths())
                .property('bower', extension.bower.toString())
                .property('version', WebResourceExtension.VERSION)
            getOutputs().files(new File(extension.workDir, BOWER_COMPONENTS_DIR), getBowerScript())
        }
    }

    @TaskAction
    void exec() {
        if (extension.bower?.dependencies?.isEmpty()) {
            return
        }
        prepareWorkDir()

        File bowerComponentsDir = new File(extension.workDir, BOWER_COMPONENTS_DIR)
        if (!bowerComponentsDir.exists()) {
            bowerComponentsDir.mkdirs()
        }

        // Ensure bower.json does not exist since it affects bower's installation.
        def rootBowerJson = new File(extension.workDir, 'bower.json')
        if (rootBowerJson.exists()) {
            project.delete(rootBowerJson)
        }

        def packages = [:]
        List dependencies = []
        extension.bower.dependencies.each {
            File bowerJson = new File(extension.workDir, "${BOWER_COMPONENTS_DIR}/${it.name}/bower.json")
            if (bowerJson.exists()) {
                // already installed
                def pkg = new JsonSlurper().parseText(bowerJson.text)
                if (pkg.version) {
                    if (pkg.version != it.version) {
                        // should be updated, so remove it before install
                        project.delete("${extension.workDir}/${BOWER_COMPONENTS_DIR}/${it.name}")
                    }
                }
            }
            Map dependency = [name: it.name, version: it.version]
            if (it.cacheName) {
                dependency['cacheName'] = it.cacheName
            }
            dependencies.add(dependency)
        }
        packages.dependencies = dependencies

        def resolutions = [:]
        if (extension.bower.dependencyResolutions.size() > 0) {
            extension.bower.dependencyResolutions.each { BowerDependencyResolution resolution ->
                resolutions[resolution.name] = resolution.version
            }
        }
        packages.resolutions = resolutions
        packages.options = extension.bower.options.collectEntries { [(it.replaceAll(/^--/, "")): true] }
        packages.configs = extension.bower.configs.collectEntries { [(it.replaceAll(/^--/, "")): true] }

        def tmpFile = project.file("${extension.workDir}/.bowerpkg.json")
        tmpFile.text = JsonOutput.toJson(packages)

        writeBowerScript()

        def triremeNodeRunner = new TriremeNodeRunner(
            scriptName: SCRIPT_NAME,
            workingDir: extension.workDir,
            args: [
                'bower',
                tmpFile.absolutePath,
                extension.bower.parallelize,
                mapLogLevel(extension.bower.logLevel),
            ] as String[])
        triremeNodeRunner.exec()
    }

    void writeBowerScript() {
        new File(extension.workDir, SCRIPT_NAME).text = getClass().getResourceAsStream("/${SCRIPT_NAME}").text
    }

    File getBowerScript() {
        new File(extension.workDir, SCRIPT_NAME)
    }
}
