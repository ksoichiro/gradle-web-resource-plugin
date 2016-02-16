var fs = require('fs');
var util = require('util');
var bower = require('bower');
var chalk = require('bower/lib/node_modules/chalk');
var Q = require('q');
var mout = require('bower/lib/node_modules/mout');
var common = require('./common');
var Logger = require('./logger');

var packages = JSON.parse(fs.readFileSync(process.argv[2], 'utf-8'));
var dependencies = packages.dependencies;
var resolutions = packages.resolutions;
var options = packages.options;
var parallelize = process.argv[3] === 'true';
var logLevel = parseInt(process.argv[4]);

var log = new Logger(logLevel, 'Bower');

if (parallelize) {
  common.handleExit();
  common.install(installParallel);
} else {
  common.handleExit(validateInstalledDependencies);
  common.installSequentially(dependencies, bowerInstallDependency);
}

function bowerInstallDependency(item, cb) {
  var cacheName = item.name;
  if (item.hasOwnProperty('cacheName')) {
    cacheName = item.cacheName;
  }
  Q.fcall(function() { return [item.name, item.version, cacheName]; })
  .then(checkCache)
  .then(install)
  .catch(function(error) {
    if (error) {
      log.e('Install failed: ' + cacheName + ': ' + (error.stack ? error.stack : error));
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
  var name, version, cacheName;
  if (data) {
    name = data.shift();
    version = data.shift();
    cacheName = data.shift();
  }
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

function installParallel(dummy, cb) {
  Q.fcall(function() { return undefined; })
  .then(install)
  .catch(function(error) {
    if (error) {
      log.e('Install failed: ' + error.stack);
    }
    common.setExitCode(1);
  })
  .done(function() {
    if (cb) {
      cb();
    }
  });
}

function install(data) {
  var name, version, cacheName, offline;
  var isSerialInstall = data !== undefined;
  if (isSerialInstall) {
    name = data.shift();
    version = data.shift();
    cacheName = data.shift();
    offline = data.shift();
  }
  var deferred = Q.defer();

  if (isSerialInstall) {
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
    saveCurrentDependencyToJson(cacheName, version);
  } else {
    saveBowerJson();
  }

  var cached = false;
  var validate = false;
  var validCacheName = name;
  var installOptions = {};
  Object.keys(options).forEach(function(k) {
    installOptions[mout.string.camelCase(k)] = options[k];
  });
  // If 1st arg is specified, they are marked as unresolvable, and resolutions have no effect.
  bower.commands.install([], installOptions, { 'offline': offline })
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
    } else if (l.id === 'install') {
      log.d(util.format("Installing: %s", l.message));
    }
  })
  .on('error', function (err) {
    if (err) {
      if (err.code === 'ECONFLICT') {
        log.e('Install failed: Unable to find a suitable version for ' + err.name + (data ? ' while installing ' + name + '#' + version : '') + '.');
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
        if (isSerialInstall) {
          log.w('Some candidates might not be shown above because they are not installed yet.');
          log.w('This is a limitation of this plugin.');
        }
      } else {
        log.e('Install failed: ' + err.code + ': ' + err.stack);
      }
    }
    deferred.reject();
  })
  .on('end', function (installed) {
    if (isSerialInstall) {
      Object.keys(installed).forEach(function(k) {
        var i = installed[k];
        log.i('Installed: ' + i.pkgMeta.name + '#' + i.pkgMeta.version + (offline ? ' (offline)' : ''));
      });
      var hasDependencies = Object.keys(installed).length > 1;
      if (cached && validate && !hasDependencies) {// offline === false) {
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
    } else {
      Object.keys(installed).forEach(function(k) {
        var i = installed[k];
        log.i('Installed: ' + i.pkgMeta.name + '#' + i.pkgMeta.version);
      });
    }
    deferred.resolve();
  });
  return deferred.promise;
}

function validateInstalledDependencies() {
  dependencies.forEach(function(e) {
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
    log.e('Some dependencies are not installed.');
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

function saveCurrentDependencyToJson(name, version) {
  var json = {
    name: "webResource",
    dependencies: {},
    resolutions: {}
  };
  json.dependencies[name] = version;
  json.resolutions = resolutions;
  var jsonStr = JSON.stringify(json, null, '  ');
  fs.writeFileSync('bower.json', jsonStr);
}

function saveBowerJson() {
  var json = {
    name: "webResource",
    dependencies: {},
    resolutions: resolutions
  };
  dependencies.forEach(function(dependency) {
    json.dependencies[dependency.name] = dependency.version;
  });
  var jsonStr = JSON.stringify(json, null, '  ');
  fs.writeFileSync('bower.json', jsonStr);
}
