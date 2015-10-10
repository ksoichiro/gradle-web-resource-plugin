# gradle-web-resource-plugin

[![Build Status](https://travis-ci.org/ksoichiro/gradle-web-resource-plugin.svg?branch=master)](https://travis-ci.org/ksoichiro/gradle-web-resource-plugin)
[![jcenter](https://api.bintray.com/packages/ksoichiro/maven/gradle-web-resource-plugin/images/download.svg)](https://bintray.com/ksoichiro/maven/gradle-web-resource-plugin/_latestVersion)
[![Maven Central](http://img.shields.io/maven-central/v/com.github.ksoichiro/gradle-web-resource-plugin.svg?style=flat)](https://github.com/ksoichiro/gradle-web-resource-plugin/releases/latest)
[![Coverage Status](https://coveralls.io/repos/ksoichiro/gradle-web-resource-plugin/badge.svg?branch=master&service=github)](https://coveralls.io/github/ksoichiro/gradle-web-resource-plugin?branch=master)

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
    id "com.github.ksoichiro.web.resource" version "0.1.6"
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
        classpath 'com.github.ksoichiro:gradle-web-resource-plugin:0.1.6'
    }
}

apply plugin: 'com.github.ksoichiro.web.resource'
```

Configure plugin if needed:

```gradle
webResource {
    // (Option) Write dependencies if you want to use library from bower
    bower {
        dependencies {
            install name: "jquery", version: "1.11.2"
            install name: "bootstrap", version: "3.3.4"
        }
    }
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
    // (Option) Set node versions
    version = '0.11.13'
}

// All configurations are optional.
webResource {
    base {
        // Change base directories for src/dest
        src = 'src/main'
        dest = 'src/main/resources/static'

        // You can omit '=' like this:
        // src 'src/main'
        // dest 'src/main/resources/static'
    }
    coffeeScript {
        // Set false if you don't use CoffeeScript related features
        enabled = true
        // Change CoffeeScript src/dest directories
        src = 'coffee'
        dest = 'js'
        // Default: ['**/*', '!**/_*.coffee']
        filter = ["app.coffee"]
        // Default: true
        minify = false
    }
    less {
        // Set false if you don't use LESS related features
        enabled = true
        // Change LESS src/dest directories and filter setting
        src = 'less'
        dest = 'css'
        // Default: ['**/*', '!**/_*.less']
        filter = ["app.less"]
        // Default: true
        minify = false
    }
    lib {
        // Change directories for libraries downloaded with bower
        dest = 'lib'
    }
    bower {
        dependencies {
            // 'filter' filters files like main-bower-files
            install name: "jquery", version: "1.11.2", filter: ["dist/*.min.*"]
            install name: "bootstrap", version: "3.3.4", filter: ["dist/css/*.min.css", "dist/js/*.min.js", "dist/fonts/*"]
        }
    }
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

**Currently this feature is disabled**

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

## Why do you need this plugin?

If I would like to use JavaScript library for browsers,
Bower or this kind of package manager is good to manage dependencies.
Bower can be managed with npm, and npm or Bower works on Node.js,
so I also need to install Node.js to include JavaScript dependencies into our apps.

[srs/gradle-node-plugin](https://github.com/srs/gradle-node-plugin) does most of all things,
but still I (or other team members who writes Java codes usually) need to learn about node, npm, bower, etc.
These are so good softwares but all we want to do is just managing JavaScript dependencies
just like other jar dependencies.
I know the Webjars project is also trying to solve this problem,
but it supports not all of the JavaScript projects and some of the jars are uploaded
by someone who we don't know and their contents are not necesserily reliable.
We want to use directly the trusted JavaScript projects.

So I wrapped all of them with a Gradle plugin.

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
