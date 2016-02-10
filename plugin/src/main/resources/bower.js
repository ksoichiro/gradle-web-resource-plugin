var fs = require('fs');
var util = require('util');
var bower = require('bower');
var Q = require('q');
var common = require('./common.js');
var Logger = require('./logger.js');

// [ {name: 'foo', version: '1.0.0', cacheName: 'Foo.js' }, ... ];
var packages = JSON.parse(fs.readFileSync(process.argv[2], 'utf-8'));
var logLevel = parseInt(process.argv[3]);

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
      log.e(error.toString());
    } else {
      log.e('Error is reported for ' + cacheName);
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
      log.e(util.format('Update required for %s: please remove bower_components/%s and execute again.', name, name));
      deferred.reject();
    } else {
      deferred.resolve();
    }
    return deferred.promise;
  }

  // TODO install multiple packages at once
  var cached = false;
  var validate = false;
  var validCacheName = name;
  bower.commands.install([name + '#' + version], {}, { 'offline': offline })
  .on('log', function (log) {
    if (log.id === 'cached') {
      cached = true;
    } else if (log.id === 'validate') {
      validCacheName = log.data.pkgMeta.name;
      validate = true;
    } else if (log.id === 'resolve') {
      log.i(util.format("Resolving: %s", log.data.resolver.name));
    } else if (log.id === 'download') {
      log.i(util.format("Downloading: %s", log.data.resolver.name));
    } else if (log.id === 'progress') {
      log.i(util.format("Downloading: %s#%s: %s", name, version, log.message));
    } else if (log.id === 'extract') {
      log.i(util.format("Extracting: %s", log.data.resolver.name));
    }
  })
  .on('error', function (err) {
    log.e('Install failed: ' + err);
    deferred.reject();
  })
  .on('end', function (installed) {
    log.i('Installed: ' + name + '#' + version + (offline ? ' (offline)' : ''));
    if (cached && validate) {// offline === false) {
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
