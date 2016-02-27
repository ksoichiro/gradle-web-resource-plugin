fs = require 'fs'
mkdirp = require 'mkdirp'
EventEmitter = require('events').EventEmitter

@.exitCode = 0

mkdirsSync = (dir) -> mkdirp.sync dir

mkdirsIfNotExistSync = (dir) -> mkdirsSync dir unless fs.existsSync dir

setExitCode = (exitCode) -> @.exitCode = exitCode

hasError = -> @.exitCode isnt 0

pin = (exitCondition) -> setImmediate -> pin exitCondition unless exitCondition()

handleExit = (validationCb) ->
  # Calling exit from async function does not work,
  # so hook exiting event and exit again.
  that = @
  process.on 'exit', (code) ->
    if code is 0
      if that.exitCode is 0
        validationCb?()
    else
      if that.exitCode is 0
        that.exitCode = code
    process.reallyExit that.exitCode

installSequentially = (elements, singleInstaller) ->
  installer = new EventEmitter
  setImmediate -> installer.emit 'install', 0

  finished = 0
  installer.on 'finish', -> finished = 1

  pin -> finished isnt 0

  that = this
  installer.on 'install', (idx) ->
    if elements.length <= idx
      setImmediate -> installer.emit 'finish'
      return
    item = elements[idx]
    singleInstaller item, ->
      if that.exitCode is 0
        setImmediate -> installer.emit 'install', idx + 1
      else
        that.exitCode = 1
        setImmediate -> installer.emit 'finish'

install = (parallelInstaller) -> installSequentially [1], parallelInstaller

exports.mkdirsSync = mkdirsSync
exports.mkdirsIfNotExistSync = mkdirsIfNotExistSync
exports.handleExit = handleExit
exports.installSequentially = installSequentially
exports.install = install
exports.setExitCode = setExitCode
exports.hasError = hasError
