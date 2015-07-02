// Filename: app.js

define([
  // These are path alias that we configured in our bootstrap
  'jquery',     				// lib/jquery/jquery
  'underscore', 				// lib/underscore/underscore
  'backbone',    				// lib/backbone/backbone
  '/app/js/views/DeviceView.js'	// app
], function($, _, Backbone, DeviceView){
  var initialize = function() {
    console.log('init');
    // load apps into menu
    $.getJSON('/web.json', function(apps) {
      console.log(apps);
      $.each(apps, function(key, app) {
        var $li = $(document.createElement('li'));
        var $a = $(document.createElement('a'));
        $a.append(app.name).attr('href', '#');
        //$a.attr('href', '/'+key);
        $a.click(function() {
          $(this).closest('ul').find('li').removeClass('pure-menu-selected');
          $(this).closest('li').addClass('pure-menu-selected');
          $('#main').load('/'+key);
        });
        $li.append($a);

        $('#menu ul').append($li);
      });
    });
    $('#main').text('OpenDaylight Phoenix App');
    $('#menu .pure-menu-selected').click(function() {
      $(this).closest('ul').find('li').removeClass('pure-menu-selected');
      $(this).closest('li').addClass('pure-menu-selected');
      $('#main').empty();
      $('#main').text('OpenDaylight Phoenix');
    });

    // test backbone
    new DeviceView().render();
  }

  return {
    initialize : initialize
  };
});

