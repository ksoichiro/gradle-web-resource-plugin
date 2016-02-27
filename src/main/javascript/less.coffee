module.exports = ->
  fs = require 'fs'
  path = require 'path'
  less = require 'less'
  Q = require 'q'
  common = require './common'
  Logger = require './logger'

  lessSrcSet = JSON.parse fs.readFileSync process.argv[3], 'utf-8'
  minify = process.argv[4] is 'true'
  parallelize = process.argv[5] is 'true'
  logLevel = parseInt process.argv[6]

  log = new Logger logLevel, 'LESS'

  lessConvertItem = (item, cb) ->
    log.d "Started: #{item.name}"
    lessConvert item.path, item.name, [path.dirname item.path], path.join(item.destDir, item.name.replace(/\.less/, '.css')), ->
      log.d "Finished: #{item.name}"
      cb?()

  lessConvert = (filepath, filename, searchPaths, outputPath, cb) ->
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
          log.i "Compiled: #{filepath}"
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
