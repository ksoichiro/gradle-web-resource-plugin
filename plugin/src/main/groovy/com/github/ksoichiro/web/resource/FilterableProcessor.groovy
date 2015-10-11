package com.github.ksoichiro.web.resource

class FilterableProcessor extends WebResourceProcessor {
    boolean enabled
    List<String> include
    List<String> exclude
    boolean minify

    FilterableProcessor(String src, String dest, List<String> include, List<String> exclude) {
        super(src, dest)
        enabled = true
        minify = true
        this.include = include
        this.exclude = exclude
    }
}
