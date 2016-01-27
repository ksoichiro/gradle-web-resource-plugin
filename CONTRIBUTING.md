# Contribution guideline

Thank you for your contribution!

Please confirm the following guideline before contbuting to this project.

# Issues

* Before submitting a new issue,
please confirm that similar issues are not posted yet.
* Please describe your environment. (OS, Gradle version, plugin version, etc.)

# Pull requests

* Please confirm that all check passes successfully.
    * You can check it by..
        * opening your terminal
        * moving to the project's root directory
        * executing `./gradlew clean check`
            * If you're a Windows user, `./gradlew` would be `gradlew`.
* When the pull request is opened or updated codes are pushed to the pull request, automatic builds will be invoked on Travis CI and AppVeyor.
If something fail, please confirm again that the checks really succeeds on your machine.
