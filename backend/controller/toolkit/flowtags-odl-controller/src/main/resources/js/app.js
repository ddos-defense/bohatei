// Filename: app.js

define([
  // These are path alias that we configured in our bootstrap
  'jquery',     				// toolkit.web
  'underscore', 				// toolkit.web
  'backbone',    				// toolkit.web
  'datatables',    				// dnsguard
  'd3',		    				// dnsguard
  'd3pie'
], function($, _, Backbone, DataTables, d3){
  
	
  $(document).ready(function () {
	    //This function is called once the DOM is ready.
	    //It will be safe to query the DOM and manipulate
	    //DOM nodes in this function.
 
  });
	
  var initialize = function() {
    //var view = new View(); // this calls initialize which in turn calls render
  }

  return {
    initialize : initialize
  };
  
  
});
