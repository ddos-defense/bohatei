var http = require('http'),
static = require('./static');

/* Server start */
var server = http.createServer(function(req, res) {
  static(req, res); // if no matches, then serve a static file
}).listen(8000);
