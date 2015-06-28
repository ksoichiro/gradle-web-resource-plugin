package com.github.ksoichiro.web.resource

import org.gradle.api.GradleException
import org.gradle.api.Project
import org.gradle.util.ConfigureUtil

class WebResourceExtension {
    static final NAME = 'webResource'

    Project project
    File workDir
    Processor base
    Processor coffeeScript
    Processor less
    Map npm
    Map bower

    WebResourceExtension(Project project) {
        this.project = project
        this.workDir = project.file("${this.project.buildDir}/webResource");
        this.base = new Processor()
        this.coffeeScript = new Processor()
        this.less = new Processor()
    }

    def methodMissing(String name, def args) {
        if (this.metaClass.hasProperty(this, name)) {
            return ConfigureUtil.configure(args[0] as Closure, this."$name")
        } else {
            throw new GradleException("Missing method: ${name}")
        }
    }
}
