package com.github.ksoichiro.web.resource

import groovy.transform.ToString
import groovy.transform.TupleConstructor
import org.gradle.api.logging.LogLevel

@ToString
@TupleConstructor
class WebResourceProcessor {
    String src
    String dest
    LogLevel logLevel

    def methodMissing(String name, args) {
        this."$name" = args[0]
    }
}
