#!/bin/sh


flowtag_control mb add -mbId 61 -hostId 61 -type CONSUME -address 10.61.0.1 -mask 255.255.0.0
flowtag_control mb add -mbId 62 -hostId 62 -type CONSUME -address 10.62.0.1 -mask 255.255.0.0
flowtag_control mb add -mbId 63 -hostId 63 -type CONSUME -address 10.63.0.1 -mask 255.255.0.0
flowtag_control mb add -mbId 64 -hostId 64 -type CONSUME -address 10.64.0.1 -mask 255.255.0.0

flowtag_control mb add -mbId 65 -hostId 65 -type CONSUME -address 10.65.0.1 -mask 255.255.0.0
flowtag_control mb add -mbId 66 -hostId 66 -type CONSUME -address 10.66.0.1 -mask 255.255.0.0
flowtag_control mb add -mbId 67 -hostId 67 -type CONSUME -address 10.67.0.1 -mask 255.255.0.0
flowtag_control mb add -mbId 68 -hostId 68 -type CONSUME -address 10.68.0.1 -mask 255.255.0.0

flowtag_control mb add -mbId 71 -hostId 71 -type MONITOR -address 10.71.0.1 -mask 255.255.0.0
flowtag_control mb add -mbId 72 -hostId 72 -type MONITOR -address 10.72.0.1 -mask 255.255.0.0
flowtag_control mb add -mbId 73 -hostId 73 -type MONITOR -address 10.73.0.1 -mask 255.255.0.0
flowtag_control mb add -mbId 74 -hostId 74 -type MONITOR -address 10.74.0.1 -mask 255.255.0.0
flowtag_control mb add -mbId 75 -hostId 75 -type MONITOR -address 10.75.0.1 -mask 255.255.0.0
flowtag_control mb add -mbId 76 -hostId 76 -type MONITOR -address 10.76.0.1 -mask 255.255.0.0
flowtag_control mb add -mbId 77 -hostId 77 -type MONITOR -address 10.77.0.1 -mask 255.255.0.0
flowtag_control mb add -mbId 78 -hostId 78 -type MONITOR -address 10.78.0.1 -mask 255.255.0.0


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
flowtag_control out add -mbId 10 -state 0 -preTag 0 -newTag 4 -next 61
flowtag_control out add -mbId 10 -state 0 -preTag 0 -newTag 8 -next 62
flowtag_control out add -mbId 10 -state 0 -preTag 0 -newTag 12 -next 63
flowtag_control out add -mbId 10 -state 0 -preTag 0 -newTag 16 -next 64
flowtag_control out add -mbId 10 -state 0 -preTag 0 -newTag 20 -next 65
flowtag_control out add -mbId 10 -state 0 -preTag 0 -newTag 24 -next 66
flowtag_control out add -mbId 10 -state 0 -preTag 0 -newTag 28 -next 67
flowtag_control out add -mbId 10 -state 0 -preTag 0 -newTag 32 -next 68
flowtag_control out add -mbId 10 -state 0 -preTag 0 -newTag 40 -next 71
flowtag_control out add -mbId 10 -state 0 -preTag 0 -newTag 44 -next 72
