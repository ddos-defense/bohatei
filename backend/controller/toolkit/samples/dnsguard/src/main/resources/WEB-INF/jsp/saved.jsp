
<!DOCTYPE html>
<html>
  <head>
    <meta charset="utf-8">
    <meta http-equiv="X-UA-Compatible" content="IE=edge">
    <title>DnsGuard Web</title>
    <meta name="description" content="">
    <meta name="viewport" content="width=device-width, initial-scale=1">
	
	<!-- style -->
    <link rel="stylesheet" href="/css/ext/pure.css"/>
    <link rel="stylesheet" href="/css/phoenix.css"/>
	
    <!-- style app -->
    <link rel="stylesheet" href="/dnsguard/web/css/simple.css"/>
    <link rel="stylesheet" href="http://cdn.datatables.net/1.10.0/css/jquery.dataTables.css"/>
    
    <script data-main="/dnsguard/web/js/main" src="/js/ext/require.js"></script>
    
    <script type="text/javascript" src="http://code.jquery.com/jquery-1.10.2.min.js"></script>
    <script type="text/javascript" src="http://cdn.datatables.net/1.10.0/js/jquery.dataTables.js"></script>
    
	<script type="text/javascript" src="http://cdnjs.cloudflare.com/ajax/libs/jqPlot/1.0.8/jquery.jqplot.min.js"></script>
	<script type="text/javascript" src="http://cdnjs.cloudflare.com/ajax/libs/jqPlot/1.0.8/plugins/jqplot.pieRenderer.js"></script>
    <script type="text/javascript" src="http://cdnjs.cloudflare.com/ajax/libs/jqPlot/1.0.8/plugins/jqplot.pieRenderer.min.js"></script>
    <script type="text/javascript" src="http://cdnjs.cloudflare.com/ajax/libs/jqPlot/1.0.8/plugins/jqplot.donutRenderer.min.js"></script>
	
    <script type="text/javascript">
	 
    $( document ).ready(function() {
     
	  var plotdata = [
		['Heavy Industry', 12],['Retail', 9], ['Light Industry', 14],
		['Out of home', 16],['Commuting', 7], ['Orientation', 9]
	  ];
	  
	  var plot1 = jQuery.jqplot ('chart-resp-types', [plotdata],
		{
		  seriesDefaults: {
			// Make this a pie chart.
			renderer: jQuery.jqplot.PieRenderer,
			rendererOptions: {
			  // Put data labels on the pie slices.
			  // By default, labels show the percentage of the slice.
			  showDataLabels: true
			}
		  },
		  legend: { show:true, location: 'e' }
		}
	  );
	 
	var getviolators = $.getJSON( "/dnsguard/northbound/getviolators/", function() {
		console.log( "success" );
	})
		.done(function( data ) {
		
		//alert(data);
		
		//$('#violators-table').html( '<table cellpadding="0" cellspacing="0" border="0" class="display" id="example"></table>' );
 
	    $("#example").dataTable( );
		
		console.log( "second success" );
	})
		.fail(function() {
		console.log( "error" );
	})
		.always(function() {
		console.log( "complete" );
	});
	
    });
	</script>
	
  </head>
  <body>

	<div id="main">
		
		<div id="sector1">
			<h3></h3>
		</div>
		
		<div id="sector2">
			<h3>Forge DNS</h3>
			<div id="chart-resp-types"></div>
		</div>
		
		<div id="sector3">
			<h3>Get Violators</h3>
			<table id="example" class="display" cellspacing="0" width="100%">
		        <thead>
		            <tr>
		                <th>Violator Ip</th>
		                <th>TimeStamp</th>
		            </tr>
		        </thead>
		 
		        <tfoot>
		            <tr>
		                <th>Violator Ip</th>
		                <th>TimeStamp</th>
		            </tr>
		        </tfoot>
		    </table>
		</div>  
		
	</div>
  </body>
  

	
</html>
