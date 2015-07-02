define([ 'backbone', 'underscore', 'models/FlowsModel' ], function(Backbone, _,
        FlowsModel) {
    var FlowsCollection = Backbone.Collection.extend({
        model : FlowsModel
    });
    return FlowsCollection;
});
