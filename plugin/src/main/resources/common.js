exports.logE = logE;
exports.logW = logW;
exports.logI = logI;
exports.logD = logD;
exports.logWriteI = logWriteI;
exports.mkdirsSync = mkdirsSync;
exports.mkdirsIfNotExistSync = mkdirsIfNotExistSync;

var fs = require('fs');

// 0: error, 1: warning, 2: info, 3: debug
function logE(level, message) {
  if (0 <= level) {
    console.log(message);
  }
}

function logW(level, message) {
  if (1 <= level) {
    console.log(message);
  }
}

function logI(level, message) {
  if (2 <= level) {
    console.log(message);
  }
}

function logD(level, message) {
  if (3 <= level) {
    console.log(message);
  }
}

function logWriteI(level, message) {
  if (2 <= level) {
    process.stdout.write(message);
  }
}

function mkdirsSync(dir) {
  var dirs = dir.split('/');
  var s = '';
  for (var i = 0; i < dirs.length; i++) {
    s += (s.length == 0 ? '' : '/') + dirs[i];
    if (!fs.existsSync(s)) {
      try {
        fs.mkdirSync(s);
      } catch (err) {
        if (err.code == 'EEXIST') {
          // Other thread might create this directory at the same time, so ignore it
        }
      }
    }
  }
}

function mkdirsIfNotExistSync(dir) {
  if (!fs.existsSync(dir)) {
    mkdirsSync(dir);
  }
}
