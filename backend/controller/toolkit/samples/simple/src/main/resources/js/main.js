// Filename: main.js

require.config({
  paths: {
    'jquery': '/js/ext/jquery',
    'jquery-ui': '/js/ext/jquery-ui',
    'underscore': '/js/ext/underscore'
  },
  shim: {
    'jquery-ui' : {
      exports: '$',
      deps: ['jquery']
    }
  }
});

require([
  'app'
], function(App) {
});
