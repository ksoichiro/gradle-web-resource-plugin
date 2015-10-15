package com.github.ksoichiro.web.resource.extension

import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString
import org.gradle.api.logging.LogLevel

/**
 * Configuration for using bower.
 */
// @EqualsAndHashCode is required to execute valid up-to-date check.
@EqualsAndHashCode
@ToString
class BowerConfig implements Serializable {
    static final long serialVersionUID = -1L
    LogLevel logLevel
    List<BowerDependency> dependencies

    BowerConfig() {
        this.dependencies = []
    }

    /**
     * Configure bower dependencies by closure.
     *
     * @param configureClosure closure to be passed to this config object
     */
    void dependencies(Closure configureClosure) {
        configureClosure.delegate = this
        configureClosure()
    }

    /**
     * Add a bower dependency by {@code configuration} map.
     *
     * @param configuration configuration map containing dependency information
     */
    void install(Map configuration) {
        dependencies.add(new BowerDependency(configuration))
    }

    def methodMissing(String name, args) {
        this."$name" = args[0]
    }
}
