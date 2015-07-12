package com.github.ksoichiro.web.resource

import groovy.transform.ToString
import groovy.transform.TupleConstructor

@ToString
@TupleConstructor
class WebResourceProcessor {
    String src
    String dest

    def methodMissing(String name, args) {
        this."$name" = args[0]
    }
}
