define(['backbone','underscore','/learningswitch/web/js/models/FlowModel.js'], 
		function(Backbone, _, FlowModel) {
	var FlowCollection = Backbone.Collection.extend({
		model : FlowModel,
		url : '/learningswitch/northbound/learningswitch/flowtable',
		parse: function(data) {
			console.log("Received data" + data);
			
		}
			
	});
	return FlowCollection;
});
