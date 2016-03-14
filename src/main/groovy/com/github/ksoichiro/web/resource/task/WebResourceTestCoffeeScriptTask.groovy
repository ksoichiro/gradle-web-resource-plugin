package com.github.ksoichiro.web.resource.task

import com.github.ksoichiro.web.resource.extension.WebResourceExtension
import com.github.ksoichiro.web.resource.node.TriremeNodeRunner
import com.github.ksoichiro.web.resource.util.PathResolver
import groovy.json.JsonOutput
import org.gradle.api.file.FileTree
import org.gradle.api.tasks.TaskAction

/**
 * Gradle task to test CoffeeScript files.<br>
 * This task uses Trireme and Rhino to run Node modules.
 *
 * @author Soichiro Kashima
 */
class WebResourceTestCoffeeScriptTask extends TriremeBaseTask {
    static final String NAME = "webResourceTestCoffeeScript"

    WebResourceTestCoffeeScriptTask() {
        dependsOn([WebResourceCompileCoffeeScriptTask.NAME])
        project.afterEvaluate {
            extension = project.extensions.webResource
            pathResolver = new PathResolver(project, extension)
            getInputs()
                .files(pathResolver.retrieveValidSrcCoffeePaths())
                .files(pathResolver.retrieveValidSrcTestCoffeePaths())
                .property('version', WebResourceExtension.VERSION)
            getOutputs().files(pathResolver.retrieveValidPaths(pathResolver.getDestTestCoffee()))
            onlyIf {
                project.file(pathResolver.resolveSrcPathFromProject(extension.coffeeScript?.src)).exists()
                project.file(pathResolver.resolveSrcTestPathFromProject(extension.testCoffeeScript?.src)).exists()
            }
        }
    }

    @TaskAction
    void exec() {
        if (!extension.coffeeScript.enabled) {
            return
        }
        def srcRootDir = pathResolver.resolveSrcPathFromProject(extension.coffeeScript.src)
        def srcRootFile = project.file(srcRootDir)
        def src = filterSource(srcRootFile)

        // Copy CoffeeScript built source into test working directory
        project.copy {
            from "${extension.base.dest}/${extension.coffeeScript.dest}"
            into "${extension.base.dest}/test"
        }

        test(src, srcRootFile.absolutePath)
    }

    FileTree filterSource(def srcRootDir) {
        def src = project.fileTree(dir: srcRootDir)
        extension.testCoffeeScript.include.each { src.include it }
        extension.testCoffeeScript.exclude.each { src.exclude it }
        src
    }

    void test(FileTree fileTree, String srcRootPath) {
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
                'mocha',
                pathResolver.retrieveValidSrcTestCoffeePaths()[0],
                pathResolver.retrieveValidPaths(pathResolver.getDestTestCoffee())[0],
                mapLogLevel(extension.testCoffeeScript.logLevel),
            ] as String[])
        triremeNodeRunner.exec()
    }
}
