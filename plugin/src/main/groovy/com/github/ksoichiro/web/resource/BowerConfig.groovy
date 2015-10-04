package com.github.ksoichiro.web.resource

import groovy.transform.EqualsAndHashCode

// @EqualsAndHashCode is required to execute valid up-to-date check.
@EqualsAndHashCode
class BowerConfig implements Serializable {
    static final long serialVersionUID = -1L
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
}
