package com.github.ksoichiro.web.resource.extension

class LibraryProcessor extends WebResourceProcessor {
    boolean cleanOnUpdate

    LibraryProcessor(String dest) {
        super(null, dest)
        cleanOnUpdate = true
    }
}
