// Filename: main.js

require.config({
  paths: {
    'jquery': '/js/ext/jquery/dist/jquery',
    'jquery-ui': '/js/ext/jquery-ui/ui/minified/jquery-ui.min',
    'underscore': '/js/ext/underscore/underscore'
  },  
  shim: {
    'jquery-ui' : { 
      exports: '$',
      deps: ['jquery']
    }   
  }
});

require([
  'app', '/js/phoenix.js'
], function(App, Phoenix) {
    new Phoenix.initialize();

});
