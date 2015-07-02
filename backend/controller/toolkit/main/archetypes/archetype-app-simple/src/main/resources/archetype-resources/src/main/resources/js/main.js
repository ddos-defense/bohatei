#set( $symbol_dollar = '$' )
// Filename: main.js

require.config({
  paths: {
    "jquery": "/js/ext/jquery/dist/jquery.min",
    "underscore": "/js/ext/underscore/underscore",
    "backbone": "/js/ext/backbone/backbone",
    "models": "/${artifactId}/web/js/models", 			    // app
    "views": "/${artifactId}/web/js/views", 				    // app
    "collections": "/${artifactId}/web/js/collections" 	// app
  }
});
require([
  'app', '/js/phoenix.js'
], function(App, Phoenix) {
  new App.initialize();
  new Phoenix.initialize();
});
