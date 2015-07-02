define(
  [
    'backbone',
    'underscore',
    '/simple/web/js/models/SimpleModel.js'
    ], function(Backbone, _, SimpleModel) {
      var SimpleCollection = Backbone.Collection.extend({
        model : SimpleModel,
        url : '/simple/northbound/simple'
      });
      return SimpleCollection;
    });
