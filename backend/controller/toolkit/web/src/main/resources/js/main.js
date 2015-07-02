// Filename: main.js

require.config({
  paths: {
    "jquery": "/js/ext/jquery",
    "underscore": "/js/ext/underscore",
    "backbone": "/js/ext/backbone",
    "models": "/js/models",
    "views": "/js/views",
    "collections": "/js/collections"
  }
});
require([
  'app', '/js/phoenix.js'
], function(App, Phoenix) {
  new App.initialize();
  new Phoenix.initialize();
});
