# gradle-web-resource-plugin

* Use CoffeeScript, LESS and JavaScript libraries from Gradle
* You can write all settings to build.gradle
* Pre-defined tasks to build CoffeeScript, LESS, etc.
* You can integrate with Spring Boot easily

This plugin depends on [srs/gradle-node-plugin](https://github.com/srs/gradle-node-plugin).

## Usage

```gradle
buildscript {
    repositories {
        maven {
            url uri('https://oss.sonatype.org/content/repositories/snapshots/')
        }
    }
    dependencies {
        classpath 'com.github.ksoichiro:gradle-web-resource-plugin:0.0.1-SNAPSHOT'
    }
}

apply plugin: 'com.github.ksoichiro.web.resource'

webResource {
    base {
        src = 'src/main'
        dest = 'src/main/resources/static'
    }
    coffeeScript {
        src = 'coffee'
        dest = 'js'
    }
    less {
        src = 'less'
        dest = 'css'
    }
    lib {
        dest = 'lib'
    }
    bower {
        dependencies: [
            jquery: "1.11.2",
            bootstrap: "3.3.4",
        ],
        overrides: [
            jquery: {
              "main": "dist/*.min.*"
            },
            "bootstrap": {
              "main": ["dist/css/*.min.css", "dist/js/*.min.js", "dist/fonts/*"]
            }
        ]
    }
}
```

```sh
./gradlew webResourceCompile
```

## License

    Copyright 2015 Soichiro Kashima

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

        http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
