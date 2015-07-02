define(
  [
    'backbone',
    'underscore',
    '/dnsguard/web/js/models/SimpleModel.js'
    ], function(Backbone, _, SimpleModel) {
      var SimpleCollection = Backbone.Collection.extend({
        model : SimpleModel,
        url : '/dnsguard/northbound/dnsguard'
      });
      return SimpleCollection;
    });
