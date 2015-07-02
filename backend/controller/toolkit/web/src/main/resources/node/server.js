var http = require('http'),
routing = require('./routing'),
relay = require('./relay'),
static = require('./static');

/* Routing (Route or Root) */

//routing.AddRoute('/simple', relay);
routing.AddRoot('controller', relay);
routing.AddRoot('web.json', relay);

/* Server start */
var server = http.createServer(function(req, res) {
  if (routing.Process(req, res) === false) {
    static(req, res); // if no matches, then serve a static file
  }
}).listen(8000);
