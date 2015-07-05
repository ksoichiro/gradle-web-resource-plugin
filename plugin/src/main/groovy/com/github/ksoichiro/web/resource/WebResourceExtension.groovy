package com.github.ksoichiro.web.resource

import groovy.transform.ToString
import org.gradle.api.GradleException
import org.gradle.api.Project
import org.gradle.util.ConfigureUtil

@ToString
class WebResourceExtension {
    static final NAME = 'webResource'

    Project project
    File workDir
    Map resources
    Map npm
    Map bower
    LessProcessor less

    WebResourceExtension(Project project) {
        this.project = project
        this.workDir = project.file("${this.project.buildDir}/webResource");
        this.resources = [:]
        this.base = new WebResourceProcessor("src/main", "${this.project.buildDir.name}/webResource/outputs")
        this.coffeeScript = new WebResourceProcessor("coffee", "js")
        this.less = new LessProcessor()
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
        if (this.metaClass.hasProperty(this, name)) {
            return ConfigureUtil.configure(args[0] as Closure, this."$name")
        } else if (this.resources.containsKey(name)) {
            return ConfigureUtil.configure(args[0] as Closure, this.resources."$name")
        } else {
            throw new GradleException("Missing method: ${name}")
        }
    }
}
