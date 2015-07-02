#set( $symbol_dollar = '$' )
// Filename: app.js

define([
  // These are path alias that we configured in our bootstrap
  'jquery',     				// lib/jquery/jquery
  'underscore', 				// lib/underscore/underscore
  'backbone',    				// lib/backbone/backbone
  '/${artifactId}/js/views/FlowsListView.js'	// app
], function($, _, Backbone, FlowsListView){
  var initialize = function() {
    var flowsListView = new FlowsListView(); // this calls initialize which in turn calls render
  }

  return {
    initialize : initialize
  };
});
