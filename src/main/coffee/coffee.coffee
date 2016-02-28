module.exports = ->
  fs = require 'fs'
  path = require 'path'
  Q = require 'q'
  coffee = require 'coffee-script'
  common = require './common'
  Logger = require './logger'
  include = require './include'

  projectPath = process.argv[3]
  coffeeSrcSet = JSON.parse fs.readFileSync process.argv[4], 'utf-8'
  minify = process.argv[5] is 'true'
  parallelize = process.argv[6] is 'true'
  logLevel = parseInt process.argv[7]

  log = new Logger logLevel, 'CoffeeScript'
  extensions = null
  includedFiles = []

  coffeeConvertItem = (item, cb) ->
    log.d "Started: #{common.projectRelativePath projectPath, item.path}"
    coffeeConvert item.path, item.name, [path.dirname item.path], path.join(item.destDir, item.name.replace(/\.coffee/, '.js')), ->
      log.d "Finished: #{common.projectRelativePath projectPath, item.path}"
      cb?()

  coffeeConvert = (filepath, filename, searchPaths, outputPath, cb) ->
    (->
      deferred = Q.defer()
      coffeeString = fs.readFileSync filepath, 'utf8'
      try
        coffeeString = include.processInclude coffeeString, filepath
        js = coffee.compile coffeeString, filename: filepath
        if minify
          minified = require('./uglify').minify js,
            fromString: true
            compress: evaluate: false
          js = minified.code
        deferred.resolve(js)
      catch err
        log.e "Compilation failed: #{err}"
        deferred.reject()
      deferred.promise
    )()
    .then (js) ->
      deferred = Q.defer()
      common.mkdirsIfNotExistSync path.dirname outputPath
      fs.writeFile outputPath, js, (err) ->
        if err
          log.e "Saving file failed: #{err}"
          deferred.reject()
        else
          log.i "Compiled: #{common.projectRelativePath projectPath, filepath}"
          deferred.resolve()
      deferred.promise
    .catch (err) ->
      if err
        log.e "Compilation failed: #{filename}: #{err}"
        log.w err.stack if err.stack
      common.setExitCode 1
    .done -> cb?()

  common.handleExit()

  if parallelize
    for item in coffeeSrcSet
      coffeeConvertItem item, null
  else
    common.installSequentially coffeeSrcSet, coffeeConvertItem
