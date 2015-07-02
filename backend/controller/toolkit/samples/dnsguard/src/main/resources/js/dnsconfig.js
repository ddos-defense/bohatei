// Filename: main.js

require.config({
  paths: {
    "jquery": "/js/ext/jquery",
    "underscore": "/js/ext/underscore",
    "backbone": "/js/ext/backbone",
    "datatables": "/dnsguard/web/js/ext/datatables/media/js/jquery.dataTables",
    "d3": "/dnsguard/web/js/ext/d3/d3.min",
    "d3pie": "/dnsguard/web/js/d3pie/d3pie"
  },
  shim: {
      "d3pie": ['d3', 'jquery'],
      "datatables": ['jquery']
 }
});
require([
  'dns', '/js/phoenix.js'
], function(App, Phoenix) {
  new App.initialize();
  new Phoenix.initialize();
});
