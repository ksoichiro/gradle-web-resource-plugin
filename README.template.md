# gradle-web-resource-plugin

[![Build Status](https://img.shields.io/travis/ksoichiro/gradle-web-resource-plugin/master.svg?style=flat-square)](https://travis-ci.org/ksoichiro/gradle-web-resource-plugin)
[![Build status](https://img.shields.io/appveyor/ci/ksoichiro/gradle-web-resource-plugin/master.svg?style=flat-square)](https://ci.appveyor.com/project/ksoichiro/gradle-web-resource-plugin)
[![Coverage Stagus](https://img.shields.io/coveralls/ksoichiro/gradle-web-resource-plugin/master.svg?style=flat-square)](https://coveralls.io/github/ksoichiro/gradle-web-resource-plugin?branch=master)
[![Bintray](https://img.shields.io/bintray/v/ksoichiro/maven/gradle-web-resource-plugin.svg?style=flat-square)](https://bintray.com/ksoichiro/maven/gradle-web-resource-plugin/_latestVersion)
[![Maven Central](http://img.shields.io/maven-central/v/com.github.ksoichiro/gradle-web-resource-plugin.svg?style=flat-square)](https://github.com/ksoichiro/gradle-web-resource-plugin/releases/latest)

> Gradle plugin to use CoffeeScript, LESS and Bower libraries without Node.js/npm.

You don't have to install `node`, `npm`, `bower`, `gulp`, etc.  
You don't have to write `package.json`, `bower.json`, `gulpfile.js`, etc.  
Just update your `build.gradle` and execute a task.

## Getting started

### Apply plugin in build.gradle

For Gradle 2.1+:

```gradle
plugins {
    id 'com.github.ksoichiro.web.resource' version '@PLUGIN_VERSION@'
}
```

Gradle 2.0 and former:

```gradle
buildscript {
    repositories {
        jcenter()
    }
    dependencies {
        classpath 'com.github.ksoichiro:gradle-web-resource-plugin:@PLUGIN_VERSION@'
    }
}

apply plugin: 'com.github.ksoichiro.web.resource'
```

If you use SNAPSHOT version:

```gradle
buildscript {
    repositories {
        jcenter()
        maven {
            url 'https://oss.sonatype.org/content/repositories/snapshots/'
        }
    }
    dependencies {
        classpath 'com.github.ksoichiro:gradle-web-resource-plugin:X.X.X-SNAPSHOT'
    }
}

apply plugin: 'com.github.ksoichiro.web.resource'
```

### Configure plugin if you need

See configuration section for details.

```gradle
webResource {
    bower {
        dependencies {
            install name: 'jquery', version: '1.11.2'
            install name: 'bootstrap', version: '3.3.4'
        }
    }
}
```

### Put your CoffeeScript and LESS source files

```
src
└── main
    ├── coffee
    │   └── app.coffee
    └── less
        └── app.less
```

### Execute build task

```console
$ ./gradlew webResourceCompile
```

You can see the built resources:

```
build/webResource/outputs
├── css
│   └── app.css
├── js
│   └── app.js
└── lib
    ├── bootstrap
:
```

## Why do you need this plugin?

If I would like to use JavaScript library for browsers,
Bower or this kind of package manager is good to manage dependencies.
Bower can be managed with npm, and npm or Bower works on Node.js,
so I also need to install Node.js to include JavaScript dependencies into our apps.

[srs/gradle-node-plugin](https://github.com/srs/gradle-node-plugin) does most of all things,
but still I (or other team members who writes Java codes usually) need to learn about node, npm, bower, etc.
These are so good software but all we want to do is just managing JavaScript dependencies
just like other jar dependencies.
I know the Webjars project is also trying to solve this problem,
but it supports not all of the JavaScript projects and some of the jars are uploaded
by someone who we don't know and their contents are not necessarily reliable.
We want to use directly the trusted JavaScript projects.

So I wrapped all of them with a Gradle plugin.

## Dependency

### Git

This plugin bundles bower, and it depends on git to handle dependencies.  
To use bower features, please install git first.

## Task

| Task name          | Description               |
| ------------------ | ------------------------- |
| webResourceCompile | Triggers all other tasks. If you need to set task dependency (e.g. `classes.dependsOn 'webResourceCompile'`), you should use this task. |
| webResourceInstallBowerDependencies | Installs JavaScript dependencies using bower. If the configuration `webResource.bower.dependencies` is empty, this task will be skipped (since v1.6.0). |
| webResourceCopyBowerDependencies    | Copies bower dependencies which `webResourceInstallBowerDependencies` installed to a certain directory. If the configuration `webResource.bower.dependencies` is empty, this task will be skipped (since v1.6.0). |
| webResourceCompileCoffeeScript      | Compiles CoffeeScript source files into JavaScript files. If the source directory does not exist, this task will be skipped (since v1.6.0). |
| webResourceTestCoffeeScript         | Tests CoffeeScript source files with Mocha. If the source directory does not exist, this task will be skipped (since v1.7.0). |
| webResourceCompileLess              | Compiles LESS source files into CSS files. If the source directory does not exist, this task will be skipped (since v1.6.0). |

## Configuration

All configurations are optional.

```gradle
webResource {
    base {
        // Change base directories for src/dest
        src = 'src/main'
        dest = 'src/main/resources/static'

        // You can omit '=' like this:
        // src 'src/main'
        // dest 'src/main/resources/static'
    }

    testBase {
        // Change base directories
        src = 'src/test'
    }

    coffeeScript {
        // Set false if you don't use CoffeeScript related features
        enabled = true
        // Change CoffeeScript src/dest directories
        src = 'coffee'
        dest = 'js'
        // Default: ['**/*.coffee']
        include = ['app.coffee']
        // Default: ['**/_*.coffee']
        exclude = ['**/_*.coffee']
        // Default: true
        minify = false
        // Default: true
        parallelize = true
    }

    testCoffeeScript {
        // Set false if you don't test CoffeeScript source files
        enabled = true
        // Change CoffeeScript src/dest directories
        src = 'coffee'
        dest = 'test'
    }

    less {
        // Set false if you don't use LESS related features
        enabled = true
        // Change LESS src/dest directories and filter setting
        src = 'less'
        dest = 'css'
        // Default: ['**/*.less']
        include = ['app.less']
        // Default: ['**/_*.less']
        exclude = ['**/_*.less']
        // Default: true
        minify = false
        // Default: true
        parallelize = true

        // Advanced filters (available from 1.1.0-SNAPSHOT)
        // If you need complex filtering, try 'filters' configuration.
        filters {
            // You can use include/exclude methods here.
            // 'exclude' excludes files from the current file tree.
            // 'include' includes files to the current file tree.
            // For example, with the next 2 filters, we can add
            // 'bootstrap.less' to the target file set
            // while ignoring all the other .less files
            // in bootstrap directory.
            // (This cannot be achieved with less.include/less.exclude configs.)
            exclude '**/bootstrap/less/**/*.less'
            include '**/bootstrap/less/bootstrap.less'

            // You can add more exclude/include if you want.
            //exclude '**/foo/**/*.less'
            //include '**/foo/**/bar*.less'
        }
    }

    lib {
        // Change directories for libraries downloaded with bower
        dest = 'lib'
    }

    bower {
        dependencies {
            // 'filter' filters files like main-bower-files
            install name: 'jquery', version: '1.11.2', filter: ['dist/*.min.*']
            install name: 'bootstrap', version: '3.3.4', filter: ['dist/css/*.min.css', 'dist/js/*.min.js', 'dist/fonts/*']

            // You can set your favorite name to 'outputName'.
            // e.g.
            //   build/webResource/bower_components/components-font-awesome
            // will be copied to
            //   build/webResource/outputs/lib/font-awesome
            install name: 'components-font-awesome', version: '4.3.0', outputName: 'font-awesome'

            // If there is a conflict, you can resolve it by using "resolve"
            //resolve name: 'jquery', version: '1.9.0'
        }

        // Set this option to true if you want to copy
        // all dependencies in build/webResource/bower_components directory
        // to lib.dest directory.
        // This is useful when your dependencies have transitive dependencies.
        // However this option can cause problems that old dependencies
        // are unintentionally copied, so it is false by default.
        copyAll true

        // Giving --force-latest option also work for resolving conflict
        //options = ["--force-latest"]

        // You can make bower installation serial, but be careful.
        // (See "Parallel installation for bower" section for details.) 
        //parallelize false
    }
}
```

## Samples

See [samples](samples) directory.

## Techniques and notes

### General

#### Running on Trireme and Rhino

This plugin executes node/npm using [Trireme][trireme] and [Rhino][rhino].  
Therefore the limitations in Trireme and Rhino might affect to the features in this plugin (e.g. performance).

#### Setting task dependency

If you use this plugin in Java project, you can set task dependency to execute build task provided by this plugin like this:

```gradle
// If you want to build CoffeeScript/LESS/bower before compiling Java sources,
// set dependency using 'compileJava' task:
compileJava.dependsOn 'webResourceCompile'
```

### CoffeeScript

#### Including and excluding files

By default, the `.coffee` files that has prefix '`_`' will be excluded (filtered).

If you want to include files into some specific files, and want to filter those files, use `include` directive.  
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

#### Including and excluding files

By default, the .coffee/.less files that has prefix '`_`' will be excluded (filtered).

If you want to include files into some specific files, and want to filter those files, use `import` directive (this feature is provided by LESS).

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

### Bower

#### Resolving version conflict

When some dependencies have transitive dependencies and they have conflict,
bower installation will fail.
To solve this problem, bower provides `resolutions` config and this plugin
can handle this option with `resolve`:

```gradle
webResource {
    bower {
        dependencies {
            resolve name: 'angular', version: '1.5.0'
        }
    }
}
```

#### Offline installation

When using parallel installation (default), the plugin will check if
all the dependencies (including transitive dependencies) are cached.
If they all have *exact version* notation and are cached,
installation will be executed using offline option.
This is faster than online installation.
*Exact version* means that the versions that does not include
any operator symbols to express version range (see [node-semver](https://github.com/npm/node-semver) for details).

For example, the following notation can be offline because it can be resolved to only 1 artifact.

```gradle
webResource {
    bower {
        dependencies {
            install name: "jquery", version: "1.10.2"
```

The following notations are valid but cannot be offline even when cache exists because they can be resolved to several versions. To lock version with cache might cause problems, especially in team development.

```gradle
webResource {
    bower {
        dependencies {
            install name: "jquery", version: ">= 1.9.0"
            install name: "angular", version: "latest"
```

#### Installation behind the proxy

If you run `webResourceInstallBowerDependencies` behind the proxy server, you must configure proxies for bower with one of the following methods.

##### Configure proxies with environment variables

Define environment variable `http_proxy` and `https_proxy`.

##### Configure proxies with .bowerrc

Create `~/.bowerrc` that defines `proxy` and `https-proxy`.

```json
{
  "proxy": "http://proxy.local",
  "https-proxy": "https://proxy.local"
}
```

#### Parallel and serial installation

By default, `webResourceInstallBowerDependencies` task will
install dependencies in parallel.  
This can be controlled by `webResource.bower.parallelize` option,
but we recommend not to overwrite it to `false`.

Serial install have issues around version resolution.  
When `bower.json` is created for each dependency,
some dependencies that has child dependencies
would install child dependencies without checking
installed or other dependencies.  
This will cause unintentional update, for example,
when jquery 1.11.2 is installed at first, and then bootstrap 3.3.4
is installed, bootstrap will also install jquery 2.2.0
(current latest stable version) because bootstrap doesn't know
that the compatible jquery version is already installed.  
This situation cannot be avoided as long as we use this method.  
Therefore we should execute normal bower installation in parallel.  
One reason that we had chosen the serial installation is
that bower's install API allows `offline` option that does not use
any network connections, and it could be used for each dependency
according to cache status.  
Offline installation is faster than one that uses network connection,
and often it's good for builds at restricted environment,
but when you have any dependencies that has child dependencies,
it is recommended to install them in parallel.

## Contribution

Contributions are welcome!  
Please check the [contribution guideline](CONTRIBUTING.md) before submitting issues or sending pull requests.

## Thanks

This plugin deeply depends on these excellent projects.

* [apigee/trireme][trireme]
    * Node engine for JVM used to execute bower, LESS, JavaScript.
* [mozilla/rhino][rhino]
    * JavaScript engine for JVM used as the backend of trireme.
* [srs/gradle-node-plugin](https://github.com/srs/gradle-node-plugin)
    * Node/npm wrapper for Gradle to aggregating npm dependencies.
* [npm/npm](https://github.com/npm/npm)
    * Node packages manager used in build phase to aggregate Node libraries.
* [substack/node-browserify](https://github.com/substack/node-browserify)
    * Used in build phase to merge and remove redundant Node modules.
* [bower/bower](https://github.com/bower/bower)
    * Bundled in the plugin jar to manage Browser JavaScript libraries.
* [less/less.js](https://github.com/less/less.js)
    * Bundled in the plugin jar to compile LESS files.
* [jashkenas/coffee-script](https://github.com/jashkenas/coffeescript)
    * Bundlded in the plugin jar to compile CoffeeScript files.
* [wiledal/gulp-include](https://github.com/wiledal/gulp-include)
    * Bundled partially (internal functions) in the plugin jar to use include feature for CoffeeScript.
* [mishoo/UglifyJS2](https://github.com/mishoo/UglifyJS2)
    * Bundled in the plugin jar to minify JavaScript files.
* [kriskowal/q](https://github.com/kriskowal/q)
    * Bundled in the plugin jar to execute async tasks without native Promise class.
* [isaacs/glob](https://github.com/isaacs/node-glob)
    * Bundled in the plugin jar to expand globs for minifying JavaScript files.
* [mochajs/mocha](https://github.com/mochajs/mocha)
    * Bundled in the plugin jar to test CoffeeScript files.

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

[trireme]:https://github.com/apigee/trireme
[rhino]:https://github.com/mozilla/rhino
