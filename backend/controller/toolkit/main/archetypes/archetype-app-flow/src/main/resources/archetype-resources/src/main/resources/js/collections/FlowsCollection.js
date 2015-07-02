#set( $symbol_dollar = '$' )

define([ 'backbone', 'underscore', '/${artifactId}/js/models/FlowsModel.js' ], function(Backbone, _,
FlowsModel) {
  var FlowsCollection = Backbone.Collection.extend({
    model : FlowsModel
  });
  return FlowsCollection;
});
