package com.github.ksoichiro.web.resource.extension

import groovy.transform.EqualsAndHashCode
import org.gradle.api.logging.LogLevel

// @EqualsAndHashCode is required to execute valid up-to-date check.
@EqualsAndHashCode
class BowerConfig implements Serializable {
    static final long serialVersionUID = -1L
    LogLevel logLevel
    List<BowerDependency> dependencies

    BowerConfig() {
        this.dependencies = []
    }

    void dependencies(Closure configureClosure) {
        configureClosure.delegate = this
        configureClosure()
    }

    void install(Map configuration) {
        dependencies.add(new BowerDependency(configuration))
    }

    def methodMissing(String name, args) {
        this."$name" = args[0]
    }
}
