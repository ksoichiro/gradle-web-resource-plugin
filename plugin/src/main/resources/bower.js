var fs = require('fs');
var util = require('util');
var bower = require('bower');
var Q = require('q');
var common = require('./common.js');

var logLevel = parseInt(process.argv[2]);

// [ {name: 'foo', version: '1.0.0', cacheName: 'Foo.js' }, ... ];
var packages = JSON.parse(fs.readFileSync('bowerPackages.json', 'utf8'));

installWithCacheIfPossible(0);

function installWithCacheIfPossible(idx) {
  if (packages.length <= idx) {
    return;
  }
  var e = packages[idx];
  var cacheName = e.name;
  if (e.hasOwnProperty('cacheName')) {
    cacheName = e.cacheName;
  }
  Q.fcall(function() { return [e.name, e.version, cacheName]; })
  .then(checkCache)
  .then(install)
  .catch(function(error) {
    common.logE(logLevel, error);
    process.exit(1);
  })
  .done(function() {
    installWithCacheIfPossible(idx + 1);
  });
}

function checkCache(data) {
  var name = data.shift();
  var version = data.shift();
  var cacheName = data.shift();
  var deferred = Q.defer();
  bower.commands.cache.list([cacheName], {}, {})
  .on('error', function(err) {
    common.logE(logLevel, 'Checking cache failed');
    common.logE(logLevel, err);
    process.exit(1);
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
      common.logE(logLevel, 'update required for %s: please remove bower_components/%s and execute again.', name, name);
      process.exit(1);
    } else if (version == installedVersion) {
      // already installed the version
    }
    deferred.resolve();
    return deferred.promise;
  }

  // TODO install multiple packages at once
  var cached = false;
  var validate = false;
  var validCacheName = name;
  var writingProgress = false;
  bower.commands.install([name + '#' + version], {}, { 'offline': offline })
  .on('log', function (log) {
    if (log.id === 'cached') {
      cached = true;
    } else if (log.id === 'validate') {
      validCacheName = log.data.pkgMeta.name;
      validate = true;
    } else if (log.id === 'resolve') {
      common.logI(logLevel, util.format("resolving %s#%s", name, version));
    } else if (log.id === 'download') {
      common.logI(logLevel, util.format("downloading %s#%s", name, version));
    } else if (log.id === 'progress') {
      if (writingProgress) {
        common.logWriteI(logLevel, "\x1B[0G");
      }
      writingProgress = true;
      common.logWriteI(logLevel, util.format("downloading %s#%s: %s", name, version, log.message));
    } else if (log.id === 'extract') {
      if (writingProgress) {
        common.logI(logLevel, '');
      }
    }
  })
  .on('error', function (err) {
      common.logE(logLevel, err);
      process.exit(1);
  })
  .on('end', function (installed) {
      common.logI(logLevel, 'installed ' + name + '#' + version + (offline ? ' (offline)' : ''));
      if (cached && validate) {// offline === false) {
        // Installed with cache, but validation occurred
        common.logW(logLevel, '  Note: cache key does not match to the package name.');
        common.logW(logLevel, '  It means, the package "%s" is cached ', name);
        common.logW(logLevel, '  but still needed a network connection to validate the package ');
        common.logW(logLevel, '  because the cache could not be found by the name "%s".', name);
        common.logW(logLevel, '  To execute installation without any network connections, add cacheName: "%s"', validCacheName);
        common.logW(logLevel, '  to "install" definition in your build.gradle. Example:');
        common.logW(logLevel, '      bower {');
        common.logW(logLevel, '          dependencies {');
        common.logW(logLevel, '              install name: "%s", version: "%s", cacheName: "%s"', name, version, validCacheName);
        common.logW(logLevel, '          }');
        common.logW(logLevel, '      }');
      } else if (installed[name] !== undefined && cacheName != installed[name].pkgMeta.name) {
        // Installed without cache but the name doesn't match to the cache key.
        validCacheName = installed[name].pkgMeta.name;
        common.logW(logLevel, '  Note: cache key does not match to the package name.');
        common.logW(logLevel, '  It means, the package "%s" was cached ', name);
        common.logW(logLevel, '  but you cannot use this cache in the next installation ');
        common.logW(logLevel, '  because the cache will not be found by the name "%s".', name);
        common.logW(logLevel, '  To use this cache in the next installation, add cacheName: "%s"', validCacheName);
        common.logW(logLevel, '  to "install" definition in your build.gradle. Example:');
        common.logW(logLevel, '      bower {');
        common.logW(logLevel, '          dependencies {');
        common.logW(logLevel, '              install name: "%s", version: "%s", cacheName: "%s"', name, version, validCacheName);
        common.logW(logLevel, '          }');
        common.logW(logLevel, '      }');
      }
      deferred.resolve();
  });
  return deferred.promise;
}

function getInstalledVersion(name) {
  var bowerJson = 'bower_components/' + name + '/bower.json';
  if (fs.existsSync(bowerJson)) {
    var pkg = JSON.parse(fs.readFileSync(bowerJson, 'utf8'));
    return pkg.version;
  }
  return '';
}
