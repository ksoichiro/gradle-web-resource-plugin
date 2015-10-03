var bower = require('bower');

var bowerPackages = ${dependencies};

var packages = [];
for (var e in bowerPackages) {
  if(bowerPackages.hasOwnProperty(e)) {
    var p = { name: e.toString(), version: bowerPackages[e] };
    packages.push(p);
  }
}

installWithCacheIfPossible(0);

function installWithCacheIfPossible(idx) {
  if (packages.length <= idx) {
    return;
  }
  var e = packages[idx];
  checkCache(e.name, e.version)
  .then(install)
  .then(function() {
    installWithCacheIfPossible(idx + 1);
  });
}

function checkCache(name, version) {
  return new Promise(function(resolve, reject) {
    bower.commands.cache.list([name, {}, {}])
    .on('end', function (r) {
      var offline = false;
      for (var i = 0; i < r.length; i++) {
        if (r[i].pkgMeta.version == version) {
          offline = true;
          break;
        }
      }
      resolve([name, version, offline]);
    });
  });
}

function install(data) {
  var name = data.shift();
  var version = data.shift();
  var offline = data.shift();
  return new Promise(function(resolve, reject) {
    // TODO install multiple packages at once
    bower.commands.install([name + '#' + version], {}, { 'offline': offline })
    .on('error', function (err) {
        console.log(err);
        process.exit(1);
    })
    .on('end', function (installed) {
        console.log('installed ' + name + '#' + version + (offline ? ' (offline)' : ''));
        resolve();
    });
  });
}
