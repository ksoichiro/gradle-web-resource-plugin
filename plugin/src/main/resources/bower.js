var fs = require('fs');
var util = require('util');
var bower = require('bower');
var chalk = require('bower/lib/node_modules/chalk');
var Q = require('q');
var common = require('./common');
var Logger = require('./logger');

// [ {name: 'foo', version: '1.0.0', cacheName: 'Foo.js' }, ... ];
var packages = JSON.parse(fs.readFileSync(process.argv[2], 'utf-8'));
var resolutions = JSON.parse(fs.readFileSync(process.argv[3], 'utf-8'));
var logLevel = parseInt(process.argv[4]);

var log = new Logger(logLevel, 'Bower');

common.handleExit(validateInstalledPackages);

common.installSequentially(packages, bowerInstallPackage);

function bowerInstallPackage(item, cb) {
  var cacheName = item.name;
  if (item.hasOwnProperty('cacheName')) {
    cacheName = item.cacheName;
  }
  Q.fcall(function() { return [item.name, item.version, cacheName]; })
  .then(checkCache)
  .then(install)
  .catch(function(error) {
    if (error) {
      log.e('Install failed: ' + cacheName + ': ' + error.stack);
    }
    common.setExitCode(1);
  })
  .done(function() {
    if (!common.hasError() && !fs.existsSync('bower_components/' + cacheName)) {
      log.e('Install failed: module does not exist: ' + cacheName);
      common.setExitCode(1);
    }
    if (cb) {
      cb();
    }
  });
}

function checkCache(data) {
  var name = data.shift();
  var version = data.shift();
  var cacheName = data.shift();
  var deferred = Q.defer();
  bower.commands.cache.list([cacheName], {}, {})
  .on('error', function(err) {
    log.e('Checking cache failed: ' + err);
    deferred.reject();
  })
  .on('end', function (r) {
    var offline = false;
    for (var i = 0; i < r.length; i++) {
      if (r[i].pkgMeta.version == version) {
        offline = true;
        break;
      }
    }
    deferred.resolve([name, version, cacheName, offline]);
  });
  return deferred.promise;
}

function install(data) {
  var name = data.shift();
  var version = data.shift();
  var cacheName = data.shift();
  var offline = data.shift();
  var deferred = Q.defer();

  var installedVersion = getInstalledVersion(name);
  if (installedVersion !== '') {
    // already installed
    if (version !== installedVersion) {
      deferred.reject(new Error(util.format('Update required for %s: please remove bower_components/%s and execute again.', name, name)));
    } else {
      deferred.resolve();
    }
    return deferred.promise;
  }

  // TODO install multiple packages at once
  var cached = false;
  var validate = false;
  var validCacheName = name;
  var hasResolutions = resolutions && Object.keys(resolutions).length > 0;
  if (hasResolutions) {
    saveCurrentPackageJson(cacheName, version);
  } else {
    deletePackageJson();
  }
  // If 1st arg(targets) is specified, their dependencies are marked as unresolvable, and resolutions have no effect.
  var targets = hasResolutions ? [] : [name + '#' + version]
  var config = { 'offline': offline }
  bower.commands.install(targets, {}, config)
  .on('log', function (l) {
    if (l.id === 'cached') {
      cached = true;
    } else if (l.id === 'validate') {
      log.d('Validating: ' + l.data.resolver.name);
      validCacheName = l.data.pkgMeta.name;
      validate = true;
    } else if (l.id === 'resolve') {
      log.i(util.format("Resolving: %s", l.data.resolver.name));
    } else if (l.id === 'download') {
      log.i(util.format("Downloading: %s", l.data.resolver.name));
    } else if (l.id === 'progress') {
      log.i(util.format("Downloading: %s#%s: %s", name, version, l.message));
    } else if (l.id === 'extract') {
      log.i(util.format("Extracting: %s", l.data.resolver.name));
    }
  })
  .on('error', function (err) {
    if (err) {
      if (err.code === 'ECONFLICT') {
        log.e('Install failed: Unable to find a suitable version for ' + err.name + ' while installing ' + name + '#' + version + '.');
        log.w('To resolve this conflict, please define resolution to your build.gradle.');
        log.w('Example:');
        log.w('    bower {');
        log.w('        dependencies {');
        log.w("            resolve name: '" + err.name + "', version: '" + err.picks[0].pkgMeta.version + "'");
        log.w('        }');
        log.w('    }');

        log.w('Candidate versions:');
        for (var i = 1; i <= err.picks.length; i++) {
          var pick = err.picks[i - 1];
          var dependants = pick.dependants.map(function(dependant) {
            var result = dependant.pkgMeta.name + '#' + dependant.pkgMeta.version;
            if (result === 'webResource#undefined') {
              result = 'this project'
            }
            return result;
          });
          log.w('    ' + chalk.magenta(i + ') ') + chalk.cyan(pick.endpoint.name + '#' + pick.endpoint.target) + ' which resolved to ' + chalk.green(pick.pkgMeta.version) + ' and is required by ' + chalk.green(dependants.join(', ')));
        }
        log.w('Some candidates might not be shown above because they are not installed yet.');
        log.w('This is a limitation of this plugin.');
      } else {
        log.e('Install failed: ' + err.code + ': ' + err.stack);
      }
    }
    deferred.reject();
  })
  .on('end', function (installed) {
    log.i('Installed: ' + name + '#' + version + (offline ? ' (offline)' : ''));
    var validatedDueToDependencies = false;
    if (installed[cacheName] && installed[cacheName].dependencies) {
      validatedDueToDependencies = installed[cacheName].dependencies.hasOwnProperty(validCacheName);
    }
    if (cached && validate && !validatedDueToDependencies) {// offline === false) {
      // Installed with cache, but validation occurred
      log.w(util.format('  Note: cache key does not match to the package name.'));
      log.w(util.format('  It means, the package "%s" is cached ', name));
      log.w(util.format('  but still needed a network connection to validate the package '));
      log.w(util.format('  because the cache could not be found by the name "%s".', name));
      log.w(util.format('  To execute installation without any network connections, add cacheName: "%s"', validCacheName));
      log.w(util.format('  to "install" definition in your build.gradle. Example:'));
      log.w(util.format('      bower {'));
      log.w(util.format('          dependencies {'));
      log.w(util.format('              install name: "%s", version: "%s", cacheName: "%s"', name, version, validCacheName));
      log.w(util.format('          }'));
      log.w(util.format('      }'));
    } else if (installed[name] !== undefined && cacheName != installed[name].pkgMeta.name) {
      // Installed without cache but the name doesn't match to the cache key.
      validCacheName = installed[name].pkgMeta.name;
      log.w(util.format('  Note: cache key does not match to the package name.'));
      log.w(util.format('  It means, the package "%s" was cached ', name));
      log.w(util.format('  but you cannot use this cache in the next installation '));
      log.w(util.format('  because the cache will not be found by the name "%s".', name));
      log.w(util.format('  To use this cache in the next installation, add cacheName: "%s"', validCacheName));
      log.w(util.format('  to "install" definition in your build.gradle. Example:'));
      log.w(util.format('      bower {'));
      log.w(util.format('          dependencies {'));
      log.w(util.format('              install name: "%s", version: "%s", cacheName: "%s"', name, version, validCacheName));
      log.w(util.format('          }'));
      log.w(util.format('      }'));
    }
    deferred.resolve();
  });
  return deferred.promise;
}

function validateInstalledPackages() {
  packages.forEach(function(e) {
    var cacheName = e.name;
    if (e.hasOwnProperty('cacheName')) {
      cacheName = e.cacheName;
    }
    if (!fs.existsSync('bower_components/' + cacheName)) {
      common.setExitCode(1);
      log.w('Not installed: ' + cacheName);
    }
  });
  if (common.hasError()) {
    log.e('Some packages are not installed.');
  }
}

function getInstalledVersion(name) {
  // Some bower.json files don't have "version" attribute.
  // Try .bower.json first, then check bower.json.
  var bowerJson = 'bower_components/' + name + '/.bower.json';
  if (fs.existsSync(bowerJson)) {
    var pkg = JSON.parse(fs.readFileSync(bowerJson, 'utf8'));
    if (pkg.version) {
      return pkg.version;
    }
  }
  bowerJson = 'bower_components/' + name + '/bower.json';
  if (fs.existsSync(bowerJson)) {
    var pkg = JSON.parse(fs.readFileSync(bowerJson, 'utf8'));
    if (pkg.version) {
      return pkg.version;
    }
  }
  return '';
}

function saveCurrentPackageJson(name, version) {
  var json = {
    name: "bower",
    dependencies: {},
    resolutions: {}
  };
  json.dependencies[name] = version;
  json.resolutions = resolutions;
  var jsonStr = JSON.stringify(json, null, '  ');
  fs.writeFileSync('bower.json', jsonStr);
}

function deletePackageJson() {
  if (fs.existsSync('bower.json')) {
    fs.unlinkSync('bower.json');
  }
}
