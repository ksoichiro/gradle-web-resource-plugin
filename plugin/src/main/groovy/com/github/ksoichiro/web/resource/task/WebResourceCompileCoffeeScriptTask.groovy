package com.github.ksoichiro.web.resource.task

import com.github.ksoichiro.web.resource.node.TriremeNodeRunner
import com.github.ksoichiro.web.resource.util.PathResolver
import com.github.ksoichiro.web.resource.extension.WebResourceExtension
import groovyx.gpars.GParsPool
import org.gradle.api.tasks.TaskAction

/**
 * Gradle task to compile CoffeeScript files.<br>
 * This task uses Trireme and Rhino to run Node modules.
 *
 * @author Soichiro Kashima
 */
class WebResourceCompileCoffeeScriptTask extends TriremeBaseTask {
    static final String NAME = "webResourceCompileCoffeeScript"
    static final int NUM_OF_THREADS = 8
    static final String SCRIPT_NAME = "coffee.js"

    WebResourceCompileCoffeeScriptTask() {
        dependsOn([WebResourceInstallBowerDependenciesTask.NAME])
        project.afterEvaluate {
            extension = project.extensions.webResource
            pathResolver = new PathResolver(project, extension)
            getInputs()
                .files(pathResolver.retrieveValidPaths(pathResolver.getSrcCoffee()))
                .property('coffeeScript.minify', extension.coffeeScript?.minify)
                .property('version', WebResourceExtension.VERSION)
            getOutputs().files(pathResolver.retrieveValidPaths(pathResolver.getDestCoffee()))
        }
    }

    @TaskAction
    void exec() {
        if (!extension.coffeeScript.enabled) {
            return
        }
        new File(extension.workDir, SCRIPT_NAME).text = getClass().getResourceAsStream("/${SCRIPT_NAME}").text
        writeCommonScript()
        def srcRootDir = pathResolver.resolveSrcPathFromProject(extension.coffeeScript.src)
        def srcRootFile = project.file(srcRootDir)
        def src = project.fileTree(dir: srcRootDir)
        extension.coffeeScript.include.each { src.include it }
        extension.coffeeScript.exclude.each { src.exclude it }
        GParsPool.withPool(NUM_OF_THREADS) {
            src.asConcurrent {
                src.each { File file ->
                    def triremeNodeRunner = new TriremeNodeRunner(
                        scriptName: SCRIPT_NAME,
                        workingDir: extension.workDir,
                        args: [
                            // coffeeSrcPath
                            file.absolutePath,
                            // coffeeSrcName
                            file.name,
                            // coffeeDestDir
                            pathResolver.getDestCoffee()
                                + '/'
                                + file.parent.replace(srcRootFile.absolutePath, "")
                                .replaceAll("\\\\", "/")
                                .replaceAll("^/", "")
                                .replaceAll("/\$", ""),
                            extension.coffeeScript.minify,
                            mapLogLevel(extension.coffeeScript.logLevel),
                        ] as String[])
                    triremeNodeRunner.exec()
                }
            }
        }
    }
}
