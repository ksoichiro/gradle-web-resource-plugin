# Release

1. Update VERSION_NAME in `gradle.properties`.
1. Update VERSION in `WebResourceExtension.groovy`.
1. Update version in `samples/example/build.gradle`.
1. Update version in `README.md`.
1. Commit changes.
1. `./gradlew clean build uploadArchives -Prelease bintrayUpload publishPlugin`
1. Release modules manually at oss.sonatype.org.
1. Release modules manually at bintray.com.
