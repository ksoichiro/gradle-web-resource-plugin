package com.github.ksoichiro.web.resource

class FilterableProcessor extends WebResourceProcessor {
    List filter

    FilterableProcessor(String src, String dest) {
        super(src, dest)
    }
}
