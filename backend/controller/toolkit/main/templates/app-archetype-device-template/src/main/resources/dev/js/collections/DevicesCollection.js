define([ 'backbone', 'underscore', 'models/DeviceModel' ], function(Backbone,
		_, DeviceModel) {
	var DevicesCollection = Backbone.Collection.extend({
		model : DeviceModel,
		url : "/controller/nb/v2/switchmanager/default/nodes"
	});
	return DevicesCollection;
});
