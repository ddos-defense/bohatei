#!/bin/bash

cd /var/www/ConferenceDemo/
rm ele_output.png
rm dns_output.png
rm udp_output.png
rm output.png

sh /testbed/bin/eval/syn/clear.sh
sh /testbed/bin/eval/udpflood/clear.sh
sh /testbed/bin/eval/dns/clear.sh
sh /testbed/bin/eval/elephant/clear.sh

kill `ps -ef | grep -i attack_server | grep -v grep | awk '{print $2}'`
cd /testbed/bin/module/C
./attack_server > /dev/null &

