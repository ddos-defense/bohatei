#!/bin/

echo "Script starting"
#sh /testbed/bin/eval/syn/ddos_eval_syn_1.sh
echo "Applied rules"
sh /testbed/bin/eval/syn/proxy.sh
sh /testbed/bin/eval/syn/cpu.sh
sh /testbed/bin/eval/syn/1.sh

