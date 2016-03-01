# Release

1. Update `PLUGIN_VERSION` in `gradle/variables.properties`.
1. Update version in `README.md` by executing `./gradlew updateReadme`.
1. Commit changes.
1. `./gradlew clean build uploadArchives -Prelease bintrayUpload publishPlugin`
1. Release modules manually at oss.sonatype.org.
1. Release modules manually at bintray.com.
