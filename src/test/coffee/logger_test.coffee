Logger = require './logger'
log = new Logger 3, 'Test'

describe 'Logger', ->
  describe '#i', ->
    it 'should log as info level without error', ->
      log.i 'something'
