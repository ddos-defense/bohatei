#!/bin/sh

ovs-ofctl add-flow ha25 priority=300,tcp,in_port=2,nw_dst=10.25.0.1,,actions=mod_dl_dst:00:00:00:00:00:19,output:1
ovs-ofctl add-flow ha25 priority=200,tcp,in_port=2,actions=mod_dl_dst:00:00:00:00:00:19,mod_nw_dst:100.0.0.1,output:1
ovs-ofctl add-flow ha25 priority=200,tcp,in_port=1,nw_dst=10.190.0.1,actions=output:2
ovs-ofctl add-flow ha25 priority=10,tcp,in_port=1,nw_src=100.0.0.1,actions=mod_nw_src:10.190.0.1,output:2


