if (process.argv.length < 3) {
  console.log('start: <working dir>');
}

var workingDir = process.argv[2];

console.log('Running NPM install in %s', workingDir);
process.chdir(workingDir);

var npm = require('npm');

var loglevel = process.argv[3];
var config = {"loglevel": loglevel};

npm.load(config, function(err, n) {
  if (err) {
    console.log('Error initializing NPM: %s', er);
    return;
  }

  npm.commands.install([], function(err) {
    if (err) {
      console.log('Error running NPM: %s', err);
      process.exit(2);
    }
  });
});
