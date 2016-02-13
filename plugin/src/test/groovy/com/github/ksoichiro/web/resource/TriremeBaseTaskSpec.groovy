package com.github.ksoichiro.web.resource

import com.github.ksoichiro.web.resource.task.TriremeBaseTask
import org.gradle.api.logging.LogLevel

class TriremeBaseTaskSpec extends BaseSpec {
    def logLevel() {
        expect:
        s == TriremeBaseTask.mapLogLevel(l)

        where:
        l                   | s
        LogLevel.DEBUG      | "3"
        LogLevel.INFO       | "2"
        LogLevel.WARN       | "1"
        LogLevel.ERROR      | "0"
        LogLevel.LIFECYCLE  | "1"
        LogLevel.QUIET      | "1"
    }
}
