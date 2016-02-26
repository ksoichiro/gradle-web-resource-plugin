package com.github.ksoichiro.web.resource

import com.github.ksoichiro.web.resource.extension.WebResourceExtension
import com.github.ksoichiro.web.resource.extension.WebResourceProcessor
import org.gradle.api.GradleException
import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder

class ExtensionSpec extends BaseSpec {
    def "propertyMissing"() {
        setup:
        Project project = ProjectBuilder.builder().build()
        def extension = new WebResourceExtension(project)

        when:
        // Set
        extension.unknown = "foo"
        // Get
        extension.unknown
        // Get undefined property
        extension.undefined

        then:
        notThrown(Exception)
        extension.unknown == "foo"
    }

    def "methodMissing"() {
        setup:
        Project project = ProjectBuilder.builder().build()
        def extension = new WebResourceExtension(project)

        when:
        extension.unknown = new WebResourceProcessor(null, "foo")
        extension.unknown {
            src "bar"
        }

        then:
        notThrown(Exception)
    }

    def "methodMissing with undefined property"() {
        setup:
        Project project = ProjectBuilder.builder().build()
        def extension = new WebResourceExtension(project)

        when:
        extension.unknown {
            src "bar"
        }

        then:
        thrown(GradleException)
    }
}
