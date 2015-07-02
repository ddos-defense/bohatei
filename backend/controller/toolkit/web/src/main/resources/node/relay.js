var http = require('http');

var Relay = function(req, res) {
  //res.writeHead(200, {'content-type': 'text/plain'});
  //res.end('Simple');
  var options = {
    headers : req.headers,
    method : req.method,
    port : '8080',
    //path : '/controller/nb/v2/switchmanager/default/nodes'
    path : req.url
  };
  var request = http.request(options, function(response) {
    var str = '';
    response.on('data', function(chunk) {
      str += chunk;
    });
    response.on('end', function() {
      res.writeHead(response.statusCode, response.headers);
      res.end(str);
    });
  });
  request.end();
}

module.exports = Relay;
