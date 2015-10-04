package com.github.ksoichiro.web.resource

import groovy.transform.EqualsAndHashCode

// @EqualsAndHashCode is required to execute valid up-to-date check.
@EqualsAndHashCode
class BowerDependency implements Serializable {
    String name
    String version
    List<String> filter
    String cacheName
}
