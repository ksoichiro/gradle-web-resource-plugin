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
    // Currently only npm versions that are provided as webjars are available
    String npmVersion = "2.11.2" // "1.3.26"
    String[] args
    ScriptStatus status

    @TaskAction
    void exec() {
        URL url = getClass().getResource("/META-INF/resources/webjars/npm")
        String jarPath = url.toString().replaceAll("jar:file:", "").replaceAll("!.*\$", "")
        String npmInstallPath = "${workingDir}/node_modules/npm"
        File npmInstallDir = new File(npmInstallPath)
        if (!npmInstallDir.exists()) {
            npmInstallDir.mkdirs()
        }
        project.copy {
            from project.zipTree(new File(jarPath)).matching { it.include("META-INF/resources/webjars/npm/**") }
            into "${npmInstallDir}/tmp"
        }
        project.copy {
            from project.fileTree("${npmInstallDir}/tmp/META-INF/resources/webjars/npm/${npmVersion}")
            into npmInstallDir
        }
        project.delete("${npmInstallDir}/tmp")
        if (npmVersion == "2.11.2") {
            // We should fix/overwrite npm's package.json because that of webjars' doesn't include
            // 'main' attribute in it and trireme doesn't recognize npm.
            new File("${npmInstallDir}/package.json").text = """\
{
  "name": "npm",
  "main": "./lib/npm.js"
}
"""
        }
        NodeEnvironment env = new NodeEnvironment()
        File path = scriptPath ? new File(scriptPath) : new File(workingDir, scriptName)
        NodeScript script = env.createScript(scriptName, path, args)
        script.setNodeVersion("0.12")
        status = script.execute().get()
        env.close()
    }
}
