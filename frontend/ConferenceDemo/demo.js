$(function(){
	if (window.File && window.FileReader && window.FileList && window.Blob) {
	// Great success! All the File APIs are supported.
	} else {
		alert('The File APIs are not fully supported in this browser.');
	}

	document.getElementById('files').addEventListener('change', handleFileSelect, false);

    $('#syn_form').submit(function() {
        visualize_syn();
        $.post("action.php",{attack: "syn_flood"}, function(reply) { 
            var loaderInterval = setInterval(function() {
                $.get('load.php', function(data) {
                    //alert(data);
                    if (data.trim() == "YES") {
                        $("<li/>").appendTo("#graphs ul").html('<img class="graph" src="output.png">');
                        clearInterval(loaderInterval);
                    }         
                 });
            },2500); 
        });
        return false;
    });

    $('#udp_form').submit(function() {
        visualize_udp();
        $.post( "action.php",{attack: "udp_flood"}, function(reply){
            var loaderInterval = setInterval(function() {
                $.get('load_udp.php', function(data) {
                    //alert(data);
                    if (data.trim() == "YES") {
                        $("<li/>").appendTo("#graphs ul").html('<img class="graph" src="udp_output.png">');
                        clearInterval(loaderInterval);
                    }         
                 });
            },2500);         
        });
        return false;
    });

    $('#dns_form').submit(function() {
        visualize_dns();
        $.post( "action.php",{attack: "dns_amp"}, function(reply){  
            var loaderInterval = setInterval(function() {
                $.get('load_dns.php', function(data) {
                    //alert(data);
                    if (data.trim() == "YES") {
                        $("<li/>").appendTo("#graphs ul").html('<img class="graph" src="dns_output.png">');
                        clearInterval(loaderInterval);
                    }         
                 });
            } ,2500); 
        });
        return false;
    });

    $('#ele_form').submit(function() {
        visualize_ele();
        $.post( "action.php",{attack: "ele_flow"}, function(reply){
            var loaderInterval = setInterval(function() {
                $.get('load_ele.php', function(data) {
                    //alert(data);
                    if (data.trim() == "YES") {
                        $("<li/>").appendTo("#graphs ul").html('<img class="graph" src="ele_output.png">');
                        clearInterval(loaderInterval);
                    }         
                 });
            } ,2500);  
        });
        return false;
        });

});

function visualize_syn() {
    var Interval = setInterval(function () {
        var edge1 = cy.getElementById('h1s2').css({'width':cy.getElementById('h1s2').width() + 0.5, 'line-color':'red'});
        var edge2 = cy.getElementById('s2s5').css({'width':cy.getElementById('s2s5').width() + 0.5, 'line-color':'red'});
        var edge3 = cy.getElementById('s5s6').css({'width':cy.getElementById('s5s6').width() + 0.5, 'line-color':'red'});
        var edge4 = cy.getElementById('h187s6').css({'width':cy.getElementById('h187s6').width() + 0.5, 'line-color':'red'});
        var attack_node = cy.getElementById('h187').css({'border-width':3, 'border-color':'red'});
        if (cy.getElementById('h187s6').width() > 4) {
           clearInterval(Interval);
           var attack_node = cy.getElementById('h187').css({'border-width':3, 'border-color':'red', 'background-image':'managerangry.png'});
        }
    }, 1000);
    var Timeout = setTimeout(function() { 
            var Interval2 = setInterval(function () {
            var edge1 = cy.getElementById('h1s2').css({'width':cy.getElementById('h1s2').width() - 0.5, 'line-color':'red'});
            var edge2 = cy.getElementById('s2s5').css({'width':cy.getElementById('s2s5').width() - 0.5, 'line-color':'red'});
            var edge3 = cy.getElementById('s5s6').css({'width':cy.getElementById('s5s6').width() - 0.5, 'line-color':'red'});
            var edge4 = cy.getElementById('h187s6').css({'width':cy.getElementById('h187s6').width() - 0.5, 'line-color':'red'});
            var attack_node = cy.getElementById('h187').css({'border-width':0, 'border-color':'red'});

            var n_edge1 = cy.getElementById('h1s2').css({'width':cy.getElementById('h1s2').width() + 0.5, 'line-color':'red'});
            var n_edge2 = cy.getElementById('s1s2').css({'width':cy.getElementById('s1s2').width() + 0.5, 'line-color':'red'});
            var n_edge3 = cy.getElementById('h21-d1s1').css({'width':cy.getElementById('h21-d1s1').width() + 0.5, 'line-color':'red'});
            var volume = document.getElementById('sflood_attack-1').value;
            if (volume=="High")
                 var n_edge4 = cy.getElementById('h31-d1s1').css({'width':cy.getElementById('h31-d1s1').width() + 0.5, 'line-color':'red'});
            if (cy.getElementById('h187s6').width() < 1.0) {
                var fw_node = cy.getElementById('h21-d1').css({'background-image':'red_firewall.png'});
                if (volume=="High")
                  var fw_node = cy.getElementById('h31-d1').css({'background-image':'red_firewall.png'});
                var edge2 = cy.getElementById('s2s5').css({'line-color':'grey'});
                var edge3 = cy.getElementById('s5s6').css({'line-color':'grey'});
                var edge4 = cy.getElementById('h187s6').css({'line-color':'grey'});
                var attack_node = cy.getElementById('h187').css({'background-image':'managerhappy.png','border-width':'4','border-color':'green'}); 
                clearInterval(Interval2);
                }  
            }, 1000);
    },25000);
}

function visualize_udp() {
    var Interval = setInterval(function () {
        var edge1 = cy.getElementById('h12s5').css({'width':cy.getElementById('h12s5').width() + 0.5, 'line-color':'orange'});
        var edge2 = cy.getElementById('s2s3').css({'width':cy.getElementById('s2s3').width() + 0.5, 'line-color':'orange'});
        var edge3 = cy.getElementById('s2s4').css({'width':cy.getElementById('s2s4').width() + 0.5, 'line-color':'orange'});
        var edge3 = cy.getElementById('s4s5').css({'width':cy.getElementById('s4s5').width() + 0.5, 'line-color':'orange'});
        var edge4 = cy.getElementById('h188s3').css({'width':cy.getElementById('h188s3').width() + 0.5, 'line-color':'orange'});
        var attack_node = cy.getElementById('h188').css({'border-width':3, 'border-color':'orange'});
        if (cy.getElementById('h188s3').width() > 4) {
           clearInterval(Interval);
           var attack_node = cy.getElementById('h188').css({'border-width':3, 'border-color':'orange', 'background-image':'managerangry.png'});
        }
    }, 1000);
    var Timeout = setTimeout(function() { 
            var Interval2 = setInterval(function () {
            var edge1 = cy.getElementById('s2s3').css({'width':cy.getElementById('s2s3').width() - 0.5, 'line-color':'orange'});
            var edge2 = cy.getElementById('s2s4').css({'width':cy.getElementById('s2s4').width() - 0.5, 'line-color':'orange'});
            var edge3 = cy.getElementById('s4s5').css({'width':cy.getElementById('s4s5').width() - 0.5, 'line-color':'orange'});
            var edge4 = cy.getElementById('h188s3').css({'width':cy.getElementById('h188s3').width() - 0.5, 'line-color':'orange'});
            var attack_node = cy.getElementById('h188').css({'border-width':0, 'border-color':'orange'});

            var n_edge3 = cy.getElementById('h41-d2s5').css({'width':cy.getElementById('h41-d2s5').width() + 0.5, 'line-color':'orange'});
            var volume = document.getElementById('uflood_attack-1').value;
            if (volume=="High")
                 var n_edge4 = cy.getElementById('h42-d2s5').css({'width':cy.getElementById('h42-d2s5').width() + 0.5, 'line-color':'orange'});

            if (cy.getElementById('h188s3').width() < 1.0) {
                var fw_node = cy.getElementById('h41-d2').css({'background-image':'orange_firewall.png'});
                if (volume=="High")
                  var fw_node = cy.getElementById('h42-d2').css({'background-image':'orange_firewall.png'});
                var edge2 = cy.getElementById('s2s3').css({'line-color':'grey'});
                var edge3 = cy.getElementById('s2s4').css({'line-color':'grey'});
                var edge2 = cy.getElementById('s4s5').css({'line-color':'grey'});
                var edge4 = cy.getElementById('h188s3').css({'line-color':'grey'});
                var attack_node = cy.getElementById('h188').css({'background-image':'managerhappy.png','border-width':'4','border-color':'green'}); 
                clearInterval(Interval2);
                }  
            }, 1000);
    },25000);
}

function visualize_dns() {
    var Interval = setInterval(function () {
        var edge1 = cy.getElementById('h2s8').css({'width':cy.getElementById('h2s8').width() + 0.5, 'line-color':'black'});
        var edge2 = cy.getElementById('s8s9').css({'width':cy.getElementById('s8s9').width() + 0.5, 'line-color':'black'});
        var edge3 = cy.getElementById('s9s3').css({'width':cy.getElementById('s9s3').width() + 0.5, 'line-color':'black'});
        var edge4 = cy.getElementById('h191s3').css({'width':cy.getElementById('h191s3').width() + 0.5, 'line-color':'black'});
        var attack_node = cy.getElementById('h191').css({'border-width':3, 'border-color':'black'});
        if (cy.getElementById('h191s3').width() > 4) {
           clearInterval(Interval);
           var attack_node = cy.getElementById('h191').css({'border-width':3, 'border-color':'black', 'background-image':'managerangry.png'});
        }
    }, 1000);
    var Timeout = setTimeout(function() { 
            var Interval2 = setInterval(function () {
            var edge1 = cy.getElementById('s9s3').css({'width':cy.getElementById('s9s3').width() - 0.5, 'line-color':'black'});
            var edge2 = cy.getElementById('s8s9').css({'width':cy.getElementById('s8s9').width() - 0.5, 'line-color':'black'});
            var edge4 = cy.getElementById('h191s3').css({'width':cy.getElementById('h191s3').width() - 0.5, 'line-color':'black'});
            var attack_node = cy.getElementById('h191').css({'border-width':0, 'border-color':'black'});

            var n_edge3 = cy.getElementById('h61-d5s7').css({'width':cy.getElementById('h61-d5s7').width() + 0.5, 'line-color':'black'});
            var n_edge4 = cy.getElementById('s7s8').css({'width':cy.getElementById('s7s8').width() + 0.5, 'line-color':'black'});
            var volume = document.getElementById('damp_attack-1').value;
            if (volume=="High")
                 var n_edge4 = cy.getElementById('h71-d5s7').css({'width':cy.getElementById('h71-d5s7').width() + 0.5, 'line-color':'black'});

            if (cy.getElementById('h191s3').width() < 1.0) {
                var fw_node = cy.getElementById('h61-d5').css({'background-image':'black_firewall.png'});
                if (volume=="High")
                    var fw_node = cy.getElementById('h71-d5').css({'background-image':'black_firewall.png'});
                var edge2 = cy.getElementById('s9s3').css({'line-color':'grey'});
                var edge3 = cy.getElementById('s8s9').css({'line-color':'grey'});
                var edge4 = cy.getElementById('h191s3').css({'line-color':'grey'});
                var attack_node = cy.getElementById('h191').css({'background-image':'managerhappy.png','border-width':'4','border-color':'green'}); 
                clearInterval(Interval2);
                }  
            }, 1000);
    },25000);
}


function visualize_ele() {
    var Interval = setInterval(function () {
        var edge1 = cy.getElementById('h11s3').css({'width':cy.getElementById('h11s3').width() + 0.5, 'line-color':'purple'});
        var edge2 = cy.getElementById('h189s3').css({'width':cy.getElementById('h189s3').width() + 0.5, 'line-color':'purple'});
        var attack_node = cy.getElementById('h189').css({'border-width':3, 'border-color':'purple'});
        if (cy.getElementById('h189s3').width() > 4) {
           clearInterval(Interval);
           var attack_node = cy.getElementById('h189').css({'border-width':3, 'border-color':'purple', 'background-image':'managerangry.png'});
        }
    }, 1000);
    var Timeout = setTimeout(function() { 
            var Interval2 = setInterval(function () {
            var edge4 = cy.getElementById('h189s3').css({'width':cy.getElementById('h189s3').width() - 0.5, 'line-color':'purple'});
            var attack_node = cy.getElementById('h189').css({'border-width':0, 'border-color':'purple'});
            var n_edge3 = cy.getElementById('h81-d3s3').css({'width':cy.getElementById('h81-d3s3').width() + 0.5, 'line-color':'purple'});
            var volume = document.getElementById('ele_attack-1').value;
            if (volume=="High")
                 var n_edge4 = cy.getElementById('h91-d3s3').css({'width':cy.getElementById('h91-d3s3').width() + 0.5, 'line-color':'purple'});
            
            if (cy.getElementById('h189s3').width() < 1.0) {
                var fw_node = cy.getElementById('h81-d3').css({'background-image':'purple_firewall.png'});
                if (volume=="High")
                    var fw_node = cy.getElementById('h91-d3').css({'background-image':'purple_firewall.png'});
                var edge4 = cy.getElementById('h189s3').css({'line-color':'grey'});
                var attack_node = cy.getElementById('h189').css({'background-image':'managerhappy.png','border-width':'4','border-color':'green'}); 
                clearInterval(Interval2);
                }  
            }, 1000);
    },25000);
}


function handleFileSelect(evt) {
    var files = evt.target.files; // FileList object
    // files is a FileList of File objects. List some properties.
    for (var i = 0, f; f = files[i]; i++) {
		var reader = new FileReader();
		reader.onload = (function(theFile) {
			return function(e) {
				var set = {};
				var nodes = [];
				var edges = [];
				var lines = e.target.result.split('\n');
				for (var j=0; j<lines.length-1; j++) {
					var elements = lines[j].split(' ');
					if (!(elements[0].trim() in set)) 
						set[elements[0]] = true;
                    if (!(elements[1].trim() in set))	
					       set[elements[1]] = true;
						
					var e2={};
					var e3={};
					if ((elements[0] == "undefined") || (elements[0] =="") || (elements[1] == "undefined") || (elements[1] ==""))
					   continue;
					e3['id'] = elements[0] + elements[1];
					e3['source'] = elements[0];
					e3['target'] = elements[1];
                    console.log(e2);
					e2['data'] = e3;
					edges.push(e2);
				}
				for (var keys in set) {
					if ((keys == "undefined") || (keys==""))
					  continue;
					var n2 ={};
					var n3 = {};
					var par = {};
					var par2 = {};
					if (keys.charAt(0) == 'h') 
                        if (keys.contains("-")) {
						    n3['id']=keys;
						    n3['parent'] = keys.split('-')[1].trim();
						    n2['data'] = n3;
						    par['id'] = keys.split('-')[1].trim();
						    par2['data'] = par;
						    nodes.push(par2);

				        } else {
						    n3['id']=keys;
						    n2['data'] = n3;
					    }
                    else {
                        n3['id']=keys;
                        n2['data'] = n3;
                    }
					nodes.push(n2);
				}
				
		    cy = cytoscape({
			container: document.getElementById('cy'),
			elements : {
				nodes: nodes,
				edges: edges,
				},				
			layout: {
				'name': 'cose',
				'fit':true,
				'gravity':'10',
                'nodeOverlap':'2'
			},
		});
		
		
		var collection = cy.nodes();
		for (var i=0; i< collection.length; i++)
		   if (collection[i].id().charAt(0) == "s") {
			collection[i].css({'shape':'round' ,
					   'background-fit':'contain',
					   'height':'50',
					   'width':'50',
					   'background-color':'white',
					   'padding':'0px 0px 0px 0px',
                       'background-image':'switch.jpg',
                       });
					   
		   } else if (collection[i].id().charAt(0) == "h") {
                  if (collection[i].id().contains("-"))     
                       collection[i].css({'shape':'roundrectangle' ,
										  'background-fit':'contain',
										  'height':'50',
                                          'width':'50',
										  'background-color':'white',
                                          'padding':'0px 0px 0px 0px',
                                          'background-image':'data-center.png',
                                        });
                    
                    else if (collection[i].id() == "h1" || collection[i].id() == "h2" || collection[i].id() == "h11" || collection[i].id() == "h12" )
                        collection[i].css({'shape':'roundrectangle' ,
                                           'background-fit':'contain',
                                           'height':'40',
                                           'width':'40',
                                           'background-color':'white',
                                           'padding':'0px 0px 0px 0px',
                                           'background-image':'Attacker.png',
                                        });

                    else if (collection[i].id() == "h187" || collection[i].id() == "h188" || collection[i].id() == "h189" || collection[i].id() == "h191" )
                        collection[i].css({'shape':'roundrectangle' ,
                                           'background-fit':'contain',
                                           'height':'100',
                                           'width':'100',
                                           'content' :'Customer',
                                           'text-valign':'bottom',
                                           'text-halign':'center',
                                           'background-color':'white',
                                           'padding':'0px 0px 0px 0px',
                                           'background-image':'managerhappy.png',
                                           'border-width':5,
                                           'border-color':'green',
                        });


		   }
           /*var attack_node = cy.getElementById('h187').css({'border-width':4, 'border-color':'green'});
           var attack_node = cy.getElementById('h188').css({'border-width':4, 'border-color':'green'});
           var attack_node = cy.getElementById('h189').css({'border-width':4, 'border-color':'green'}); 
           var attack_node = cy.getElementById('h191').css({'border-width':4, 'border-color':'green'}); */
		};
	})(f);
	  
      reader.readAsText(f);
    }
  }
 
