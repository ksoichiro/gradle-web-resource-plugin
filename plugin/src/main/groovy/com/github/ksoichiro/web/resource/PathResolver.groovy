package com.github.ksoichiro.web.resource

import org.gradle.api.Project

class PathResolver {
    Project project
    WebResourceExtension extension

    PathResolver(Project project, WebResourceExtension extension) {
        this.project = project
        this.extension = extension
    }

    String getSrcCoffee() {
        resolveSrcPath(extension.coffeeScript?.src)
    }

    String getSrcLess() {
        resolveSrcPath(extension.less?.src)
    }

    String getDestCoffee() {
        resolveDestPath(extension.coffeeScript?.dest)
    }

    String getDestLess() {
        resolveDestPath(extension.less?.dest)
    }

    String getDestLib() {
        resolveDestPath(extension.lib?.dest)
    }

    List retrieveValidPaths(String... paths) {
        List result = []
        paths.findAll { project.file("${extension.workDir}/${it}") }.each {
            result += "${extension.workDir}/${it}"
        }
        result
    }

    String resolveSrcPathFromProject(def path) {
        String src = ""
        if (path) {
            src += extension.base?.src ? "${extension.base?.src}/" : ""
            src += path
        }
        src
    }
    String resolveSrcPath(def path) {
        "../../${resolveSrcPathFromProject(path)}"
    }

    String resolveDestPath(def path) {
        String dest = ""
        if (path) {
            dest = extension.base?.dest ? "${extension.base?.dest}/" : ""
            dest += path
            dest = "../../${dest}"
        }
        dest
    }
}
