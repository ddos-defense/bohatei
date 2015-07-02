#!/bin/sh


flowtag_control mb add -mbId 21 -hostId 21 -type MONITOR -address 10.21.0.1 -mask 255.255.0.0
flowtag_control mb add -mbId 22 -hostId 22 -type CONSUME -address 10.22.0.1 -mask 255.255.0.0
flowtag_control mb add -mbId 23 -hostId 23 -type CONSUME -address 10.23.0.1 -mask 255.255.0.0
flowtag_control mb add -mbId 24 -hostId 24 -type CONSUME -address 10.24.0.1 -mask 255.255.0.0

flowtag_control mb add -mbId 25 -hostId 25 -type CONSUME -address 10.25.0.1 -mask 255.255.0.0
flowtag_control mb add -mbId 26 -hostId 26 -type CONSUME -address 10.26.0.1 -mask 255.255.0.0
flowtag_control mb add -mbId 27 -hostId 27 -type CONSUME -address 10.27.0.1 -mask 255.255.0.0
flowtag_control mb add -mbId 28 -hostId 28 -type CONSUME -address 10.28.0.1 -mask 255.255.0.0

flowtag_control mb add -mbId 31 -hostId 31 -type MONITOR -address 10.31.0.1 -mask 255.255.0.0
flowtag_control mb add -mbId 32 -hostId 32 -type MONITOR -address 10.32.0.1 -mask 255.255.0.0
flowtag_control mb add -mbId 33 -hostId 33 -type MONITOR -address 10.33.0.1 -mask 255.255.0.0
flowtag_control mb add -mbId 34 -hostId 34 -type MONITOR -address 10.34.0.1 -mask 255.255.0.0
flowtag_control mb add -mbId 35 -hostId 35 -type MONITOR -address 10.35.0.1 -mask 255.255.0.0
flowtag_control mb add -mbId 36 -hostId 36 -type MONITOR -address 10.36.0.1 -mask 255.255.0.0
flowtag_control mb add -mbId 37 -hostId 37 -type MONITOR -address 10.37.0.1 -mask 255.255.0.0
flowtag_control mb add -mbId 38 -hostId 38 -type MONITOR -address 10.38.0.1 -mask 255.255.0.0

flowtag_control mb add -mbId 25 -hostId 25 -type CONSUME -address 100.0.0.1 -mask 255.255.0.0

# FlowTag List
flowtag_control tag clear
flowtag_control tag add -tag 0 -srcIP 0.0.0.0 -next 2
flowtag_control tag add -tag 4 -srcIP 0.0.0.0 -next 2
flowtag_control tag add -tag 8 -srcIP 0.0.0.0 -next 2
flowtag_control tag add -tag 12 -srcIP 0.0.0.0 -next 2
flowtag_control tag add -tag 16 -srcIP 0.0.0.0 -next 2
flowtag_control tag add -tag 20 -srcIP 0.0.0.0 -next 2
flowtag_control tag add -tag 24 -srcIP 0.0.0.0 -next 2
flowtag_control tag add -tag 28 -srcIP 0.0.0.0 -next 2
flowtag_control tag add -tag 32 -srcIP 0.0.0.0 -next 2
flowtag_control tag add -tag 36 -srcIP 0.0.0.0 -next 2
flowtag_control tag add -tag 40 -srcIP 0.0.0.0 -next 2
flowtag_control tag add -tag 44 -srcIP 0.0.0.0 -next 2
flowtag_control tag add -tag 48 -srcIP 0.0.0.0 -next 2
flowtag_control tag add -tag 72 -srcIP 0.0.0.0 -next 2
flowtag_control tag add -tag 56 -srcIP 0.0.0.0 -next 2
flowtag_control tag add -tag 60 -srcIP 0.0.0.0 -next 2

# Chaining Rule
# UDP Flood  (C1:4 (attack? R1:8) egress)
flowtag_control out add -mbId 10 -state 0 -preTag 0 -newTag 4 -next 21
flowtag_control out add -mbId 10 -state 0 -preTag 0 -newTag 8 -next 22
flowtag_control out add -mbId 10 -state 0 -preTag 0 -newTag 12 -next 23
flowtag_control out add -mbId 10 -state 0 -preTag 0 -newTag 16 -next 24
flowtag_control out add -mbId 10 -state 0 -preTag 0 -newTag 20 -next 25
flowtag_control out add -mbId 10 -state 0 -preTag 0 -newTag 24 -next 26
flowtag_control out add -mbId 10 -state 0 -preTag 0 -newTag 28 -next 27
flowtag_control out add -mbId 10 -state 0 -preTag 0 -newTag 32 -next 28
flowtag_control out add -mbId 10 -state 0 -preTag 0 -newTag 40 -next 31
flowtag_control out add -mbId 10 -state 0 -preTag 0 -newTag 44 -next 32

ovs-ofctl add-flow ha21 priority=10,ip,in_port=1,nw_tos=4,actions=mod_nw_tos:20,output:2
