package com.github.ksoichiro.web.resource

import com.moowork.gradle.node.task.NodeTask
import org.gradle.api.tasks.TaskAction

class CompileTask extends NodeTask {
    static final String NAME = "webCompile"

    CompileTask() {
        dependsOn([BowerInstallTask.NAME])
    }

    @TaskAction
    void exec() {
        // TODO write gulpfile.js to build/webResource/gulpfile.js
        // TODO node node_modules/gulp/bin/gulp.js
        def gulp = this.project.file(new File(this.project.node.nodeModulesDir, "node_modules/gulp/bin/gulp.js"))
        setScript(gulp)
    }
}
