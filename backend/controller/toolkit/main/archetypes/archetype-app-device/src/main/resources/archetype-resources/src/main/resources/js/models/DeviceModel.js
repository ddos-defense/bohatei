define(['backbone', 'underscore'], function(Backbone, _) {
  var DeviceModel = Backbone.Model.extend({
    defaults: {
      name: "New device"
    },
    initialize: function() {
      // initialize of DeviceModel called
    }
  });
  return DeviceModel;
});
