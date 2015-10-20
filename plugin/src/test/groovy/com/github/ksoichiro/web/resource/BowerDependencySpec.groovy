package com.github.ksoichiro.web.resource

import com.github.ksoichiro.web.resource.extension.BowerDependency
import spock.lang.Specification

class BowerDependencySpec extends Specification {
    def "resolve cacheName"() {
        when:
        def bd = new BowerDependency(name: x, cacheName: y)

        then:
        cacheName == bd.getCacheName()

        where:
        x     | y     || cacheName
        'foo' | 'bar' || 'bar'
        'foo' | null  || 'foo'
    }
}
