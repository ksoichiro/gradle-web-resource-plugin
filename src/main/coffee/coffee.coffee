module.exports = ->
  fs = require 'fs'
  path = require 'path'
  Q = require 'q'
  coffee = require 'coffee-script'
  common = require './common'
  Logger = require './logger'
  include = require './include'

  projectPath = (3 < process.argv.length and process.argv[3]) or path.resolve '../../'
  if 4 < process.argv.length and process.argv[4]
    coffeeSrcSet = JSON.parse fs.readFileSync process.argv[4], 'utf-8'
  else
    defaultCoffeeSrcSetFile = '.coffeesrc.json'
    if fs.existsSync defaultCoffeeSrcSetFile
      coffeeSrcSet = JSON.parse fs.readFileSync defaultCoffeeSrcSetFile, 'utf-8'
    else
      coffeeSrcSet = []
  minify = (5 < process.argv.length and process.argv[5]) or 'true'
  parallelize = (6 < process.argv.length and process.argv[6]) or 'true'
  logLevel = (7 < process.argv.length and parseInt process.argv[7]) or 3

  log = new Logger logLevel, 'CoffeeScript'
  extensions = null
  includedFiles = []

  coffeeConvertItem = (item, cb) ->
    log.d "Started: #{common.projectRelativePath projectPath, item.path}"
    coffeeConvert item.path, item.name, [path.dirname item.path], path.join(item.destDir, item.name.replace(/\.coffee/, '.js')), ->
      log.d "Finished: #{common.projectRelativePath projectPath, item.path}"
      cb?()

  coffeeConvert = (filepath, filename, searchPaths, outputPath, cb) ->
    startTime = common.startTime()
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
          execTime = common.formatExecutionTime startTime
          log.i "Compiled #{common.projectRelativePath projectPath, filepath} in #{execTime}"
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
