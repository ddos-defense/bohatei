$('document').ready(function() {
  $.getJSON('/app/visual.json', function(data) { // TODO hard-coded
    $('#topology').width(960);
    $('#topology').height(500);
    phoenix.topology.build(data, '#topology');
  });
});
