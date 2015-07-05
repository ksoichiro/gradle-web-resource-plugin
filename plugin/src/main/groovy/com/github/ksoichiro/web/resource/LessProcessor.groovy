package com.github.ksoichiro.web.resource

class LessProcessor extends WebResourceProcessor {
    List filter

    LessProcessor() {
        super("less", "css")
    }
}
