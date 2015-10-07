package com.github.ksoichiro.web.resource

import groovyx.gpars.GParsPool
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

class TriremeCoffeeScriptTask extends DefaultTask {
    static final String NAME = "triremeCoffeeScript"
    static final int NUM_OF_THREADS = 8
    WebResourceExtension extension

    TriremeCoffeeScriptTask() {
        project.afterEvaluate {
            extension = project.extensions.webResource
            getInputs()
                .files(retrieveValidPaths(getSrcCoffee()))
                .property('coffeeScript.minify', extension.coffeeScript?.minify)
                .property('version', WebResourceExtension.VERSION)
            getOutputs().files(retrieveValidPaths(getDestCoffee()))
        }
    }

    @TaskAction
    void exec() {
        if (!extension.coffeeScript.enabled) {
            return
        }
        new File(extension.workDir, 'coffee.js').text = getClass().getResourceAsStream('/coffee.js').text
        def srcRootDir = 'src/main/coffee'
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
                            'outputs/js/'
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

    String resolveSrcPath(def path) {
        String src = "../../"
        if (path) {
            src += extension.base?.src ? "${extension.base?.src}/" : ""
            src += path
        }
        src
    }

    String resolveDestPath(def path) {
        String dest = ""
        if (path) {
            dest = extension.base?.dest ? "${extension.base?.dest}/" : ""
            dest += path
            dest = "../../${dest}"
        }
        dest
    }

    String getSrcCoffee() {
        resolveSrcPath(extension.coffeeScript?.src)
    }

    String getDestCoffee() {
        resolveDestPath(extension.coffeeScript?.dest)
    }

    List retrieveValidPaths(String... paths) {
        List result = []
        paths.findAll { project.file("${extension.workDir}/${it}") }.each {
            result += "${extension.workDir}/${it}"
        }
        result
    }
}
