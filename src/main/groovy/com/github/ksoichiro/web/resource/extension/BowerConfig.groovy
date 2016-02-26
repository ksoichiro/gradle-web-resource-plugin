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
    List<BowerDependencyResolution> dependencyResolutions
    boolean parallelize
    List options
    List configs
    boolean copyAll

    BowerConfig() {
        this.dependencies = []
        this.dependencyResolutions = []
        this.options = []
        this.configs = []
        this.parallelize = true
        this.copyAll = false
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

    /**
     * Add a bower dependency resolution by ${@code configuration} map.
     * This is equivalent to resolutions in bower.json.
     *
     * @param configuration configuration map containing dependency resolution information
     */
    void resolve(Map configuration) {
        dependencyResolutions.add(new BowerDependencyResolution(configuration))
    }

    def methodMissing(String name, args) {
        this."$name" = args[0]
    }
}
