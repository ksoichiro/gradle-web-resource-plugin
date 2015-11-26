package com.github.ksoichiro.web.resource

import com.github.ksoichiro.web.resource.extension.BowerDependency

class BowerDependencySpec extends BaseSpec {
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

    def "resolve outputName"() {
        when:
        def bd = new BowerDependency(name: x, outputName: y)

        then:
        outputName == bd.getOutputName()

        where:
        x     | y     || outputName
        'foo' | 'bar' || 'bar'
        'foo' | null  || 'foo'
    }
}
