var fs = require('fs');
var bower = require('bower');

// [ {name: 'foo', version: '1.0.0', cacheName: 'Foo.js' }, ... ];
var packages = ${dependencies};

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
  checkCache(e.name, e.version, cacheName)
  .then(install)
  .then(function() {
    installWithCacheIfPossible(idx + 1);
  });
}

function checkCache(name, version, cacheName) {
  return new Promise(function(resolve, reject) {
    bower.commands.cache.list([cacheName], {}, {})
    .on('end', function (r) {
      var offline = false;
      for (var i = 0; i < r.length; i++) {
        if (r[i].pkgMeta.version == version) {
          offline = true;
          break;
        }
      }
      resolve([name, version, cacheName, offline]);
    });
  });
}

function install(data) {
  var name = data.shift();
  var version = data.shift();
  var cacheName = data.shift();
  var offline = data.shift();
  return new Promise(function(resolve, reject) {
    var installedVersion = getInstalledVersion(name);
    if (installedVersion !== '') {
      // already installed
      if (version !== installedVersion) {
        console.log('update required for %s: please remove bower_components/%s and execute again.', name, name);
        process.exit(1);
      } else if (version == installedVersion) {
        // already installed the version
      }
      resolve();
      return;
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
      }
    })
    .on('error', function (err) {
        console.log(err);
        process.exit(1);
    })
    .on('end', function (installed) {
        console.log('installed ' + name + '#' + version + (offline ? ' (offline)' : ''));
        if (cached && validate) {// offline === false) {
          // Installed with cache, but validation occurred
          console.log('  Note: cache key does not match to the package name.');
          console.log('  It means, the package "%s" is cached ', name);
          console.log('  but still needed a network connection to validate the package ');
          console.log('  because the cache could not be found by the name "%s".', name);
          console.log('  To execute installation without any network connections, add cacheName: "%s"', validCacheName);
          console.log('  to "install" definition in your build.gradle. Example:');
          console.log('      bower {');
          console.log('          dependencies {');
          console.log('              install name: "%s", version: "%s", cacheName: "%s"', name, version, validCacheName);
          console.log('          }');
          console.log('      }');
        } else if (installed[name] !== undefined && cacheName != installed[name].pkgMeta.name) {
          // Installed without cache but the name doesn't match to the cache key.
          validCacheName = installed[name].pkgMeta.name;
          console.log('  Note: cache key does not match to the package name.');
          console.log('  It means, the package "%s" was cached ', name);
          console.log('  but you cannot use this cache in the next installation ');
          console.log('  because the cache will not be found by the name "%s".', name);
          console.log('  To use this cache in the next installation, add cacheName: "%s"', validCacheName);
          console.log('  to "install" definition in your build.gradle. Example:');
          console.log('      bower {');
          console.log('          dependencies {');
          console.log('              install name: "%s", version: "%s", cacheName: "%s"', name, version, validCacheName);
          console.log('          }');
          console.log('      }');
        }
        resolve();
    });
  });
}

function getInstalledVersion(name) {
  var bowerJson = 'bower_components/' + name + '/bower.json';
  if (fs.existsSync(bowerJson)) {
    var pkg = JSON.parse(fs.readFileSync(bowerJson, 'utf8'));
    return pkg.version;
  }
  return '';
}
