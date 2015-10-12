package com.github.ksoichiro.web.resource.extension

import groovy.transform.EqualsAndHashCode

// @EqualsAndHashCode is required to execute valid up-to-date check.
@EqualsAndHashCode
class BowerDependency implements Serializable {
    static final long serialVersionUID = -1L
    String name
    String version
    List<String> filter
    String cacheName
}
