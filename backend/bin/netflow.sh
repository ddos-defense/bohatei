#!/bin/sh

#ovs-vsctl -- set Bridge s1 netflow=@nf -- --id=@nf create NetFlow target=\"192.168.123.50:5566\" active-timeout=30
#ovs-vsctl -- set Bridge s2 netflow=@nf -- --id=@nf create NetFlow target=\"192.168.123.50:5566\" active-timeout=30
#ovs-vsctl -- set Bridge s3 netflow=@nf -- --id=@nf create NetFlow target=\"192.168.123.50:5566\" active-timeout=30

ovs-vsctl -- set Bridge s1 netflow=@nf -- --id=@nf create NetFlow target=\"192.168.123.50:5566\" active-timeout=30
ovs-vsctl -- set Bridge s2 netflow=@nf -- --id=@nf create NetFlow target=\"192.168.123.50:5566\" active-timeout=30
ovs-vsctl -- set Bridge s3 netflow=@nf -- --id=@nf create NetFlow target=\"192.168.123.50:5566\" active-timeout=30
ovs-vsctl -- set Bridge s4 netflow=@nf -- --id=@nf create NetFlow target=\"192.168.123.50:5566\" active-timeout=30
ovs-vsctl -- set Bridge s5 netflow=@nf -- --id=@nf create NetFlow target=\"192.168.123.50:5566\" active-timeout=30
ovs-vsctl -- set Bridge s6 netflow=@nf -- --id=@nf create NetFlow target=\"192.168.123.50:5566\" active-timeout=30
ovs-vsctl -- set Bridge s7 netflow=@nf -- --id=@nf create NetFlow target=\"192.168.123.50:5566\" active-timeout=30
ovs-vsctl -- set Bridge s8 netflow=@nf -- --id=@nf create NetFlow target=\"192.168.123.50:5566\" active-timeout=30
ovs-vsctl -- set Bridge s9 netflow=@nf -- --id=@nf create NetFlow target=\"192.168.123.50:5566\" active-timeout=30
ovs-vsctl -- set Bridge s10 netflow=@nf -- --id=@nf create NetFlow target=\"192.168.123.50:5566\" active-timeout=30
ovs-vsctl -- set Bridge s14 netflow=@nf -- --id=@nf create NetFlow target=\"192.168.123.50:5566\" active-timeout=30
