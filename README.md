# gradle-web-resource-plugin

[![Build Status](https://travis-ci.org/ksoichiro/gradle-web-resource-plugin.svg?branch=master)](https://travis-ci.org/ksoichiro/gradle-web-resource-plugin)
[![Maven Central](http://img.shields.io/maven-central/v/com.github.ksoichiro/gradle-web-resource-plugin.svg?style=flat)](https://github.com/ksoichiro/gradle-web-resource-plugin/releases/latest)

Gradle plugin to use CoffeeScript, LESS and Bower libraries to focus on writing CoffeeScript and LESS files.  
You don't have to install `node`, `npm`, `bower`, `gulp`, etc.  
You don't have to write `package.json`, `bower.json`, `gulpfile.js`, etc.  
Just update your `build.gradle` and execute a task.

This plugin depends on [srs/gradle-node-plugin](https://github.com/srs/gradle-node-plugin).

## Usage

Write build.gradle:

```gradle
buildscript {
    repositories {
        jcenter()

        // for '-SNAPSHOT' version
        //maven {
        //    url uri('https://oss.sonatype.org/content/repositories/snapshots/')
        //}
    }
    dependencies {
        classpath 'com.github.ksoichiro:gradle-web-resource-plugin:0.1.0'
    }
}

apply plugin: 'com.github.ksoichiro.web.resource'

webResource {
    // (Option) Write dependencies if you want to use library from bower
    bower = [
        dependencies: [
            jquery: "1.11.2",
            bootstrap: "3.3.4",
        ]
    ]
}
```

Put your CoffeeScript and LESS script:

```
src
└── main
    ├── coffee
    │   └── app.coffee
    └── less
        └── app.less
```

Execute build task:

```sh
$ ./gradlew webResourceCompile
```

You can see the built resources:

```sh
build/webResource/outputs
├── css
│   └── app.css
├── js
│   └── app.js
└── lib
    ├── bootstrap
:
```

## Configuration

```gradle
node {
    // (Option) Set node/npm versions
    version = '0.11.13'
    npmVersion = '1.4.16'
}

webResource {
    // (Option) Change base directories for src/dest
    base {
        src = 'src/main'
        dest = 'src/main/resources/static'

        // You can omit '=' like this:
        // src 'src/main'
        // dest 'src/main/resources/static'
    }
    // (Option) Change CoffeeScript src/dest directories
    coffeeScript {
        src = 'coffee'
        dest = 'js'
    }
    // (Option) Change LESS src/dest directories and filter setting
    less {
        src = 'less'
        dest = 'css'
        // Default: ['*', '!**/_*.less']
        filter = ["app.less"]
    }
    // (Option) Change directories for libraries downloaded with bower
    lib {
        dest = 'lib'
    }
    // (Option) Change versions of npm libraries that are internally used for bower and gulp.
    npm = [
        devDependencies: [
            bower: "1.3.12",
            gulp: "3.8.11",
            "coffee-script": "1.9.1",
            "main-bower-files": "2.7.0",
            "gulp-less": "3.0.2",
            "gulp-minify-css": "1.0.0",
            "gulp-filter": "2.0.2",
            "gulp-coffee": "2.3.1",
            "gulp-filter": "2.0.2",
            "gulp-uglify": "0.2.1"
        ]
    ]
    bower = [
        dependencies: [
            jquery: "1.11.2",
            bootstrap: "3.3.4",
        ],
        // (Option) Filter files using main-bower-files
        overrides: [
            jquery: [
                main: "dist/*.min.*"
            ],
            bootstrap: [
                main: ["dist/css/*.min.css", "dist/js/*.min.js", "dist/fonts/*"]
            ]
        ]
    ]
}
```

## Sample

See [samples/example](samples/example) directory.

To build this project:

```sh
$ git clone https://github.com/ksoichiro/gradle-web-resource-plugin.git
$ cd gradlew-web-resource-plugin
$ ./gradlew uploadArchives
$ cd samples/example
$ ./gradlew webResourceCompile
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
