//Filename: app.js

define
([
 // These are path alias that we configured in our bootstrap
 'jquery',
 'jquery-ui',
 'underscore',
 ], function($, _) 
 { 
   // ajax settings
   $.ajaxSetup({
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

   var controller_uri = "http://127.0.0.1:8080";

   var switchfunction = "";


   var nodelist = [];

   var mactable =  {
     findNode: function (nodename) 
     {
       var ret = [];
       if (!mactable.obj) {
         return undefined;
       }

       nodelist = [];
       $.each(mactable.obj.table, function(i, entry) {
         if (entry.node) {
           var nodenm =  entry.node.type + "|" + entry.node.id;
           nodelist.push(nodenm);
           if (nodenm == nodename) {
             $.each(entry.table, function(ii, elem) {
               var x = {};
               x.mac = elem.first;
               x.nodeconnector = elem.second.nodeconnector.type + "|" + elem.second.nodeconnector.id;
               ret.push(x);
             });
           }
         }
       });
       return ret;
     },

     obj: undefined
   };

   var flowtable = {
     findNode:  function(nodename) 
     {
       var ret = [];
       if (!flowtable.obj) {
         return undefined;
       }

       nodelist = [];
       $.each(flowtable.obj, function(i, entry) {
         if (entry.node)  {

           nodelist.push(entry.node);

           if (entry.node == nodename) {
             $.each(entry.flows, function(ii, flow) {
               ret.push(flow);
             });
           }
         }
       });
       return ret;
     },
     obj: undefined
   };

   function populateFlowTable(data) {

     var tbody = $('#flowtable tbody');
     var rows = $('#flowtable tbody > tr');
     rows.remove();

     var selectednode = $("#nodeselect option:selected").val();
     var data = flowtable.findNode(selectednode);
     console.log("flow table data: " + JSON.stringify(data));

     $.each(data, function(i, elem) {
       var tr = $(document.createElement('tr'));
       tr.append($(document.createElement('td')).append(i));
       tr.append($(document.createElement('td')).append(elem));
       tbody.append(tr);
       console.log("appending : " + tr);
     }); 
   }

   function populateMacTable(data) {
     var tbody = $('#mactable tbody');
     var rows = $('#mactable tbody > tr');
     rows.remove();

     var selectednode = $("#nodeselect option:selected").val();
     var data = mactable.findNode(selectednode);
     console.log("mac table data: " + JSON.stringify(data));

     $.each(data, function(i, elem) {
       var tr = $(document.createElement('tr'));

       tr.append($(document.createElement('td')).append(i));
       tr.append($(document.createElement('td')).append(elem.mac));
       tr.append($(document.createElement('td')).append(elem.nodeconnector));

       tbody.append(tr);
       console.log("appending : " + tr);
     }); 
   }

   // bind form submit
   //	$('button').click(function() {
   //		var simple = {}; 
   //		simple.foo = $('#foo').val();
   //		simple.bar = $('#bar').val();
   //		$.post('http://127.0.0.1:8080/simple/northbound/simple', JSON.stringify(simple), function(result) {
   //			console.log(result);
   //		});
   //	});

   function refreshNodeList() {
     var foo = flowtable.findNode("asdf"); // dummy to get node list to refresh
     var select = $("#nodeselect");
     var selectoptions = $("#nodeselect option");
     selectoptions.remove();
     $.each(nodelist, function(i, node) {
       select.append('<option value="' + node + '">' + node + '</option>');
     });
   }

   function updateMode() {
       $.getJSON(controller_uri + '/learningswitch/northbound/learningswitch/function', function(result) {
           console.log("Got data " + JSON.stringify(result)); 
           switchfunction = result.function;
           $("#mode").html(result.function); 
       });
   }

   $('#refreshNodeList').click(function() {
     // populate table
     $.getJSON(controller_uri + '/learningswitch/northbound/learningswitch/flowtable', function(result) {
       flowtable.obj = result;
       refreshNodeList();
       });
   });

   $('#refreshMacTable').click(function() {
     // populate table
     $.getJSON(controller_uri + '/learningswitch/northbound/learningswitch/table', function(result) {
       console.log("Got result: " + JSON.stringify(result));
       mactable.obj = result;
       populateMacTable();
       });
   });


   $('#refreshFlowTable').click(function() {
     // populate table
     $.getJSON(controller_uri + '/learningswitch/northbound/learningswitch/flowtable', function(result) {
       console.log("Got result: " + JSON.stringify(result));
       flowtable.obj = result;
       populateFlowTable();
       });
   });

   $('#clearFlowTable').click(function() {
     // populate table
     $.getJSON(controller_uri + '/learningswitch/northbound/learningswitch/flowtable', function(result) {
       console.log("Got result: " + JSON.stringify(result));
       flowtable.obj = result;
       populateFlowTable();
       });
   });

   $("#nodeselect").change(function() {
     populateMacTable();
     populateFlowTable();
   });

   $('#clearMacTable').click(function() {
     // populate table
     $.ajax({
       url: controller_uri + '/learningswitch/northbound/learningswitch/table',
       type: 'DELETE',
        });

     updateMode();
   });

   $('#togglemode').click(function() {
     $.ajax({
         type: 'PUT',
         url: controller_uri + '/learningswitch/northbound/learningswitch/function',
         contentType: 'application/json',
         data: {"function": (switchfunction.function == "hub" ? "switch" : "hub") }
     });

     updateMode();
   });

   $(updateMode());

});
