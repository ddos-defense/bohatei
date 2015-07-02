define(
    [
        'jquery',
        'backbone',
        'underscore',
        '/app/js/collections/DevicesCollection.js',
        '/app/js/models/DeviceModel.js',
        '/js/ext/text/text.js!/app/js/templates/device.html'
    ], function($, Backbone, _, DevicesCollection, DeviceModel, DeviceTemplate) {

    var DeviceView = Backbone.View.extend({
        el: $("#main"),
        render: function() {
            console.log("DevicesView initialize called");
            this.collection = new DevicesCollection();
            this.deviceModel = new DeviceModel({
                'name': 'device1',
                'ipAddress': '1.2.3.4'
            });
            this.collection.add(this.deviceModel);
            var templateVariables = {
                'device_name': this.deviceModel.get('name'),
                'device_ip': this.deviceModel.get('ipAddress')
            };
            var compiledTemplate = _.template(DeviceTemplate, templateVariables);
            $(this.el).append(compiledTemplate);
        }
    });
    return DeviceView;
});
