#set( $symbol_dollar = '$' )

define(
  [
    'jquery',
    'backbone',
    'underscore',
    '/${artifactId}/js/models/FlowsModel.js',
    '/${artifactId}/js/collections/DevicesCollection.js',
    '/js/ext/text/text.js!/${artifactId}/js/templates/flow.html'
    ], function($, Backbone, _, FlowsModel, DevicesCollection, FlowTemplate) {
      var FlowView = Backbone.View.extend({
        el: $("#main"),
        initialize: function() {
        },
        render: function() {
          // remove any existing form
          $("#flowFormContainer").remove();
          var self = this;
          var devicesCollection = new DevicesCollection();
          devicesCollection.fetch({
            success: function(coll, response) {
              var flowModel = self.flowModel;
              var flowAction = "Edit";
              if(!flowModel) {
                flowAction = "Create";
                flowModel = {
                  get: function(arg1) {
                    return "";
                  }
                };
              }
              var compiledTemplate = _.template(FlowTemplate, 
                {
                  "flowAction": flowAction,
                  "devices": response.nodeProperties,
                  "flowModel": flowModel
                });
                $(self.el).append($(compiledTemplate).html());
                // hack to set the actions value in case of edit
                if(flowAction == "Edit") {
                  $("#actions option[value='" + flowModel.get('actions') + "']")[0].selected =  true;
                }
            }
          });
        },
        events: {
          "click div#flowFormButtonsContainer button": "handleFlowFormButtons"
        },
        handleFlowFormButtons: function(evt) {
          var self = this;
          var $button = $(evt.currentTarget);
          if($button.attr("id") == "saveFlowButton") {
            // create FlowModel and save it.
            var flowModel = new FlowsModel({
              id: $("#flowName").val(),
              installInHw: "true",
              name: $("#flowName").val(),
              node: {
                id: $("#nodeId").val(),
                type: "OF"
              },
              ingressPort: $("#ingressPort").val(),
              priority: $("#priority").val(),
              etherType: $("#etherType").val(),
              nwSrc: $("#nwSrc").val(),
              actions: [$("#actions").val()]
            });
            flowModel.urlRoot="/controller/nb/v2/flowprogrammer/default/node/OF/";
            flowModel.urlRoot += $("#nodeId").val() + "/";
            flowModel.urlRoot += "staticFlow/";
            flowModel.save(null, {
              // REST call does not return JSON. so we need this, else 
                // the success callback wont get called
              dataType: "text",
              success: function(model, response) {
                console.log("Flow Created.");
                $("#flowFormContainer").remove();
                self.parentListView.updateView();
              }
            });

          } else {
            // cancel button
            $("#flowFormContainer").remove();
          }
        }
      });
      return FlowView;
    });
