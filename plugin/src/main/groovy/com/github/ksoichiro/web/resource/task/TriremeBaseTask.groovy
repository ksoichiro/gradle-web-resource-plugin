package com.github.ksoichiro.web.resource.task

import com.github.ksoichiro.web.resource.util.PathResolver
import com.github.ksoichiro.web.resource.extension.WebResourceExtension
import org.gradle.api.DefaultTask
import org.gradle.api.logging.LogLevel

/**
 * Base task to run JavaScript on Trireme.
 *
 * @author Soichiro Kashima
 */
class TriremeBaseTask extends DefaultTask {
    static final String COMMON_SCRIPT_NAME = "common.js"

    WebResourceExtension extension
    PathResolver pathResolver

    TriremeBaseTask() {
        project.afterEvaluate {
            extension = project.extensions.webResource
            pathResolver = new PathResolver(project, extension)
        }
    }

    void writeCommonScript() {
        new File(extension.workDir, COMMON_SCRIPT_NAME).text = getClass().getResourceAsStream("/${COMMON_SCRIPT_NAME}").text
    }

    static String mapLogLevel(LogLevel logLevel) {
        String level = "0"
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
        }
        level
    }
}
