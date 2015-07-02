// Filename: main.js

require.config({
  paths: {
    "jquery": "/js/ext/jquery/dist/jquery.min",
    "underscore": "/js/ext/underscore/underscore",
    "backbone": "/js/ext/backbone/backbone",
    "models": "/device/js/models", 			// app
    "views": "/device/js/views", 				// app
    "collections": "/device/js/collections" 	// app
  }
});
require([
  'app', '/js/phoenix.js'
], function(App, Phoenix) {
  new App.initialize();
  new Phoenix.initialize();
});
