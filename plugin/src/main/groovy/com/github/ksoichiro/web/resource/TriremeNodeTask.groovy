package com.github.ksoichiro.web.resource

import io.apigee.trireme.core.NodeEnvironment
import io.apigee.trireme.core.NodeScript
import io.apigee.trireme.core.ScriptStatus
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

class TriremeNodeTask extends DefaultTask {
    static final String PRE_INSTALLED_NODE_MODULES_DIR = "node_modules"
    File workingDir
    String scriptName
    String scriptPath
    String[] args
    ScriptStatus status

    @TaskAction
    void exec() {
        URL url = getClass().getResource("/${PRE_INSTALLED_NODE_MODULES_DIR}/npm")
        String jarPath = url.toString().replaceAll("jar:file:", "").replaceAll("!.*\$", "")
        String npmInstallPath = "${workingDir}/node_modules/npm"
        File npmInstallDir = new File(npmInstallPath)
        if (!npmInstallDir.exists()) {
            npmInstallDir.mkdirs()
        }
        project.copy {
            from project.zipTree(new File(jarPath)).matching { it.include("${PRE_INSTALLED_NODE_MODULES_DIR}/npm") }
            into npmInstallDir
        }
        NodeEnvironment env = new NodeEnvironment()
        File path = scriptPath ? new File(scriptPath) : new File(workingDir, scriptName)
        NodeScript script = env.createScript(scriptName, path, args)
        script.setWorkingDirectory(workingDir.absolutePath)
        script.setNodeVersion("0.12")
        status = script.execute().get()
        env.close()
    }
}
