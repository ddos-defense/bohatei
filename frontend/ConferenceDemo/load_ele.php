<?php 
    if (file_exists("/var/www/ConferenceDemo/ele_output.png"))
       echo "YES";
    else {
        echo "NO";
        shell_exec("scp root@192.168.123.7:/toby/ele_output.png /var/www/ConferenceDemo/");  
    }
?>

