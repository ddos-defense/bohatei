#set( $symbol_dollar = '$' )
define(
  [
    'backbone',
    'underscore',
    '/${artifactId}/web/js/models/SimpleModel.js'
    ], function(Backbone, _, SimpleModel) {
      var SimpleCollection = Backbone.Collection.extend({
        model : SimpleModel,
        url : '/${artifactId}/northbound/${artifactId}'
      });
      return SimpleCollection;
    });
