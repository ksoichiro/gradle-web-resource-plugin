package com.github.ksoichiro.web.resource

import com.github.ksoichiro.web.resource.extension.FilterableProcessor
import spock.lang.Specification

class FilterableProcessorSpec extends Specification {
    def "init"() {
        when:
        def fp = new FilterableProcessor("src", "dest", ["**/*.less"], ["**/_*.less"])

        then:
        fp.src == "src"
        fp.dest == "dest"
        fp.include == ["**/*.less"]
        fp.exclude == ["**/_*.less"]
        fp.enabled
        fp.minify
    }

    def "delegating to closure"() {
        setup:
        def fp = new FilterableProcessor("src", "dest", ["**/*.less"], ["**/_*.less"])

        when:
        fp.filters {
            include("**/app.less")
            exclude("**/_app.less")
        }

        then:
        2 == fp.filters.size()
        fp.filters[0].include == "**/app.less"
        fp.filters[1].exclude == "**/_app.less"
    }
}
