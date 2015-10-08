package com.github.ksoichiro.web.resource

import groovyx.gpars.GParsPool
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

class TriremeCoffeeScriptTask extends DefaultTask {
    static final String NAME = "triremeCoffeeScript"
    static final int NUM_OF_THREADS = 8
    WebResourceExtension extension
    PathResolver pathResolver

    TriremeCoffeeScriptTask() {
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
        new File(extension.workDir, 'coffee.js').text = getClass().getResourceAsStream('/coffee.js').text
        def srcRootDir = pathResolver.resolveSrcPathFromProject(extension.coffeeScript.src)
        def srcRootFile = project.file(srcRootDir)
        def src = project.fileTree(dir: srcRootDir)
        src.include '**/*.coffee'
        src.exclude '**/_*.coffee'
        GParsPool.withPool(NUM_OF_THREADS) {
            src.asConcurrent {
                src.each { File file ->
                    def triremeNodeRunner = new TriremeNodeRunner(
                        scriptName: 'coffee.js',
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
                        ] as String[])
                    triremeNodeRunner.exec()
                }
            }
        }
    }
}
