exports.logE = logE;
exports.logW = logW;
exports.logI = logI;
exports.logD = logD;
exports.mkdirsSync = mkdirsSync;
exports.mkdirsIfNotExistSync = mkdirsIfNotExistSync;

var fs = require('fs');
var mkdirp = require('mkdirp');
var gconsole = require('gradle-console');

// 0: error, 1: warning, 2: info, 3: debug
function logE(level, tag, message) {
  if (0 <= level) {
    gconsole.log('ERROR', tag, message);
  }
}

function logW(level, tag, message) {
  if (1 <= level) {
    gconsole.log('WARN', tag, message);
  }
}

function logI(level, tag, message) {
  if (2 <= level) {
    gconsole.log('INFO', tag, message);
  }
}

function logD(level, tag, message) {
  if (3 <= level) {
    gconsole.log('DEBUG', tag, message);
  }
}

function mkdirsSync(dir) {
  mkdirp.sync(dir);
}

function mkdirsIfNotExistSync(dir) {
  if (!fs.existsSync(dir)) {
    mkdirsSync(dir);
  }
}
