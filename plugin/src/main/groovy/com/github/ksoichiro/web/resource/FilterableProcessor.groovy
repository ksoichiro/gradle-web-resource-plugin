package com.github.ksoichiro.web.resource

class FilterableProcessor extends WebResourceProcessor {
    boolean enabled
    List filter
    boolean minify

    FilterableProcessor(String src, String dest) {
        super(src, dest)
        enabled = true
        minify = true
    }
}
