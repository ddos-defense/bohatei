define(['backbone', 'underscore'], function(Backbone, _) {
  var SimpleModel = Backbone.Model.extend({
    idAttribute : 'uuid',
    defaults : {
      foo : '',
      bar : ''
    },
    initialize : function() {
    },
    setUrlRoot: function() {
      this.urlRoot = '/dnsguard/northbound/dnsguard';
    }
  });
  return SimpleModel;
});
