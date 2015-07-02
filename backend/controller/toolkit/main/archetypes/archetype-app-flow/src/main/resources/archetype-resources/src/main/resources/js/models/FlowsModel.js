define(['backbone', 'underscore'], function(Backbone, _) {
  var FlowsModel = Backbone.Model.extend({
    defaults: {
      id: 'NewFlow',
      installInHw: "false",
      node: {
        id: '',
        type: 'OF'
      },
      ingressPort: "1",
      priority: "500",
      etherType: "0x800",
      nwSrc: "9.9.1.1",
      actions: ["OUTPUT=2"]
    },
    initialize: function() {
      console.log("FlowsModel initialize called");
    },
    setUrlRoot: function() {
      this.urlRoot="/controller/nb/v2/flowprogrammer/default/node/";
      this.urlRoot += this.get("node").type + "/";
      this.urlRoot += this.get("node").id + "/";
      this.urlRoot += "staticFlow/";
    }
  });
  return FlowsModel;
});
