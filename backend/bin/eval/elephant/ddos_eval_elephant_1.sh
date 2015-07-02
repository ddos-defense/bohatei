#!/bin/sh


flowtag_control mb add -mbId 81 -hostId 81 -type MONITOR -address 10.81.0.1 -mask 255.255.0.0
flowtag_control mb add -mbId 82 -hostId 82 -type MONITOR -address 10.82.0.1 -mask 255.255.0.0
flowtag_control mb add -mbId 83 -hostId 83 -type MONITOR -address 10.83.0.1 -mask 255.255.0.0
flowtag_control mb add -mbId 84 -hostId 84 -type MONITOR -address 10.84.0.1 -mask 255.255.0.0

flowtag_control mb add -mbId 85 -hostId 85 -type MONITOR -address 10.85.0.1 -mask 255.255.0.0
flowtag_control mb add -mbId 86 -hostId 86 -type MONITOR -address 10.86.0.1 -mask 255.255.0.0
flowtag_control mb add -mbId 87 -hostId 87 -type MONITOR -address 10.87.0.1 -mask 255.255.0.0
flowtag_control mb add -mbId 88 -hostId 88 -type MONITOR -address 10.88.0.1 -mask 255.255.0.0

flowtag_control mb add -mbId 91 -hostId 91 -type MONITOR -address 10.91.0.1 -mask 255.255.0.0
flowtag_control mb add -mbId 92 -hostId 92 -type MONITOR -address 10.92.0.1 -mask 255.255.0.0
flowtag_control mb add -mbId 93 -hostId 93 -type MONITOR -address 10.93.0.1 -mask 255.255.0.0
flowtag_control mb add -mbId 94 -hostId 94 -type MONITOR -address 10.94.0.1 -mask 255.255.0.0
flowtag_control mb add -mbId 95 -hostId 95 -type MONITOR -address 10.95.0.1 -mask 255.255.0.0
flowtag_control mb add -mbId 96 -hostId 96 -type MONITOR -address 10.96.0.1 -mask 255.255.0.0
flowtag_control mb add -mbId 97 -hostId 97 -type MONITOR -address 10.97.0.1 -mask 255.255.0.0
flowtag_control mb add -mbId 98 -hostId 98 -type MONITOR -address 10.98.0.1 -mask 255.255.0.0


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
flowtag_control out add -mbId 10 -state 0 -preTag 0 -newTag 4 -next 81
flowtag_control out add -mbId 10 -state 0 -preTag 0 -newTag 8 -next 82
flowtag_control out add -mbId 10 -state 0 -preTag 0 -newTag 12 -next 83
flowtag_control out add -mbId 10 -state 0 -preTag 0 -newTag 16 -next 84
flowtag_control out add -mbId 10 -state 0 -preTag 0 -newTag 20 -next 85
flowtag_control out add -mbId 10 -state 0 -preTag 0 -newTag 24 -next 86
flowtag_control out add -mbId 10 -state 0 -preTag 0 -newTag 28 -next 87
flowtag_control out add -mbId 10 -state 0 -preTag 0 -newTag 32 -next 88
flowtag_control out add -mbId 10 -state 0 -preTag 0 -newTag 40 -next 91
flowtag_control out add -mbId 10 -state 0 -preTag 0 -newTag 44 -next 92
