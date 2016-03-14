package com.github.ksoichiro.web.resource.util

import com.github.ksoichiro.web.resource.extension.WebResourceExtension
import org.gradle.api.Project

class PathResolver {
    Project project
    WebResourceExtension extension

    PathResolver(Project project, WebResourceExtension extension) {
        this.project = project
        this.extension = extension
    }

    List retrieveValidSrcCoffeePaths() {
        retrieveValidPaths(resolveSrcPath(extension.coffeeScript?.src))
    }

    List retrieveValidSrcTestCoffeePaths() {
        retrieveValidPaths(resolveSrcTestPath(extension.testCoffeeScript?.src))
    }

    List retrieveValidSrcLessPaths() {
        retrieveValidPaths(resolveSrcPath(extension.less?.src))
    }

    String getDestCoffee() {
        resolveDestPath(extension.coffeeScript?.dest)
    }

    String getDestTestCoffee() {
        resolveTestDestPath(extension.testCoffeeScript?.dest)
    }

    String getDestLess() {
        resolveDestPath(extension.less?.dest)
    }

    List retrieveValidDestLibPaths() {
        retrieveValidPaths(resolveDestPath(extension.lib?.dest))
    }

    List retrieveValidPaths(String... paths) {
        List result = []
        paths.findAll { project.file("${extension.workDir}/${it}") }.each {
            result += new File("${extension.workDir}/${it}").canonicalPath
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

    String resolveSrcTestPathFromProject(def path) {
        String src = ""
        if (path) {
            src += extension.testBase?.src ? "${extension.testBase?.src}/" : ""
            src += path
        }
        src
    }

    String resolveSrcPath(def path) {
        "../../${resolveSrcPathFromProject(path)}"
    }

    String resolveSrcTestPath(def path) {
        "../../${resolveSrcTestPathFromProject(path)}"
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

    String resolveTestDestPath(def path) {
        String dest = ""
        if (path) {
            dest = extension.testBase?.dest ? "${extension.testBase?.dest}/" : ""
            dest += path
            dest = "../../${dest}"
        }
        dest
    }
}
