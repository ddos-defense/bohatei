#set( $symbol_dollar = '$' )
// Filename: main.js

require.config({
  paths: {
    "jquery": "/js/ext/jquery/dist/jquery.min",
    "underscore": "/js/ext/underscore/underscore",
    "backbone": "/js/ext/backbone/backbone",
    "models": "/${artifactId}/js/models", 			    // app
    "views": "/${artifactId}/js/views", 				    // app
    "collections": "/${artifactId}/js/collections" 	// app
  }
});
require([
  'app', '/js/phoenix.js'
], function(App, Phoenix) {
  new App.initialize();
  new Phoenix.initialize();
});
