package com.github.ksoichiro.web.resource

import com.github.ksoichiro.web.resource.extension.BowerConfig
import org.gradle.api.logging.LogLevel
import spock.lang.Specification

class BowerConfigSpec extends Specification {
    def "methodMissing"() {
        setup:
        def bc = new BowerConfig()

        when:
        bc.logLevel LogLevel.INFO

        then:
        notThrown(Exception)
        bc.logLevel == LogLevel.INFO
    }
}
