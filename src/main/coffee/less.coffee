module.exports = ->
  fs = require 'fs'
  path = require 'path'
  less = require 'less'
  Q = require 'q'
  common = require './common'
  Logger = require './logger'

  projectPath = (3 < process.argv.length and process.argv[3]) or path.resolve '../../'
  if 4 < process.argv.length and process.argv[4]
    lessSrcSet = JSON.parse fs.readFileSync process.argv[4], 'utf-8'
  else
    defaultLessSrcSetFile = '.lesssrc.json'
    if fs.existsSync defaultLessSrcSetFile
      lessSrcSet = JSON.parse fs.readFileSync defaultLessSrcSetFile, 'utf-8'
    else
      lessSrcSet = []
  minify = (5 < process.argv.length and process.argv[5]) or 'true'
  parallelize = (6 < process.argv.length and process.argv[6]) or 'true'
  logLevel = (7 < process.argv.length and parseInt process.argv[7]) or 3

  log = new Logger logLevel, 'LESS'

  lessConvertItem = (item, cb) ->
    log.d "Started: #{common.projectRelativePath projectPath, item.path}"
    lessConvert item.path, item.name, [path.dirname item.path], path.join(item.destDir, item.name.replace(/\.less/, '.css')), ->
      log.d "Finished: #{common.projectRelativePath projectPath, item.path}"
      cb?()

  lessConvert = (filepath, filename, searchPaths, outputPath, cb) ->
    startTime = common.startTime()
    (->
      deferred = Q.defer()
      lessString = fs.readFileSync filepath, 'utf8'
      less.render lessString,
        paths: searchPaths
        filename: filename
        compress: minify,
        (err, output) ->
          if err
            log.e "Compilation failed: #{err}"
            deferred.reject()
          else
            deferred.resolve output
      return deferred.promise
    )()
    .then (output) ->
      deferred = Q.defer()
      common.mkdirsIfNotExistSync path.dirname outputPath
      fs.writeFile outputPath, output.css, (err) ->
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
    for item in lessSrcSet
      lessConvertItem item, null
  else
    common.installSequentially lessSrcSet, lessConvertItem
