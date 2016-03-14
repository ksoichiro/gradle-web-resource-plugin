lib = require './lib'
assert = require 'assert'
describe 'Test', ->
  describe '#test', ->
    it 'should be test', ->
      console.log 'test'
      assert.equal lib.foo(1), 2
