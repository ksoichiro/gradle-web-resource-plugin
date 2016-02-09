exports.logE = logE;
exports.logW = logW;
exports.logI = logI;
exports.logD = logD;
exports.mkdirsSync = mkdirsSync;
exports.mkdirsIfNotExistSync = mkdirsIfNotExistSync;
exports.handleExit = handleExit;
exports.installSequentially = installSequentially;

var fs = require('fs');
var mkdirp = require('mkdirp');
var gconsole = require('gradle-console');
var EventEmitter = require('events').EventEmitter;

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

function pin(exitCondition) {
  setImmediate(function() {
    if (!exitCondition()) {
      pin(exitCondition);
    }
  });
}

function handleExit(logLevel, logTag, exitCodeCb, validationCb) {
  // Calling exit from async function does not work,
  // so hook exiting event and exit again.
  process.on('exit', function(code) {
    if (exitCodeCb() === 0) {
      if (code !== 0) {
        logW(logLevel, logTag, 'Process is exited by some module with code ' + code + '.');
      }
      if (validationCb) {
        validationCb();
      }
    }
    process.reallyExit(exitCodeCb());
  });
}

function installSequentially(elements, singleInstaller, continueCondition) {
  var installer = new EventEmitter;
  setImmediate(function() {
    installer.emit('install', 0);
  });

  var finished = 0;
  installer.on('finish', function() {
    finished = 1;
  });

  pin(function() {
    return finished !== 0;
  });

  installer.on('install', function(idx) {
    if (elements.length <= idx) {
      setImmediate(function() {
        installer.emit('finish');
      });
      return;
    }
    var item = elements[idx];
    singleInstaller(item, function() {
      if (continueCondition() === true) {
        setImmediate(function() {
          installer.emit('install', idx + 1);
        });
      }
    });
  });
};
