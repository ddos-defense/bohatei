define(
    [
        'jquery',
        'backbone',
        'underscore',
        'collections/DevicesCollection',
        'models/DeviceModel',
        'views/DevicePropertiesView',
        'ext/text/text!templates/device.html'
    ], function($, Backbone, _, DevicesCollection, DeviceModel, DevicePropertiesView, DeviceTemplate) {
    var DeviceView = Backbone.View.extend({
        el: $("#main"),
        initialize: function() {
            var self = this;
            this.collection = new DevicesCollection();
            this.collection.url = "/controller/nb/v2/switchmanager/default/nodes";
            this.collection.fetch({
                success: function(coll, response) {
                    console.log("passed collection call to get devices", response);
                    self.render();
                }
            });
        },
        render: function() {
            console.log("DevicesView initialize called");
            var that = this;
            var compiledTemplate = _.template(DeviceTemplate, {devices: that.collection.models[0].get('nodeProperties')});
            $(this.el).append($(compiledTemplate).html());
        },
        events: {
            "click tr": "getNodeProperties"
        },
        getNodeProperties: function(evt) {
            var $tr = $(evt.currentTarget);
            var devicePropsView = new DevicePropertiesView();
            devicePropsView.nodeId = $tr.attr("data-nodeId");
            devicePropsView.render();
        }
    });
    return DeviceView;
});
