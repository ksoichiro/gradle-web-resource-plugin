var commands = {
  bower: require('./bower'),
  coffee: require('./coffee'),
  less: require('./less')
};

commands[process.argv[2]]();
