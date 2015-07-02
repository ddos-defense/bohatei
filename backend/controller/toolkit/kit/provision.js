require('shelljs/global');

// "constants"
var constants = {
  opendaylight : {
    source : pwd()+'/../main/src/main/resources',
    target : pwd()+'/../main/target/main-osgipackage/opendaylight'
  }
};
constants.opendaylight.cors = {
  source : constants.opendaylight.source+'/cors.json',
  target : constants.opendaylight.target+'/cors.json'
}
var cors = {};

// help
var help = 'Usage: node provision.js [ --scaffold <your-ip> | --teardown | --status ]' +
  '\n\n' +
  'Options:' +
  '\n -s, --scaffold <your-ip>     setup cors filter with <your-ip> address' +
  '\n -t, --teardown               tear down cors filter' +
  '\n -a, --status                 display current state' +
  '\n -h, --help                   display this help';

// basic args check
if (process.argv.length <= 2) {
  console.log(help);
  return;
}

// help args check
if (process.argv.length === 3
  && (process.argv[2] == '-h' 
  || process.argv[2] == '--help')) {
  console.log(help);
  return;
}

// status
function statusCheck() {
  if (test('-f', constants.opendaylight.cors.target)) {
    console.log('Scaffold setup in '+constants.opendaylight.cors.target);
  } else if (test('-f', constants.opendaylight.cors.source)) {
    console.log('Scaffold setup in '+constants.opendaylight.cors.source);
  } else {
    console.log('Currently not scaffolded');
  }
}

if (process.argv.length === 3
  && (process.argv[2] == '-a'
  || process.argv[2] == '--status')) {
  statusCheck();
  return;
}

// scaffold
function scaffold(ip) {
  cors.address = ip;
  // check if cors already exists
  if (test('-f', constants.opendaylight.cors.target)
    || test('-f', constants.opendaylight.cors.source)) {
    console.log('Scaffolding already exists');
    return;
  }
  // scaffold
  if (test('-d', constants.opendaylight.target)) {
    JSON.stringify(cors).to(constants.opendaylight.cors.target);
    console.log('Scaffolding initialized in '+constants.opendaylight.cors.target);
  } else if (test('-d', constants.opendaylight.source)) {
    JSON.stringify(cors).to(constants.opendaylight.cors.source);
    console.log('Scaffolding initialized in '+constants.opendaylight.cors.source);
  } else {
    console.log('Error: Unable to initialize scaffolding; your distribution directory doesn\'t seem to exist');
  }
}

if (process.argv.length === 3
  && (process.argv[2] == '-s'
  || process.argv[2] == '--scaffold')) {
  console.log('Error: specify your IP address');
  return;
}

if (process.argv.length === 4
  && (process.argv[2] == '-s'
  || process.argv[2] == '--scaffold')) {
  scaffold(process.argv[3]);
  return;
}

// teardown
function teardown() {
  if (test('-f', constants.opendaylight.cors.target)
    || test('-f', constants.opendaylight.cors.source)) {
    rm('-f', constants.opendaylight.cors.target);
    rm('-f', constants.opendaylight.cors.source);
    console.log('Teardown scaffold');
  } else {
    console.log('Currently not scaffolded');
  }
}

if (process.argv.length === 3
  && (process.argv[2] == '-t'
  || process.argv[2] == '--teardown')) {
  teardown();
  return;
}

// assume failed state (invalid args)
console.log('Error: unrecognized flag '+process.argv[2]+
'\nTry --help for options');
