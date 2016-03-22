Logger = require './logger'
log = new Logger 3, 'Test'

describe 'Logger', ->
  describe '#d', ->
    it 'should log as debug level without error', ->
      log.d 'something'
  describe '#i', ->
    it 'should log as info level without error', ->
      log.i 'something'
  describe '#w', ->
    it 'should log as warn level without error', ->
      log.w 'something'
  describe '#e', ->
    it 'should log as error level without error', ->
      log.e 'something'
