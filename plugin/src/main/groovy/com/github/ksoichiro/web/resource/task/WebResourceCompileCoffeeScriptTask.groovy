package com.github.ksoichiro.web.resource.task

import com.github.ksoichiro.web.resource.extension.WebResourceExtension
import com.github.ksoichiro.web.resource.node.TriremeNodeRunner
import com.github.ksoichiro.web.resource.util.PathResolver
import groovy.json.JsonOutput
import org.gradle.api.file.FileTree
import org.gradle.api.tasks.TaskAction

/**
 * Gradle task to compile CoffeeScript files.<br>
 * This task uses Trireme and Rhino to run Node modules.
 *
 * @author Soichiro Kashima
 */
class WebResourceCompileCoffeeScriptTask extends TriremeBaseTask {
    static final String NAME = "webResourceCompileCoffeeScript"
    static final String SCRIPT_NAME = "coffee.js"

    WebResourceCompileCoffeeScriptTask() {
        dependsOn([WebResourceInstallBowerDependenciesTask.NAME])
        project.afterEvaluate {
            extension = project.extensions.webResource
            pathResolver = new PathResolver(project, extension)
            getInputs()
                .files(pathResolver.retrieveValidSrcCoffeePaths())
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
        writeCoffeeScript()
        writeCommonScript()
        def srcRootDir = pathResolver.resolveSrcPathFromProject(extension.coffeeScript.src)
        def srcRootFile = project.file(srcRootDir)
        def src = filterSource(srcRootFile)
        compile(src, srcRootFile.absolutePath)
    }

    void writeCoffeeScript() {
        new File(extension.workDir, SCRIPT_NAME).text = getClass().getResourceAsStream("/${SCRIPT_NAME}").text
    }

    FileTree filterSource(def srcRootDir) {
        def src = project.fileTree(dir: srcRootDir)
        extension.coffeeScript.include.each { src.include it }
        extension.coffeeScript.exclude.each { src.exclude it }
        src
    }

    void compile(FileTree fileTree, String srcRootPath) {
        def tmpFile = project.file("${extension.workDir}/.coffeesrc.json")
        def maps = []
        fileTree.each { File file ->
            maps += [
                path: file.absolutePath,
                name: file.name,
                destDir: new File("${extension.workDir}/${pathResolver.getDestCoffee()}/${file.parent.replace(srcRootPath, "")}").canonicalPath,
            ]
        }
        tmpFile.text = JsonOutput.toJson(maps)
        def triremeNodeRunner = new TriremeNodeRunner(
            scriptName: SCRIPT_NAME,
            workingDir: extension.workDir,
            args: [
                tmpFile.absolutePath,
                extension.coffeeScript.minify,
                extension.coffeeScript.parallelize,
                mapLogLevel(extension.coffeeScript.logLevel),
            ] as String[])
        triremeNodeRunner.exec()
    }
}
