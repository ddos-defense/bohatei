#!/bin/bash
    
    echo "Starting OpenDaylight controller in the background"
    cd /testbed/opendaylight/toolkit/main/target/main-osgipackage/opendaylight
    ./run.sh > /testbed/bin/odl_log &
    sleep 90

    echo "Fetching topology"
    cd /testbed/bin/
    unset http_proxy
    python topo_get.py
    sleep 5

    echo "Topology fetched -- Adding Flowtags to OpenDaylight"
    sh fw.sh > fw_log.txt

    echo "Register non attack related hosts and middleboxes"
    sleep 2
    sh ddos_ip.sh > /dev/null

    echo "Configure attack specific VMs"
    echo "SYN Flood Attack"

    ssh root@192.168.123.21 'sh /toby/run.sh && exit'
    ssh root@192.168.123.25 'sh /toby/setup.sh && exit'
    ssh root@192.168.123.31 'sh /toby/setup.sh && exit'

    echo "UDP Flood Attack"
    
    ssh root@192.168.123.41 'sh /toby/run.sh && exit'
    ssh root@192.168.123.42 'sh /toby/run.sh && exit'
    ssh root@192.168.123.51 'sh /toby/setup.sh && exit'
    sh /testbed/bin/ddos_eval_udp_1.sh > /dev/null

    echo "DNS Amp Attack -- need to configure hosts 61/65 manually"

    #ssh root@192.168.123.61 'sh /toby/run.sh && exit'
    #ssh root@192.168.123.65 'sh /toby/run.sh && exit'
    ssh root@192.168.123.71 'sh /toby/setup.sh && exit'
    ssh root@192.168.123.72 'sh /toby/setup.sh && exit'

    echo "Elephant Flow Attack"

    ssh root@192.168.123.81 'sh /toby/run.sh && exit'
    ssh root@192.168.123.91 'sh /toby/setup.sh && exit'

    echo "Configure switches to emit netflow and start data server"
    
    sh /testbed/bin/netflow.sh
    cd /testbed/bin/module/C
    ./attack_server > /testbed/bin/server_output.txt & 
    

    echo "Done.."
