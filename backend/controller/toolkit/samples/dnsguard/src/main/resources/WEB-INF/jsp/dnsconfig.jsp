<html>
	<head>
	
	<!-- style -->
    <link rel="stylesheet" href="/css/ext/pure.css"/>
    <link rel="stylesheet" href="/css/phoenix.css"/>
	
    <!-- style app -->
    <link rel="stylesheet" href="/dnsguard/web/css/simple.css"/>
	<link rel="stylesheet" href="http://cdn.datatables.net/1.10.0/css/jquery.dataTables.css"/>
	 
	<script data-main="/dnsguard/web/js/dnsconfig" src="/js/ext/require.js"></script> 
	
	</head>
	<body>
	   	
	   	<div id="main">
			<h3>Local Dns configuration</h3>
			
			<h5>Please provide the Ip address of the Local DNS Server</h5>
			
			<form action="updatedns" method="post">
				<div>
				  Local Dns Resolver Ip: <input id="localdns" name="localdns" value="" type="text">
				</div>
				
				<input type="submit" value="Save">
			</form>
			
		</div>
		
	</body>

</html>
