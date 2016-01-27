# Contribution guideline

Thank you for your contribution!

Please confirm the following guideline before contributing to this project.

## Issues

* Before submitting a new issue,
  please confirm that similar issues are not posted yet.
* Please describe your environment. (OS, Gradle version, plugin version, etc.)

## Pull requests

1. Fork this repository and clone it.
1. Create a new branch.
1. Write codes.
1. Commit changes to your branch.
1. Confirm all checks pass.
    1. Open your terminal.
    1. Move to the project's root directory.
    1. Execute `./gradlew clean check`.  
       If you're a Windows user, `./gradlew` would be `gradlew`.  
       Note that this project uses Gradle wrapper (gradlew)
       to lock the Gradle version,
       so even if you have your locally installed Gradle distribution,
       please execute the above command with Gradle wrapper. 
1. Push your branch to your forked repository.
1. Create a pull request against master branch of this repository.
1. When the pull request is created or updated codes are pushed to your existing pull request, automatic builds will be invoked on Travis CI and AppVeyor.  
   If something fails, please confirm again that the checks really succeeds on your machine.
