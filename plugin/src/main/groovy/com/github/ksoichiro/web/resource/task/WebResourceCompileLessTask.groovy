package com.github.ksoichiro.web.resource.task

import com.github.ksoichiro.web.resource.node.TriremeNodeRunner
import com.github.ksoichiro.web.resource.util.PathResolver
import com.github.ksoichiro.web.resource.extension.WebResourceExtension
import groovyx.gpars.GParsPool
import org.gradle.api.file.FileTree
import org.gradle.api.tasks.TaskAction

/**
 * Gradle task to compile LESS files.<br>
 * This task uses Trireme and Rhino to run Node modules.
 *
 * @author Soichiro Kashima
 */
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
        writeLessScript()
        writeCommonScript()
        def srcRootDir = pathResolver.resolveSrcPathFromProject(extension.less.src)
        def srcRootFile = project.file(srcRootDir)
        def src = filterSource(srcRootDir)
        GParsPool.withPool(NUM_OF_THREADS) {
            src.asConcurrent {
                src.each { File file ->
                    compile(file, srcRootFile.absolutePath)
                }
            }
        }
    }

    void writeLessScript() {
        new File(extension.workDir, SCRIPT_NAME).text = getClass().getResourceAsStream("/${SCRIPT_NAME}").text
    }

    FileTree filterSource(def srcRootDir) {
        def src = project.fileTree(dir: srcRootDir)
        extension.less.include.each { src.include it }
        extension.less.exclude.each { src.exclude it }
        if (extension.less.filters?.size()) {
            extension.less.filters.each {
                if (it.include) {
                    def additionalTree = project.fileTree(dir: srcRootDir)
                    additionalTree.include it.include
                    src = src.plus(additionalTree)
                } else if (it.exclude) {
                    src.exclude it.exclude
                }
            }
        }
        src
    }

    void compile(File file, String srcRootPath) {
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
                    + file.parent.replace(srcRootPath, "")
                    .replaceAll("\\\\", "/")
                    .replaceAll("^/", "")
                    .replaceAll("/\$", ""),
                extension.less.minify,
                mapLogLevel(extension.less.logLevel),
            ] as String[])
        triremeNodeRunner.exec()
    }
}
