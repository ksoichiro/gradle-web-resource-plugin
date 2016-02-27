package com.github.ksoichiro.web.resource.task

import com.github.ksoichiro.web.resource.extension.WebResourceExtension
import com.github.ksoichiro.web.resource.node.TriremeNodeRunner
import com.github.ksoichiro.web.resource.util.PathResolver
import groovy.json.JsonOutput
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

    WebResourceCompileLessTask() {
        dependsOn([WebResourceInstallBowerDependenciesTask.NAME])
        project.afterEvaluate {
            extension = project.extensions.webResource
            pathResolver = new PathResolver(project, extension)
            getInputs()
                .files(pathResolver.retrieveValidSrcLessPaths())
                .property('less.minify', extension.less?.minify)
                .property('version', WebResourceExtension.VERSION)
            getOutputs().files(pathResolver.retrieveValidPaths(pathResolver.getDestLess()))
            onlyIf {
                project.file(pathResolver.resolveSrcPathFromProject(extension.less?.src)).exists()
            }
        }
    }

    @TaskAction
    void exec() {
        if (!extension.less.enabled) {
            return
        }
        prepareWorkDir()
        writeLessScript()
        def srcRootDir = pathResolver.resolveSrcPathFromProject(extension.less.src)
        def srcRootFile = project.file(srcRootDir)
        def src = filterSource(srcRootDir)
        compile(src, srcRootFile.absolutePath)
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

    void compile(FileTree fileTree, String srcRootPath) {
        def tmpFile = project.file("${extension.workDir}/.lesssrc.json")
        def maps = []
        fileTree.each { File file ->
            maps += [
                path: file.absolutePath,
                name: file.name,
                destDir: new File("${extension.workDir}/${pathResolver.getDestLess()}/${file.parent.replace(srcRootPath, "")}").canonicalPath,
            ]
        }
        tmpFile.text = JsonOutput.toJson(maps)
        def triremeNodeRunner = new TriremeNodeRunner(
            scriptName: SCRIPT_NAME,
            workingDir: extension.workDir,
            args: [
                'less',
                tmpFile.absolutePath,
                extension.less.minify,
                extension.less.parallelize,
                mapLogLevel(extension.less.logLevel),
            ] as String[])
        triremeNodeRunner.exec()
    }
}
