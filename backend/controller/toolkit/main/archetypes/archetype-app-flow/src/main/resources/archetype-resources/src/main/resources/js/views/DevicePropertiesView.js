#set( $symbol_dollar = '$' )
define(
  [
    'jquery',
    'backbone',
    'underscore',
    '/${artifactId}/js/collections/DevicePropertiesCollection.js',
    '/js/ext/text/text.js!/${artifactId}/js/templates/deviceProperties.html'
    ], function($, Backbone, _, DevicePropertiesCollection, DevicePropertiesTemplate) {

      var DevicePropertiesView = Backbone.View.extend({
        el: $("#main"),
        initialize: function() {
          // initialize of DevicePropertiesView called
        },
        render: function() {
          var self = this;
          $(self.el).find("#deviceProperties").remove();
          this.collection = new DevicePropertiesCollection();
          this.collection.url = "/controller/nb/v2/switchmanager/default/node/OF/" + self.nodeId;
          this.collection.fetch({success: function(coll, response) {
            var compiledTemplate = _.template(DevicePropertiesTemplate, {
              deviceProps: response.nodeConnectorProperties,
              nodeId: self.nodeId
            });
            $(self.el).append($(compiledTemplate).html());
          }});
        }
      }); // DevicePropertiesView extend ends here
      return DevicePropertiesView;
    });
