# gradle-web-resource-plugin

[![Build Status](https://travis-ci.org/ksoichiro/gradle-web-resource-plugin.svg?branch=master)](https://travis-ci.org/ksoichiro/gradle-web-resource-plugin)
[![jcenter](https://api.bintray.com/packages/ksoichiro/maven/gradle-web-resource-plugin/images/download.svg)](https://bintray.com/ksoichiro/maven/gradle-web-resource-plugin/_latestVersion)
[![Maven Central](http://img.shields.io/maven-central/v/com.github.ksoichiro/gradle-web-resource-plugin.svg?style=flat)](https://github.com/ksoichiro/gradle-web-resource-plugin/releases/latest)

Gradle plugin to use CoffeeScript, LESS and Bower libraries to focus on writing CoffeeScript and LESS files.  
You don't have to install `node`, `npm`, `bower`, `gulp`, etc.  
You don't have to write `package.json`, `bower.json`, `gulpfile.js`, etc.  
Just update your `build.gradle` and execute a task.

This plugin depends on [srs/gradle-node-plugin](https://github.com/srs/gradle-node-plugin).

## Usage

Apply plugin in build.gradle:

```gradle
// Gradle 2.1+
plugins {
    id "com.github.ksoichiro.web.resource" version "0.1.3"
}

// Gradle 2.0 and former
buildscript {
    repositories {
        jcenter()

        // for '-SNAPSHOT' version
        //maven {
        //    url uri('https://oss.sonatype.org/content/repositories/snapshots/')
        //}
    }
    dependencies {
        classpath 'com.github.ksoichiro:gradle-web-resource-plugin:0.1.3'
    }
}

apply plugin: 'com.github.ksoichiro.web.resource'
```

Configure plugin if needed:

```gradle
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
        // Default: ['**/*', '!**/_*.coffee']
        filter = ["app.coffee"]
    }
    // (Option) Change LESS src/dest directories and filter setting
    less {
        src = 'less'
        dest = 'css'
        // Default: ['**/*', '!**/_*.less']
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
            "gulp-uglify": "0.2.1",
            "gulp-include": "2.0.1"
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

## Include/exclude files

By default, the .coffee/.less files that has prefix '`_`' will be excluded (filtered).  
So if you want to include files into some specific files, and want to filter those files,  
please try the following instructions.

### CoffeeScript

Use `include` directive provided by [wiledal/gulp-include](https://github.com/wiledal/gulp-include).  
This plugin will include this library as a dependency, so you can use it without any configurations.

For example, in `app.coffee`, you can include `a/_b.coffee` by writing this:

```coffee
#=include a/_b.coffee
```

Then `a/_b.coffee` will be exploded into the `app.coffee` just before compiling,  
and `_b.coffee` itself will be filtered.  
As a result, you can see the compiled and concatenated JavaScript file `app.js`.

```coffee
# a.coffee:
#=include _b.coffee
console.log 'a'

# _b.coffee:
console.log 'b'
```

↓

```javascript
// a.js:
(function(){console.log("b"),console.log("a")}).call(this);

// b.js: (will not be generated)
```

### LESS

Use `import` directive (this feature is provided by LESS).

For example, in `app.less`, you can include `a/_b.less` by writing this:

```less
@import 'a/_b.less';
```

Then `a/_b.less` will be exploded into the `app.less` just before compiling,  
and `_b.less` itself will be filtered.  
As a result, you can see the compiled and concatenated CSS file `app.css`.

```less
// a.less:
@import '_b.less';
#a1 { color #f00; }

// _b.less:
#b1 { color #fff; }
```

↓

```css
/* a.css: */
#b1{color #fff;}#a1{color #f00;}

/* b.css: (will not be generated) */
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
