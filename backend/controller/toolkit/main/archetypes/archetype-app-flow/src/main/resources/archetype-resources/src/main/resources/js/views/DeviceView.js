#set( $symbol_dollar = '$' )
define(
  [
    'jquery',
    'backbone',
    'underscore',
    '/${artifactId}/js/collections/DevicesCollection.js',
    '/${artifactId}/js/models/DeviceModel.js',
    '/${artifactId}/js/views/DevicePropertiesView.js',
    '/js/ext/text/text.js!/${artifactId}/js/templates/device.html'
    ], function($, Backbone, _, DevicesCollection, DeviceModel, DevicePropertiesView, DeviceTemplate) {
      var DeviceView = Backbone.View.extend({
        el: $("#main"),
        initialize: function() {
          var self = this;
          this.collection = new DevicesCollection();
          this.collection.url = "/controller/nb/v2/switchmanager/default/nodes";
          this.collection.fetch({
            success: function(coll, response) {
              // pass collection call to get devices
              self.render();
            }
          });
        },
        render: function() {
          // DevicesView initialize call
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
