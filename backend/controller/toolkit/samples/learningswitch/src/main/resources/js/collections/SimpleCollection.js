define(
  [
    'backbone',
    'underscore',
    '/learningswitch/web/js/models/SimpleModel.js'
    ], function(Backbone, _, SimpleModel) {
      var SimpleCollection = Backbone.Collection.extend({
        model : SimpleModel,
        url : '/learningswitch/northbound/learningswitch/mactable'
      });
      return SimpleCollection;
    });
