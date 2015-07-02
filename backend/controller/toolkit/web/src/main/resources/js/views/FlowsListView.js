define(
    [
        'jquery',
        'backbone',
        'underscore',
        'collections/FlowsCollection',
        'models/FlowsModel',
        'views/FlowView',
        'ext/text/text!templates/flows.html'
    ], function($, Backbone, _, FlowsCollection, FlowsModel, FlowView, FlowsTemplate) {
    var FlowsView = Backbone.View.extend({
        el: $("#main"),
        initialize: function() {
            var self = this;
            this.collection = new FlowsCollection();
            this.collection.url = "/controller/nb/v2/flowprogrammer/default";
            this.collection.fetch({
                success: function(coll, response) {
                    self.render();
                }
            });
        },
        render: function() {
            var self = this;
            // populate collection with models
            var flowObjectsArr = self.collection.models[0].get('flowConfig');
            $(flowObjectsArr).each(function(index, flowObject) {
                var flowsModel = new FlowsModel({
                    id: flowObject.name,
                    installInHw: flowObject.installInHw,
                    name: flowObject.name,
                    node: {
                        id: flowObject.node.id,
                        type: flowObject.node.type
                    },
                    priority: flowObject.priority,
                    ingressPort: flowObject.ingressPort,
                    etherPort: flowObject.etherPort,
                    nwSrc: flowObject.nwSrc,
                    actions: flowObject.actions
                });
                self.collection.add(flowsModel);
            });
            var compiledTemplate = _.template(FlowsTemplate,
                {
            		flows: self.collection.models[0].get('flowConfig'),
            		actionsMap: self.actionsMap
            	});
            $(this.el).append($(compiledTemplate).html());
        },
        events: {
            "click div#flowsTableButtonsContainer button": "handleFlowCrud",
            "click div#flowsContainer table tbody tr": "tableRowClicked"
        },
        handleFlowCrud: function(evt) {
            var self = this;
            var $button = $(evt.currentTarget);
            if($button.attr("id") == "createFlowButton") {
                self.flowView = self.flowView || new FlowView();
                self.flowView.parentListView = self;
                delete self.flowView.flowModel;
                self.flowView.render();
            } else if($button.attr("id") == "editFlowButton") {
            	self.flowView = self.flowView || new FlowView();
                self.flowView.parentListView = self;
                // get data for selected model
                var flowName = $("div#flowsContainer tbody tr.selectedrow").attr("data-flowName");
                var flowModel = self.collection.get(flowName);
                self.flowView.flowModel = flowModel;
                self.flowView.render();
            } else {
                // delete flow
                var id = $("div#flowsContainer tbody tr.selectedrow").attr("data-flowName");
                var flowModel = self.collection.get(id);
                flowModel.setUrlRoot();
                flowModel.destroy({
                    dataType: "text",
                    success: function() {
                        console.log("delete succeeded!");
                        $("#flowFormContainer").remove();
                        self.updateView();
                    },
                    error: function() {
                        console.log("delete error callback called");
                        $("#flowFormContainer").remove();
                        self.updateView();
                    }
                });
            }
        },
        tableRowClicked: function(evt) {
            $("div#flowsContainer tbody tr.selectedrow").removeClass("selectedrow");
            var $tr = $(evt.currentTarget);
            $tr.addClass("selectedrow");
        },
        updateView: function() {
            $("#flowsContainer").remove();
            this.initialize();
        },
        /*
         * temporary map of actions
         */
        actionsMap: {
        	"DROP" : "Drop",
            "LOOPBACK" : "Loopback",
            "FLOOD" : "Flood",
            "SW_PATH" : "Software Path",
            "HW_PATH" : "Hardware Path",
            "CONTROLLER" : "Controller",
            "OUTPUT" : "Add Output Ports",
            "SET_VLAN_ID" : "Set VLAN ID",
            "SET_VLAN_PCP" : "Set VLAN Priority",
            "POP_VLAN" : "Strip VLAN Header",
            "SET_DL_SRC" : "Modify Datalayer Source Address",
            "SET_DL_DST" : "Modify Datalayer Destination Address",
            "SET_NW_SRC" : "Modify Network Source Address",
            "SET_NW_DST" :"Modify Network Destination Address",
            "SET_NW_TOS" : "Modify ToS Bits",
            "SET_TP_SRC" : "Modify Transport Source Port",
            "SET_TP_DST" : "Modify Transport Destination Port"
        }
    });
    return FlowsView;
});
