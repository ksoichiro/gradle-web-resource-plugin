package com.github.ksoichiro.web.resource

class FilterableProcessor extends WebResourceProcessor {
    List filter
    boolean minify

    FilterableProcessor(String src, String dest) {
        super(src, dest)
        minify = true
    }
}
