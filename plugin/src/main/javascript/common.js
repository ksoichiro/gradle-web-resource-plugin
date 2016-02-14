exports.mkdirsSync = mkdirsSync;
exports.mkdirsIfNotExistSync = mkdirsIfNotExistSync;
exports.handleExit = handleExit;
exports.installSequentially = installSequentially;
exports.install = install;
exports.setExitCode = setExitCode;
exports.hasError = hasError;

var fs = require('fs');
var mkdirp = require('mkdirp');
var EventEmitter = require('events').EventEmitter;

this.exitCode = 0;

function mkdirsSync(dir) {
  mkdirp.sync(dir);
}

function mkdirsIfNotExistSync(dir) {
  if (!fs.existsSync(dir)) {
    mkdirsSync(dir);
  }
}

function setExitCode(exitCode) {
  this.exitCode = exitCode;
}

function hasError() {
  return this.exitCode !== 0;
}

function pin(exitCondition) {
  setImmediate(function() {
    if (!exitCondition()) {
      pin(exitCondition);
    }
  });
}

function handleExit(validationCb) {
  // Calling exit from async function does not work,
  // so hook exiting event and exit again.
  var that = this;
  process.on('exit', function(code) {
    if (code === 0) {
      if (that.exitCode === 0) {
        if (validationCb) {
          validationCb();
        }
      }
    } else {
      if (that.exitCode === 0) {
        that.exitCode = code;
      }
    }
    process.reallyExit(that.exitCode);
  });
}

function installSequentially(elements, singleInstaller) {
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

  var that = this;
  installer.on('install', function(idx) {
    if (elements.length <= idx) {
      setImmediate(function() {
        installer.emit('finish');
      });
      return;
    }
    var item = elements[idx];
    singleInstaller(item, function() {
      if (that.exitCode === 0) {
        setImmediate(function() {
          installer.emit('install', idx + 1);
        });
      } else {
        that.exitCode = 1;
        setImmediate(function() {
          installer.emit('finish');
        });
      }
    });
  });
};

function install(parallelInstaller) {
  installSequentially([1], parallelInstaller);
}
