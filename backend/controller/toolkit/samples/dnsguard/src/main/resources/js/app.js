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

		var localdns = $.getJSON( "/dnsguard/northbound/getlocaldns", function() {
			console.log( "success" );
		})
			.done(function( data ) {
			 
			$("#localdns h3:first").html("Local DNS Server: " +data);
			
			console.log( "second success" );
		})
			.fail(function() {
			console.log( "error" );
		})
			.always(function() {
			console.log( "complete" );
		});

		var externaldns = $.getJSON( "/dnsguard/northbound/getTopExternalDnsUsage", function() {
			console.log( "success" );
		}) 
			.done(function( data ) {
			
			// LC hack for d3
			window.d3 = d3;
			
			var pie = new d3pie("chart-resp-types", {
				"header": {
					"title": {
						"text": "Top 5 External Dns Servers",
						"fontSize": 24,
						"font": "open sans"
					},
					"subtitle": {
						"text": "Based on all time information",
						"color": "#999999",
						"fontSize": 12,
						"font": "open sans"
					},
					"titleSubtitlePadding": 9
				},
				"footer": {
					"color": "#999999",
					"fontSize": 10,
					"font": "open sans",
					"location": "bottom-left"
				},
				"size": {
					"canvasWidth": 590,
					"pieInnerRadius": "2%",
					"pieOuterRadius": "90%"
				},
				"data": data,
				"labels": {
					"outer": {
						"pieDistance": 32
					},
					"inner": {
						"hideWhenLessThanPercentage": 3
					},
					"mainLabel": {
						"fontSize": 11
					},
					"percentage": {
						"color": "#ffffff",
						"decimalPlaces": 0
					},
					"value": {
						"color": "#adadad",
						"fontSize": 11
					},
					"lines": {
						"enabled": true
					}
				},
				"effects": {
					"pullOutSegmentOnClick": {
						"effect": "linear",
						"speed": 400,
						"size": 8
					}
				},
				"misc": {
					"gradient": {
						"enabled": true,
						"percentage": 100
					}
				}
			});
			 
		})
			.fail(function() {
			console.log( "error" );
		})
			.always(function() {
			console.log( "complete" );
		});
		
		
		$('#violators-table').dataTable( {
			  "bInfo": false,
			  "deferRender": true,
			  "ajax": "/dnsguard/northbound/getviolators/",
		      "columns": [
		                    { "data": "ip" },
		                    { "data": "timestamp" }
		                 ]
		} );
		
		// working version not using serverside
		  $('#records-table').dataTable( {
	          "deferRender": true,
			  "ajax": "/dnsguard/northbound/getrecords/",
		      "columns": [
		                    { "data": "dstIp" },
		                    { "data": "srcIp" },
		                    { "data": "request" },
		                    { "data": "respType" },
		                    { "data": "data" },
		                    { "data": "timestamp" }
		                 ]
		} ); 
		
		/* work in progress (server side manipulating)
		$('#records-table').dataTable( {
	          "processing": true,
	          "serverSide": true,
			  "deferRender": true,
			  "ajax": {
				  "url": "/dnsguard/northbound/findrecords/",
				  "type": "POST"
					  },
		      "columns": [
		                    { "data": "dstIp" },
		                    { "data": "srcIp" },
		                    { "data": "request" },
		                    { "data": "respType" },
		                    { "data": "data" },
		                    { "data": "timestamp" }
		                 ]
		} );
		*/ 
  });
	
  var initialize = function() {
    //var view = new View(); // this calls initialize which in turn calls render
  }

  return {
    initialize : initialize
  };
  
  
});
