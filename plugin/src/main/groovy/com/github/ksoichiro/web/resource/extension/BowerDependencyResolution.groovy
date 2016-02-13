package com.github.ksoichiro.web.resource.extension

import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString

/**
 * Definition of a bower dependency resolution.
 */
// @EqualsAndHashCode is required to execute valid up-to-date check.
@EqualsAndHashCode
@ToString
class BowerDependencyResolution implements Serializable {
    static final long serialVersionUID = -1L
    String name
    String version
}
