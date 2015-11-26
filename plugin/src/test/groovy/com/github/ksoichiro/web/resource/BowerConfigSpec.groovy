package com.github.ksoichiro.web.resource

import com.github.ksoichiro.web.resource.extension.BowerConfig
import org.gradle.api.logging.LogLevel

class BowerConfigSpec extends BaseSpec {
    def "methodMissing"() {
        setup:
        def bc = new BowerConfig()

        when:
        bc.logLevel LogLevel.INFO

        then:
        notThrown(Exception)
        bc.logLevel == LogLevel.INFO
    }

    def "delegating to closure"() {
        setup:
        def bc = new BowerConfig()

        when:
        bc.dependencies {
            install(name: "foo")
        }

        then:
        1 == bc.dependencies.size()
        bc.dependencies[0].name == "foo"
    }
}
