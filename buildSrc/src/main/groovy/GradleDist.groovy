import org.gradle.api.Project
import org.gradle.api.file.FileTree
import org.gradle.wrapper.Download
import org.gradle.wrapper.Install
import org.gradle.wrapper.Logger
import org.gradle.wrapper.PathAssembler
import org.gradle.wrapper.WrapperConfiguration

/**
 * Based on a workaround reported by Abel Salgado Romero in https://issues.gradle.org/browse/GRADLE-1715
 * and the codes in https://github.com/jruby-gradle/jruby-gradle-plugin.
 *
 * Include this in your project's buildSrc, then add a dependency to your project:
 * compile new GradleDist(project, '2.6').asFileTree
 *
 * Code courtesy of @ajoberstar
 */
class GradleDist {
    private final Project project
    final String version

    GradleDist(Project project, String version) {
        this.project = project
        this.version = version
    }

    String getPath() {
        // We use 'all' distribution in this project, so we should specify not 'bin' but 'all'.
        return "https://services.gradle.org/distributions/gradle-${version}-all.zip"
    }

    File getAsFile() {
        return project.file(getPath())
    }

    URI getAsURI() {
        return project.uri(getPath())
    }

    FileTree getAsFileTree() {
        Logger logger = new Logger(true)
        Install install = new Install(logger, new Download(logger, 'gradle', ''), new PathAssembler(project.gradle.gradleUserHomeDir))
        WrapperConfiguration config = new WrapperConfiguration()
        config.distribution = getAsURI()
        File file = install.createDist(config)
        // We just want JARs under 'lib' and 'lib/plugin', so replace 'file' for 'dir:' in the original source
        // with new File(file, 'lib').
        // Also, we should add 'excludes' parameter to exclude conflicting dependency.
        project.fileTree(dir: new File(file, 'lib'), excludes: ['**/rhino-*.jar'], includes: ['**/*.jar'])
    }
}
