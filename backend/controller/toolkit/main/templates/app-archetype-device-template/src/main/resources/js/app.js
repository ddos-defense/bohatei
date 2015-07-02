// Filename: app.js

define([
  // These are path alias that we configured in our bootstrap
  'jquery',     				// lib/jquery/jquery
  'underscore', 				// lib/underscore/underscore
  'backbone',    				// lib/backbone/backbone
  '/device/js/views/DeviceView.js'	// app
], function($, _, Backbone, DeviceView){
  var initialize = function() {
    var deviceView = new DeviceView(); // this calls initialize which in turn calls render
  }

  return {
    initialize : initialize
  };
});
