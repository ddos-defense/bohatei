define(
  [
    'jquery',
    'backbone',
    'underscore',
    '/learningswitch/web/js/models/FlowModel.js',
    '/learningswitch/web/js/collections/FlowCollection.js',
    '/js/ext/text/text.js!/learningswitch/web/js/templates/flowtemplate.html'
    ], function($, Backbone, _,  FlowCollection, FlowModel, Template) {
      var FlowTableView = Backbone.View.extend({
        el: $("#flowtablediv"),
        initialize: function() {
          var self = this;
            
          this.flowcollection = new FlowCollection();
          this.flowcollection.url = '/learningswitch/northbound/learningswitch/flowtable';
          this.flowcollection.fetch({
            success : function(call, response) {
              self.renderflowtable();
            }
          });
        },

        renderflowtable: function() {
            var that = this;
            var compiledTemplate = _.template(Template, 
            {
            	flowdata  : that.flowcollection.models
            });
            $(this.el).append($(compiledTemplate).html());
          },
        
        events : {
       	'click #simpleContainer button' : 'handleSimpleButton',
          'click #simpleTable tbody tr' : 'tableRowClicked'
        },
        
        handleSwitchHubToggle : function(evt) {
        	debugger;
        	var self = this;
            var $button = $(evt.currentTarget);
            if ($button.attr('id') == 'toggleButton') {
            	$.get( "/learningswitch/northbound/learningswitch/toggle", function( data ) {
            		  $("#toggleButton").val(data);
            		});
            }
        },
 
        updateView : function() {
          $('#flowContainer').remove();
          this.initialize();
        }
      });
      return FlowTableView;
    });
