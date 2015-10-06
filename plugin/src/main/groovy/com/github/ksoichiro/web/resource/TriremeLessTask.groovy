package com.github.ksoichiro.web.resource

import groovyx.gpars.GParsPool
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

class TriremeLessTask extends DefaultTask {
    static final String NAME = "triremeLess"
    static final int NUM_OF_THREADS = 8
    WebResourceExtension extension

    TriremeLessTask() {
        project.afterEvaluate {
            extension = project.extensions.webResource
            getInputs()
                .files(retrieveValidPaths(getSrcLess()))
                .property('less.minify', extension.less?.minify)
                .property('version', WebResourceExtension.VERSION)
            getOutputs().files(retrieveValidPaths(getDestLess()))
        }
    }

    @TaskAction
    void exec() {
        if (!extension.less.enabled) {
            return
        }
        new File(extension.workDir, 'less.js').text = getClass().getResourceAsStream('/less.js').text
        def srcRootDir = 'src/main/less'
        def srcRootFile = project.file(srcRootDir)
        def src = project.fileTree(dir: srcRootDir)
        src.include '**/*.less'
        src.exclude '**/_*.less'
        GParsPool.withPool(NUM_OF_THREADS) {
            src.asConcurrent {
                src.each { File file ->
                    def triremeNodeRunner = new TriremeNodeRunner(
                        scriptName: 'less.js',
                        workingDir: extension.workDir,
                        args: [
                            // lessSrcPath
                            file.absolutePath,
                            // lessSrcName
                            file.name,
                            // lessDestDir
                            'outputs/css/'
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

    String getSrcLess() {
        resolveSrcPath(extension.less?.src)
    }

    String getDestLess() {
        resolveDestPath(extension.less?.dest)
    }

    List retrieveValidPaths(String... paths) {
        List result = []
        paths.findAll { project.file("${extension.workDir}/${it}") }.each {
            result += "${extension.workDir}/${it}"
        }
        result
    }
}
