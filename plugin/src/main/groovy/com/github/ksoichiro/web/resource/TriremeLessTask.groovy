package com.github.ksoichiro.web.resource

import org.gradle.api.tasks.TaskAction

class TriremeLessTask extends TriremeNodeTask {
    static final String NAME = "triremeLess"
    WebResourceExtension extension

    TriremeLessTask() {
        setScriptName('less.js')
        this.project.afterEvaluate {
            extension = project.webResource
            setWorkingDir(extension.workDir)
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
            setArgs([
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
            execNode()
        }
    }
}
