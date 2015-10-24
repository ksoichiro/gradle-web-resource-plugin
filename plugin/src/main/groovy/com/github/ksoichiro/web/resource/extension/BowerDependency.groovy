package com.github.ksoichiro.web.resource.extension

import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString

/**
 * Definition of a bower dependency.
 */
// @EqualsAndHashCode is required to execute valid up-to-date check.
@EqualsAndHashCode
@ToString
class BowerDependency implements Serializable {
    static final long serialVersionUID = -1L
    String name
    String version
    List<String> filter
    String cacheName
    String outputName

    /**
     * Return the cache name.
     * It will be {@code cacheName} if it's set, otherwise name will be returned.
     *
     * @return the name to be used as cache key
     */
    String getCacheName() {
        cacheName ?: name
    }

    /**
     * Return the output name.
     * It will be {@code outputName} if it's set, otherwise name will be returned.
     *
     * @return the name to be used as output directory
     */
    String getOutputName() {
        outputName ?: name
    }
}
