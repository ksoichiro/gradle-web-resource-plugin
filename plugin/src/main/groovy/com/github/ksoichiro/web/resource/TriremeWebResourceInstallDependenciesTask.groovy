package com.github.ksoichiro.web.resource

import groovy.json.JsonOutput

class TriremeWebResourceInstallDependenciesTask extends TriremeNodeTask {
    static final String NAME = 'triremeWebResourceInstallDependencies'
    String loglevel = "warn"

    TriremeWebResourceInstallDependenciesTask() {
        setScriptName('start.js')
        this.project.afterEvaluate {
            def extension = project.webResource as WebResourceExtension
            getInputs()
                    .property('npm', extension.npm)
                    .property('less', extension.less && extension.less.enabled)
                    .property('coffeeScript', extension.coffeeScript && extension.coffeeScript.enabled)
                    .property('bower', extension.bower && !extension.bower.isEmpty())
                    .property('version', WebResourceExtension.VERSION)
            getOutputs().files(new File(extension.workDir, 'package.json'), new File(extension.workDir, 'node_modules'))
            setWorkingDir(extension.workDir)
            setArgs(extension.workDir.absolutePath)
        }
    }

    @Override
    void exec() {
        def extension = project.webResource as WebResourceExtension
        def workDir = new File(extension.workDir.toURI())
        if (!workDir.exists()) {
            workDir.mkdirs()
        }
        def npmConfig = extension.npm ? extension.npm.clone() as Map : [:]
        if (!npmConfig.containsKey('devDependencies')) {
            npmConfig['devDependencies'] = [:] as Map
        }
        // Suppress warning: npm WARN package.json @ No description
        if (!npmConfig.containsKey('description')) {
            npmConfig['description'] = "Auto-configured dependencies with gradle-web-resource plugin"
        }
        // Suppress warning: npm WARN package.json @ No repository field.
        if (!npmConfig.containsKey('repository')) {
            npmConfig['repository'] = [type: "git", url: "https://github.com/ksoichiro/gradle-web-resource-plugin"]
        }
        // Suppress warning: npm WARN package.json @ No license field.
        if (!npmConfig.containsKey('license')) {
            npmConfig['license'] = "Apache-2.0"
        }

        def dependencies = [
                "gulp"            : "3.9.0",
                "bower"           : "1.4.1",
                "main-bower-files": "2.9.0",
                "gulp-less"       : "3.0.3",
                "gulp-minify-css" : "1.2.0",
                "gulp-coffee"     : "2.3.1",
                "gulp-filter"     : "2.0.2",
                "gulp-uglify"     : "1.2.0",
                "gulp-include"    : "2.0.2",
        ] as Map
        if (!extension.less.enabled) {
            dependencies.remove("gulp-less")
            dependencies.remove("gulp-minify-css")
        }
        if (!extension.coffeeScript.enabled) {
            dependencies.remove("gulp-coffee")
            dependencies.remove("gulp-uglify")
            dependencies.remove("gulp-include")
        }
        if (!extension.bower || extension.bower.isEmpty()) {
            dependencies.remove("bower")
            dependencies.remove("main-bower-files")
        }
        dependencies.each { name, version ->
            if (!npmConfig.devDependencies.containsKey(name)) {
                npmConfig.devDependencies[name] = version
            }
        }

        new File(extension.workDir, 'start.js').text = """
if (process.argv.length != 3) {
  console.log('start: <working dir>');
}

var workingDir = process.argv[2];

console.log('Running NPM install in %s', workingDir);
process.chdir(workingDir);

var npm = require('npm');

var config = {"loglevel": "${loglevel}"};

npm.load(config, function(err, n) {
  if (err) {
    console.log('Error initializing NPM: %s', er);
    return;
  }

  npm.commands.install([], function(err) {
    if (err) {
      console.log('Error running NPM: %s', err);
      process.exit(2);
    }
  });
});
"""
        new File(extension.workDir, 'package.json').text = JsonOutput.prettyPrint(JsonOutput.toJson(npmConfig))
        // Suppress warning: npm WARN package.json @ No README data
        new File(extension.workDir, 'README.md').text = "WebResource npm packages"
        super.exec()
    }
}
