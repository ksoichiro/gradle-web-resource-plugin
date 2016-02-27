try
  gconsole = require 'gradle-console'
catch err
  gconsole = (->
    @.log = (level, tag, message) ->
      console.log "#{level} #{tag} #{message}"
    return @
  )()

class Logger
  constructor: (@level, @tag) ->

# 0: error, 1: warning, 2: info, 3: debug
Logger.prototype.e = (message) -> gconsole.log 'ERROR', @.tag, message if 0 <= this.level

Logger.prototype.w = (message) -> gconsole.log 'WARN', @.tag, message if 1 <= this.level

Logger.prototype.i = (message) -> gconsole.log 'INFO', @.tag, message if 2 <= this.level

Logger.prototype.d = (message) -> gconsole.log 'DEBUG', @.tag, message if 3 <= this.level

module.exports = Logger
