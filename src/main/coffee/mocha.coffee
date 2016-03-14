module.exports = ->
  testDir = (3 < process.argv.length and process.argv[3]) or '../../src/test/coffee'
  testDestDir = (4 < process.argv.length and process.argv[4]) or 'outputs/test'
  logLevel = (5 < process.argv.length and parseInt process.argv[5]) or 3
  Logger = require './logger'
  log = new Logger logLevel, 'Mocha'
  log.i 'Start Mocha'

  fs = require 'fs'
  path = require 'path'
  common = require './common'
  coffee = require 'coffee-script'
  Mocha = require 'mocha'
  mocha = new Mocha()

  common.mkdirsIfNotExistSync testDestDir

  fs.readdirSync(testDir).filter (file) ->
    # Only keep the .coffee files
    file.substr -7 is '.coffee'

  .forEach (file) ->
    coffeeString = fs.readFileSync path.join(testDir, file), 'utf8'
    js = coffee.compile coffeeString,
      filename: file
      bare: true
    jsPath = path.join(testDestDir, path.basename(file).replace('.coffee', '.js'))
    fs.writeFileSync jsPath, js

    log.i "Add test file: #{file} -> #{jsPath}"
    mocha.addFile(jsPath)

  mocha.run (failures) ->
    log.i "Finished"
    process.on 'exit', ->
      process.exit failures # exit with non-zero status if there were failures
