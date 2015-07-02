// Filename: main.js

require.config({
  paths: {
    "jquery": "/js/ext/jquery/dist/jquery.min",
    "underscore": "/js/ext/underscore/underscore",
    "backbone": "/js/ext/backbone/backbone",
    "models": "/app/js/models", 			// app
    "views": "/app/js/views", 				// app
    "collections": "/app/js/collections" 	// app
  }
});
require([
  'app'
], function(App) {
  new App.initialize();
});
