package com.github.ksoichiro.web.resource.node

import io.apigee.trireme.core.NodeEnvironment
import io.apigee.trireme.core.NodeScript
import io.apigee.trireme.core.ScriptStatus

class TriremeNodeRunner {
    File workingDir
    String scriptName
    String scriptPath
    String[] args
    ScriptStatus status

    public void exec() {
        NodeEnvironment env = new NodeEnvironment()
        File path = scriptPath ? new File(scriptPath) : new File(workingDir, scriptName)
        NodeScript script = env.createScript(scriptName, path, args)
        script.setWorkingDirectory(workingDir.absolutePath)
        script.setNodeVersion("0.12")
        status = script.execute().get()
        env.close()
    }
}
