package com.github.ksoichiro.web.resource.task

import com.github.ksoichiro.web.resource.extension.WebResourceExtension
import com.github.ksoichiro.web.resource.util.PathResolver
import org.gradle.api.DefaultTask
import org.gradle.api.logging.LogLevel

/**
 * Base task to run JavaScript on Trireme.
 *
 * @author Soichiro Kashima
 */
class TriremeBaseTask extends DefaultTask {
    static final String SCRIPT_NAME = "build.js"
    WebResourceExtension extension
    PathResolver pathResolver

    TriremeBaseTask() {
        project.afterEvaluate {
            extension = project.extensions.webResource
            pathResolver = new PathResolver(project, extension)
        }
    }

    void prepareWorkDir() {
        def workDir = extension.workDir
        if (!workDir.exists()) {
            workDir.mkdirs()
        }
    }

    static String mapLogLevel(LogLevel logLevel) {
        String level = "1"
        switch (logLevel) {
            case LogLevel.DEBUG:
                level = "3"
                break
            case LogLevel.INFO:
                level = "2"
                break
            case LogLevel.WARN:
                level = "1"
                break
            case LogLevel.ERROR:
                level = "0"
                break
        }
        level
    }
}
