var url = require('url'),
path = require('path'),
fs = require('fs'),
mime = require('mime'),
config = require('./config');

// functions
String.prototype.endsWith = function(suffix) {
  return this.indexOf(suffix, this.length - suffix.length) !== -1;
};

function returnFile(request, response, filename, mimetype) {
  var mimetyp = typeof mimetype !== 'undefined' ? mimetype : mime.lookup(filename);

  fs.readFile(filename, 'binary', function(err, file) {
    if (err) {
      console.log('500 ' + filename);
      response.writeHead(500, {'Content-Type': 'text/plain'});
      response.write(err + "\n");
      response.end();
      return;
    }

    console.log('200 ' + filename);
    response.writeHead(200, {
      'Content-Type': mimetyp,
      'Access-Control-Allow-Origin': '*',
    });

    response.write(file, 'binary');
    response.end();
  });
}

// Static
var Static = function(request, response) {
  var uri = url.parse(request.url).pathname;
  var cwd =  process.cwd();
  var found = false;

  if (uri.endsWith('/')) {
    if (uri == '/') {
      found = true;
      return returnFile(request, response, path.join(cwd, config.index), 'text/html');
    } else {
      uri += 'index.html';
    }
  }

  var fn = '';
  if (uri.indexOf('/' + config.appdir_to_remove) > -1) {
    uri = uri.replace('/' + config.appdir_to_remove, '');
    fn = path.join(path.join(cwd, config.apppath), uri);
  } else {
    fn = path.join(path.join(cwd, config.basepath), uri);
  }

  if (fs.existsSync(fn)) {
    return returnFile(request, response, fn);
  } else {
    console.log('404 ' + uri);
    response.writeHead(404, {'Content-Type': 'text/plain'});
    response.write('404 Not Found\n');
    response.end();
  }
}

module.exports = Static;
