// Filename: app.js

define([
  // These are path alias that we configured in our bootstrap
  'jquery',
  'jquery-ui',
  'underscore'
], function($, _){
  // ajax settings
  $.ajaxSetup({
    type: 'POST',
    data: {},
    dataType: 'json',
    xhrFields: {
      withCredentials: true
    },
    crossDomain: true,
    success: 'callback',
    headers: {
      'Accept': 'application/json',
      'Content-Type': 'application/json'
    }
  });

  function populateTable(data) {
    var $tbody = $('table tbody');
    $.each(data, function(idx, d) {
      var $tr = $(document.createElement('tr'));
      var $uuid = $(document.createElement('td')).append(d.uuid);
      var $foo = $(document.createElement('td')).append(d.foo);
      var $bar = $(document.createElement('td')).append(d.bar);
      $tr.append($uuid).append($foo).append($bar);
      $tbody.append($tr);
    });
  }

  // bind form submit
  $('button').click(function() {
    var simple = {};
    simple.foo = $('#foo').val();
    simple.bar = $('#bar').val();
    $.post('http://localhost:8080/simple/northbound/simple', JSON.stringify(simple), function(result) {
      console.log(result);
    });
  });

  // populate table
  $.getJSON('http://localhost:8080/simple/northbound/simple', function(result) {
    populateTable(result);
  });

});
