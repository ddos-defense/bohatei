define(['backbone','underscore','/app/js/models/DeviceModel.js'], function(Backbone, _, DeviceModel) {
	var DevicesCollection = Backbone.Collection.extend({
		model: DeviceModel
	});
	return DevicesCollection;
});
