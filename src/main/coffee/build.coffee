commands =
  bower:  require './bower'
  coffee: require './coffee'
  less:   require './less'
  mocha:  require './mocha'

commands[process.argv[2]]()
