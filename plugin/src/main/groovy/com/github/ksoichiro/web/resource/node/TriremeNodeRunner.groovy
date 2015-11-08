package com.github.ksoichiro.web.resource.node

import io.apigee.trireme.core.NodeEnvironment
import io.apigee.trireme.core.NodeScript
import io.apigee.trireme.core.ScriptStatus

/**
 * Wrapper class to run Node.js with Trireme/Rhino.
 */
class TriremeNodeRunner {
    public static final String NODE_VERSION = "0.12"
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
        script.setNodeVersion(NODE_VERSION)
        status = script.execute().get()
        env.close()
    }
}
