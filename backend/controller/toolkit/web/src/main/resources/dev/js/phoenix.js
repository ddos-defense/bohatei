// Filename: phoenix.js

define([
  'jquery'
], function($) {
  var initialize = function() {
    // attach phoenix overlay bar
    var $overlay = $(document.createElement('div'))
    .attr('id', 'phoenix-overlay');
    var $brand = $(document.createElement('div'))
    .attr('id', 'phoenix-brand');
    var $menu = $(document.createElement('div'))
    .attr('id', 'phoenix-menu')
    .addClass('pure-menu pure-menu-open pure-menu-horizontal');
    var $ul = $(document.createElement('ul'));
    var $li = $(document.createElement('li'));
    var $a = $(document.createElement('a'))
    .attr('href', '/')
    .append('Home');
    $li.append($a);
    $ul.append($li);
    $menu.append($ul);

    $overlay.append($brand).append($menu);
    $('body').prepend($overlay);

    // determine which app we are in
    var path = window.location.pathname;
    path = path.split('/');
    var current = undefined;
    if (path.length > 1) {
      if (path[1] === '') {
        $li.addClass('pure-menu-selected');
      } else {
        current = path[1];
      }
    }

    // load apps into menu
    $.getJSON('/web.json', function(apps) {
      $.each(apps, function(key, app) {
        var $li = $(document.createElement('li'));
        if (key === current) {
          $li.addClass('pure-menu-selected');
        }
        var $a = $(document.createElement('a'));
        $a.append(app.name).attr('href', '/'+key+'/web/');
        $li.append($a);

        $('#phoenix-menu ul').append($li);
      });
    });
  }

  return {
    initialize : initialize
  };
});
