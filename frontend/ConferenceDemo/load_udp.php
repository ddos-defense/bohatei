<?php 
    if (file_exists("/var/www/ConferenceDemo/udp_output.png"))
       echo "YES";
    else {
        echo "NO";
        shell_exec("scp root@192.168.123.6:/toby/udp_output.png /var/www/ConferenceDemo/");  
    }
?>

