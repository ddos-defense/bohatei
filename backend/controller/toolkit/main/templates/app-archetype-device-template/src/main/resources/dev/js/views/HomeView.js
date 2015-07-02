define([
  'jquery',
  'backbone',
  'underscore',
  'ext/text!templates/home.html'
  ], function($, Backbone, _, HomeTemplate) {
    var HomeView = Backbone.View.extend({
      el: $("#main"),
      initialize: function() {
        var self = this;
        self.render();
      },
      render: function() {
        var self = this;
        var compiledTemplate = _.template(HomeTemplate,{});
        $(self.el).append($(compiledTemplate).html());
      }
    });
    return HomeView;
  });
