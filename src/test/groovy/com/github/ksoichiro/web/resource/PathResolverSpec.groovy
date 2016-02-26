package com.github.ksoichiro.web.resource

import com.github.ksoichiro.web.resource.extension.WebResourceExtension
import com.github.ksoichiro.web.resource.util.PathResolver
import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder

class PathResolverSpec extends BaseSpec {
    WebResourceExtension extension
    PathResolver pathResolver

    def setup() {
        Project project = ProjectBuilder.builder().build()
        project.plugins.apply PLUGIN_ID
        extension = project.extensions.webResource
        pathResolver = new PathResolver(project, extension)
    }

    def resolveSrcPathFromProjectWithNullPath() {
        expect:
        "" == pathResolver.resolveSrcPathFromProject(null)
    }

    def resolveSrcPathFromProjectWithBaseIsNull() {
        setup:
        extension.base = null

        expect:
        "foo" == pathResolver.resolveSrcPathFromProject("foo")
    }

    def resolveDestPathWithNullPath() {
        expect:
        "" == pathResolver.resolveDestPath(null)
    }

    def resolveDestPathWithBaseIsNull() {
        setup:
        extension.base = null

        expect:
        "../../foo" == pathResolver.resolveDestPath("foo")
    }
}
