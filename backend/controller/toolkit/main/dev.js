var fs = require('fs')

process.argv.forEach(function(val, index, array) {
  if (index === 2) {
    if (val == 'init') {
      init();
    } else if (val == 'teardown') {
      teardown();
    } else {
      console.log('invalid argument');
    }
  }
});

function init() {
  console.log('initializing ui dev environment');
}

function teardown() {
  console.log('tearing down ui dev environment');
}
