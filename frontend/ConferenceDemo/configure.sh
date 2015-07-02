#!/bin/bash

    if [ "$1" = "syn" ] 
    then
        ssh root@192.168.123.50 'sh /toby/run.sh && exit'
        ssh root@192.168.123.191 'sh /toby/attack/syn_attack.sh > syn_output.txt && exit' 
    elif [ "$1" = "udp" ]
    then
        ssh root@192.168.123.50 'sh /toby/run.sh && exit' 
        ssh root@192.168.123.191 'sh /toby/attack/udp_attack.sh > udp_output.txt && exit'
    elif [ "$1" = "dns" ]
    then
        ssh root@192.168.123.50 'sh /toby/run.sh && exit' 
        ssh root@192.168.123.191 'sh /toby/attack/dns_attack.sh > dns_output.txt && exit'
    elif [ "$1" = "ele" ]
    then
        ssh root@192.168.123.50 'sh /toby/run.sh && exit'
        ssh root@192.168.123.191 'sh /toby/attack/ele_attack.sh > ele_output.txt && exit'  
    fi    

    echo "Done.."
