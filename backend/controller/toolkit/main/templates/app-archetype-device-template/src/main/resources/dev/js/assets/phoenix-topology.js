phoenix = {}

phoenix.topology = {
  graph : {},
  canvas : undefined,
  width : undefined,
  height : undefined,
}

phoenix.topology.init = function(data, canvas) {
  phoenix.topology.graph = data;
  phoenix.topology.canvas = canvas ;
  phoenix.topology.width = $(canvas).width() 
  phoenix.topology.height = $(canvas).height()
}

phoenix.topology.build = function(data, canvas){
  phoenix.topology.init(data, canvas);
  var color = d3.scale.category20();

  var force = d3.layout.force()
  .charge(-120)
  .linkDistance(200)
  .size([phoenix.topology.width, phoenix.topology.height ]);

  var svg = d3.select(phoenix.topology.canvas).append("svg")
  .attr("width", phoenix.topology.width)
  .attr("height", phoenix.topology.height);

  force
  .nodes(phoenix.topology.graph.nodes)
  .links(phoenix.topology.graph.links)
  .size([phoenix.topology.width, phoenix.topology.height])
  .start();

  var link = svg.selectAll(".link")
  .data(phoenix.topology.graph.links)
  .enter().append("line")
  .attr("class", "link")
  .style("stroke-width", function(d) { return Math.sqrt(d.value); });

  var node = svg.selectAll(".node")
  .data(phoenix.topology.graph.nodes)
  .enter().append("circle")
  .attr("class", "node")
  .attr("r", 15)
  .style("fill", function(d) { return color(d.group); })
  .call(force.drag);

  var text = svg.append("svg:g").selectAll("g")
  .data(force.nodes())
  .enter().append("svg:g");

  text.append("svg:text")
  .text(function(d) { return d.id; });

  node.append("title")
  .text(function(d) { return d.id; });



  force.on("tick", function() {
    link.attr("x1", function(d) { return d.source.x; })
    .attr("y1", function(d) { return d.source.y; })
    .attr("x2", function(d) { return d.target.x; })
    .attr("y2", function(d) { return d.target.y; });

    text.attr("transform", function(d) { return "translate(" + d.x + "," + d.y + ")"; });    

    node.attr("cx", function(d) { return d.x; })
    .attr("cy", function(d) { return d.y; });
  });
}
