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
    WebResourceProcessor base
    WebResourceProcessor coffeeScript
    WebResourceProcessor less
    WebResourceProcessor lib
    Map npm
    Map bower

    WebResourceExtension(Project project) {
        this.project = project
        this.workDir = project.file("${this.project.buildDir}/webResource");
        this.base = new WebResourceProcessor()
        this.coffeeScript = new WebResourceProcessor()
        this.less = new WebResourceProcessor()
        this.lib = new WebResourceProcessor()
    }

    def methodMissing(String name, def args) {
        if (this.metaClass.hasProperty(this, name)) {
            return ConfigureUtil.configure(args[0] as Closure, this."$name")
        } else {
            throw new GradleException("Missing method: ${name}")
        }
    }
}
