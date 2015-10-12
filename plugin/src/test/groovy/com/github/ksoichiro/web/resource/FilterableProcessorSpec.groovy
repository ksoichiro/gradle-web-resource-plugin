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
}
