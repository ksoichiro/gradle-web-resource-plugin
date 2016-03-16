util = require 'util'
chalk = require 'bower/lib/node_modules/chalk'

try
  # console.log() in Trireme/Rhino cause weird output like this:
  # > Building 85% > :webResourceTestCoffeeScript
  # gradle-console module solves the problem.
  # When executed via gradle-web-resource-plugin,
  # console.log() will be executed as println in Gradle.
  gconsole = require 'gradle-console'
  console.log = (message) ->
    formattedMessage = util.format.apply null, arguments
    gconsole.log formattedMessage
catch err

class Logger
  constructor: (@level, @tag) ->

timestamp = ->
  now = new Date()
  h = "0#{now.getHours()}".slice -2
  m = "0#{now.getMinutes()}".slice -2
  s = "0#{now.getSeconds()}".slice -2
  hrt = process.hrtime()
  ms = "00#{Math.floor(((hrt[0] * 1e9 + hrt[1]) / 1e6) % 1000)}".slice(-3)
  "#{h}:#{m}:#{s}.#{ms}"

log = (level, tag, message) ->
  level = "#{level}     ".slice 0, 5
  if level is "ERROR"
    level = chalk.red level
  else if level is "WARN "
    level = chalk.yellow level
  else if level is "DEBUG"
    level = chalk.blue level
  console.log "#{chalk.gray timestamp()} #{level} #{chalk.cyan tag} #{message}"

Logger.prototype.e = (message) -> log 'ERROR', @.tag, message if 0 <= this.level
Logger.prototype.w = (message) -> log 'WARN',  @.tag, message if 1 <= this.level
Logger.prototype.i = (message) -> log 'INFO',  @.tag, message if 2 <= this.level
Logger.prototype.d = (message) -> log 'DEBUG', @.tag, message if 3 <= this.level

module.exports = Logger
