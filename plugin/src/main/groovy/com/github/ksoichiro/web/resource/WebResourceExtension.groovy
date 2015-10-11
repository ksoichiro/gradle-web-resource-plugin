package com.github.ksoichiro.web.resource

import groovy.transform.ToString
import org.gradle.api.GradleException
import org.gradle.api.Project
import org.gradle.util.ConfigureUtil

@ToString
class WebResourceExtension {
    static final String NAME = 'webResource'
    static final String VERSION = '0.1.6'
    static final String PLUGIN_DIR_NAME = 'webResource'

    Project project
    File workDir
    Map resources
    BowerConfig bower
    FilterableProcessor coffeeScript
    FilterableProcessor less
    WebResourceProcessor lib

    WebResourceExtension(Project project) {
        this.project = project
        this.workDir = project.file("${this.project.buildDir}/${PLUGIN_DIR_NAME}")
        this.resources = [:]
        this.base = new WebResourceProcessor("src/main", "${this.project.buildDir.name}/${PLUGIN_DIR_NAME}/outputs")
        this.bower = new BowerConfig()
        this.coffeeScript = new FilterableProcessor("coffee", "js", ['**/*.coffee'], ['**/_*.coffee'])
        this.less = new FilterableProcessor("less", "css", ['**/*.less'], ['**/_*.less'])
        this.lib = new WebResourceProcessor(null, "lib")
    }

    def propertyMissing(String name) {
        if (resources.containsKey(name)) {
            resources."$name"
        }
    }

    def propertyMissing(String name, arg) {
        resources.put(name, arg)
    }

    def methodMissing(String name, def args) {
        if (metaClass.hasProperty(this, name)) {
            return ConfigureUtil.configure(args[0] as Closure, this."$name")
        } else if (resources.containsKey(name)) {
            return ConfigureUtil.configure(args[0] as Closure, this.resources."$name")
        } else {
            throw new GradleException("Missing method: ${name}")
        }
    }
}
