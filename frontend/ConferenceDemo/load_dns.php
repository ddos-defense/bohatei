<?php 
    if (file_exists("/var/www/ConferenceDemo/dns_output.png"))
       echo "YES";
    else {
        echo "NO";
        shell_exec("scp root@192.168.123.8:/toby/dns_output.png /var/www/ConferenceDemo/");  
    }
?>

