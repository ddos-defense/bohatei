define(['backbone', 'underscore'], function(Backbone, _) {
  var FlowModel = Backbone.Model.extend({
    idAttribute : 'node',
    defaults : {
      node : '',
      flows : new Array()
    },
    initialize : function() {
    },
    setUrlRoot: function() {
      this.urlRoot = '/learningswitch/northbound/learningswitch/flowtable';
    }
  });
  return FlowModel;
});


