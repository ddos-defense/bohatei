<html>
	<head>
	
	<!-- style -->
    <link rel="stylesheet" href="/css/ext/pure.css"/>
    <link rel="stylesheet" href="/css/phoenix.css"/>
	
    <!-- style app -->
    <link rel="stylesheet" href="/dnsguard/web/css/simple.css"/>
	<link rel="stylesheet" href="http://cdn.datatables.net/1.10.0/css/jquery.dataTables.css"/>
	 
	<script data-main="/dnsguard/web/js/modconfig" src="/js/ext/require.js"></script> 
	
	</head>
	<body>
	   	
	   	<div id="main">
			<h3>DnsGuard configuration</h3>
			
			<h5>Please provide the following information to configure the database.
			If it is the first time using the database this module will create the required tables</h5>
			
			<form action="updateconfig" method="post">
				<div>
				  DB Server IP: <input name="dbserver" value="127.0.0.1" type="text">
				</div>
				
				<div>
				  DB Port: <input name="dbport" value="9001" type="text">
				</div>	
				
				<div>
				  DB Name: <input name="dbname" value="dnsspy" type="text">
				</div>	
				
				<div>
				  DB Username: <input name="dbuser" value="SA" type="text">
				</div>		
				
				<div>
				  DB Password: <input name="dbpasswd" value="password" type="password">
				</div>		
				 
				<div>
				  DB InternalBuffer Size: <input name="ib_size" value="200" type="text">
				</div>
	
				<div>
				  DB InternalBuffer Max Size: <input name="ib_max" value="400" type="text">
				</div>
				
				<input type="submit" value="Save">
			</form>
			
		</div>
		
	</body>

</html>
