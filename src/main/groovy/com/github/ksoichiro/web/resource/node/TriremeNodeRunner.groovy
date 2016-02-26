package com.github.ksoichiro.web.resource.node

import io.apigee.trireme.core.NodeEnvironment
import io.apigee.trireme.core.NodeScript
import io.apigee.trireme.core.ScriptStatus
import org.fusesource.jansi.AnsiConsole
import org.gradle.api.GradleException

/**
 * Wrapper class to run Node.js with Trireme/Rhino.
 */
class TriremeNodeRunner {
    public static final String NODE_VERSION = "0.10"
    File workingDir
    String scriptName
    String scriptPath
    String[] args
    ScriptStatus status

    public void exec() {
        try {
            AnsiConsole.systemInstall()
            NodeEnvironment env = new NodeEnvironment()
            File path = scriptPath ? new File(scriptPath) : new File(workingDir, scriptName)
            NodeScript script = env.createScript(scriptName, path, args)
            script.setWorkingDirectory(workingDir.absolutePath)
            script.setNodeVersion(NODE_VERSION)
            status = script.execute().get()
            env.close()
            if (!successfullyFinished()) {
                throw new GradleException("Error occurred while processing JavaScript. exitCode: ${status?.exitCode}", status?.cause)
            }
        } finally {
            AnsiConsole.systemUninstall()
        }
    }

    public boolean successfullyFinished() {
        status != null && status.isOk()
    }
}
