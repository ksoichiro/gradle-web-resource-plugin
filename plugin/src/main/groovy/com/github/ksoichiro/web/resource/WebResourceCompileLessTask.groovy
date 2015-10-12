package com.github.ksoichiro.web.resource

import groovyx.gpars.GParsPool
import org.gradle.api.tasks.TaskAction

class WebResourceCompileLessTask extends TriremeBaseTask {
    static final String NAME = "webResourceCompileLess"
    static final int NUM_OF_THREADS = 8
    static final String SCRIPT_NAME = "less.js"

    WebResourceCompileLessTask() {
        dependsOn([WebResourceInstallBowerDependenciesTask.NAME])
        project.afterEvaluate {
            extension = project.extensions.webResource
            pathResolver = new PathResolver(project, extension)
            getInputs()
                .files(pathResolver.retrieveValidPaths(pathResolver.getSrcLess()))
                .property('less.minify', extension.less?.minify)
                .property('version', WebResourceExtension.VERSION)
            getOutputs().files(pathResolver.retrieveValidPaths(pathResolver.getDestLess()))
        }
    }

    @TaskAction
    void exec() {
        if (!extension.less.enabled) {
            return
        }
        new File(extension.workDir, SCRIPT_NAME).text = getClass().getResourceAsStream("/${SCRIPT_NAME}").text
        writeCommonScript()
        def srcRootDir = pathResolver.resolveSrcPathFromProject(extension.less.src)
        def srcRootFile = project.file(srcRootDir)
        def src = project.fileTree(dir: srcRootDir)
        extension.less.include.each { src.include it }
        extension.less.exclude.each { src.exclude it }
        GParsPool.withPool(NUM_OF_THREADS) {
            src.asConcurrent {
                src.each { File file ->
                    def triremeNodeRunner = new TriremeNodeRunner(
                        scriptName: SCRIPT_NAME,
                        workingDir: extension.workDir,
                        args: [
                            // lessSrcPath
                            file.absolutePath,
                            // lessSrcName
                            file.name,
                            // lessDestDir
                            pathResolver.getDestLess()
                                + '/'
                                + file.parent.replace(srcRootFile.absolutePath, "")
                                .replaceAll("\\\\", "/")
                                .replaceAll("^/", "")
                                .replaceAll("/\$", ""),
                            extension.less.minify,
                            mapLogLevel(extension.less.logLevel),
                        ] as String[])
                    triremeNodeRunner.exec()
                }
            }
        }
    }
}
