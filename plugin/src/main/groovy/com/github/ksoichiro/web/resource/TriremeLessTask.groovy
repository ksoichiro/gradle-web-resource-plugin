package com.github.ksoichiro.web.resource

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

class TriremeLessTask extends DefaultTask {
    static final String NAME = "triremeLess"
    WebResourceExtension extension
    TriremeNodeRunner triremeNodeRunner

    TriremeLessTask() {
        this.project.afterEvaluate {
            extension = project.webResource
            triremeNodeRunner = new TriremeNodeRunner(scriptName: 'less.js', workingDir: extension.workDir)
        }
    }

    @TaskAction
    void exec() {
        new File(extension.workDir, 'less.js').text = getClass().getResourceAsStream('/less.js').text
        def srcRootDir = 'src/main/less'
        def srcRootFile = project.file(srcRootDir)
        def src = project.fileTree(dir: srcRootDir)
        src.include '**/*.less'
        src.exclude '**/_*.less'
        src.each { File file ->
            println "Less: ${file}"
            triremeNodeRunner.setArgs([
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
