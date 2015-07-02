var Routing = function() {
  var _routes = [];
  var _roots = [];
  var _addRoute = function(url, action) {
    var route = {
      Url: url,
      Action: action
    };
    _routes.push(route);
  };
  var _addRoot = function(root, action) {
    var root = {
      Root: root,
      Action: action
    };
    _roots.push(root);
  };
  var _process = function(req, res) {
    var route = _getRoute(req);
    if (route && route.Action) {
      route.Action(req, res);
      return true;
    }
    var root = _getRoot(req);
    if (root && root.Action) {
      root.Action(req, res);
      return true;
    }
    return false;
  };
  var _getRoute = function(req) {
    for(var i = 0, length = _routes.length; i < length; i++) {
      if (req.url === _routes[i].Url) {
        return _routes[i];
      }
    }
  };
  var _getRoot = function(req) {
    var elements = req.url.split('/');
    if (elements.length > 1) {
      var root = elements[1];
      for(var i = 0, length = _roots.length; i < length; i++) {
        if (root === _roots[i].Root) {
          return _roots[i];
        }
      }
    }
  };
  return {
    AddRoute: _addRoute,
    AddRoot: _addRoot,
    Process: _process
  };
}();

module.exports = Routing;
