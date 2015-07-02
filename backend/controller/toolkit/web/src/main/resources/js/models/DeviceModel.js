define(['backbone', 'underscore'], function(Backbone, _) {
    var DeviceModel = Backbone.Model.extend({
        defaults: {
        	name: "New device"
        },
        initialize: function() {
        	console.log("initialize of DeviceModel called");
        }
	});
	return DeviceModel;
});
