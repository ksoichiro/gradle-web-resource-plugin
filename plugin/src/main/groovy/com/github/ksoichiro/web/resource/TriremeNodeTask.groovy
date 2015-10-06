package com.github.ksoichiro.web.resource

import io.apigee.trireme.core.NodeEnvironment
import io.apigee.trireme.core.NodeScript
import io.apigee.trireme.core.ScriptStatus
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

class TriremeNodeTask extends DefaultTask {
    File workingDir
    String scriptName
    String scriptPath
    String[] args
    ScriptStatus status

    @TaskAction
    void exec() {
        execNode()
    }

    void execNode() {
        NodeEnvironment env = new NodeEnvironment()
        File path = scriptPath ? new File(scriptPath) : new File(workingDir, scriptName)
        NodeScript script = env.createScript(scriptName, path, args)
        script.setWorkingDirectory(workingDir.absolutePath)
        script.setNodeVersion("0.12")
        status = script.execute().get()
        env.close()
    }
}
