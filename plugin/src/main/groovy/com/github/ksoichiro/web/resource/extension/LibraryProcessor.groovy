package com.github.ksoichiro.web.resource.extension

class LibraryProcessor extends WebResourceProcessor {
    List<String> excludeFromClean
    boolean cleanOnUpdate

    LibraryProcessor(String dest) {
        super(null, dest)
        excludeFromClean = []
        cleanOnUpdate = true
    }
}
