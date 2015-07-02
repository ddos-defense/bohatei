<?php 
    if (file_exists("/var/www/ConferenceDemo/output.png"))
       echo "YES";
    else {
        shell_exec("scp root@192.168.123.5:/toby/output.png /var/www/ConferenceDemo/");  
        echo "NO";
    }
?>

