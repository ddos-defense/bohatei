<html>
	<head>
	
	<!-- style -->
    <link rel="stylesheet" href="/css/ext/pure.css"/>
    <link rel="stylesheet" href="/css/phoenix.css"/>
	
    <!-- style app -->
    <link rel="stylesheet" href="/dnsguard/web/css/simple.css"/>
	<link rel="stylesheet" href="http://cdn.datatables.net/1.10.0/css/jquery.dataTables.css"/>
	
	<script data-main="/dnsguard/web/js/main" src="/js/ext/require.js"></script>
	
	</head>
	<body>
	   	
	   	<div id="main">
			
			<div id="localdns">
				<h3></h3>
				<h5><a href="modconfig">Configure Database</a> | <a href="dnsconfig">Set Local Dns Server Ip</a></h5>
			</div>
			
			<div id="leftsection">
				<div class="internal-centeredsection">
					<div id="chart-resp-types"></div>
				</div>
			</div>
			
			<div id="rightsection">
				<div class="internal-centeredsection">
					<h3>List of Violators</h3>
					<table id="violators-table" class="display" cellspacing="0" width="100%">
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
			
			<div id="bottomsection">
				<h3>List of queries</h3>
				<table id="records-table" class="display" cellspacing="0" width="100%">
 		        	<thead>
				        <tr>
				            <th>Violator Ip</th>
				            <th>Non official dns Ip</th>
				            <th>Query</th>
				            <th>Response Type</th>
				            <th>Response</th>
				            <th>TimeStamp</th>
				        </tr>
				    </thead>
				
				    <tfoot>
				        <tr>
				            <th>Violator Ip</th>
				            <th>Non official dns Ip</th>
				            <th>Query</th>
				            <th>Response Type</th>
				            <th>Response</th>
				            <th>TimeStamp</th>
				        </tr>
				    </tfoot>
				</table>
 			</div>
	 			
			
		</div>
		
	</body>

</html>
