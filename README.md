# gradle-web-resource-plugin

* Use CoffeeScript, LESS and JavaScript libraries from Gradle
* You can write all settings to build.gradle
* Pre-defined tasks to build CoffeeScript, LESS, etc.
* You can integrate with Spring Boot easily

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
    npm {
        devDependencies: [
            bower: "1.3.12",
            gulp: "3.8.11",
            del: "1.1.1",
            coffee-script: "1.9.1",
            main-bower-files: "2.7.0",
            gulp-less: "3.0.2",
            gulp-minify-css: "1.0.0",
            gulp-filter: "2.0.2",
            gulp-coffee: "2.3.1",
            gulp-uglify: "0.2.1"
        ],
        scripts: [
            prepublish: "bower install --config.interactive=false",
            build: "gulp build",
            clean: "gulp clean",
            watch: "gulp watch"
        ]
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
./gradlew webCompile
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
