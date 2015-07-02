define(['backbone', 'underscore'], function(Backbone, _) {
  var SimpleModel = Backbone.Model.extend({
    idAttribute : 'mac',
    defaults : {
      mac : '',
      nodeconnector : ''
    },
    initialize : function() {
    },
    setUrlRoot: function() {
      this.urlRoot = '/learningswitch/northbound/learningswitch/mactable';
    }
  });
  return SimpleModel;
});
