#!/bin/sh


flowtag_control mb add -mbId 41 -hostId 41 -type MONITOR -address 10.41.0.1 -mask 255.255.0.0
flowtag_control mb add -mbId 42 -hostId 42 -type MONITOR -address 10.42.0.1 -mask 255.255.0.0
flowtag_control mb add -mbId 43 -hostId 43 -type MONITOR -address 10.43.0.1 -mask 255.255.0.0
flowtag_control mb add -mbId 44 -hostId 44 -type MONITOR -address 10.44.0.1 -mask 255.255.0.0
flowtag_control mb add -mbId 45 -hostId 45 -type MONITOR -address 10.45.0.1 -mask 255.255.0.0
flowtag_control mb add -mbId 46 -hostId 46 -type MONITOR -address 10.46.0.1 -mask 255.255.0.0
flowtag_control mb add -mbId 47 -hostId 47 -type MONITOR -address 10.47.0.1 -mask 255.255.0.0
flowtag_control mb add -mbId 48 -hostId 48 -type MONITOR -address 10.48.0.1 -mask 255.255.0.0
flowtag_control mb add -mbId 49 -hostId 49 -type MONITOR -address 10.49.0.1 -mask 255.255.0.0
flowtag_control mb add -mbId 50 -hostId 50 -type MONITOR -address 10.50.0.1 -mask 255.255.0.0

flowtag_control mb add -mbId 51 -hostId 51 -type MONITOR -address 10.51.0.1 -mask 255.255.0.0
flowtag_control mb add -mbId 52 -hostId 52 -type MONITOR -address 10.52.0.1 -mask 255.255.0.0
flowtag_control mb add -mbId 53 -hostId 53 -type MONITOR -address 10.53.0.1 -mask 255.255.0.0
flowtag_control mb add -mbId 55 -hostId 55 -type MONITOR -address 10.55.0.1 -mask 255.255.0.0


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
flowtag_control tag add -tag 52 -srcIP 0.0.0.0 -next 2
flowtag_control tag add -tag 56 -srcIP 0.0.0.0 -next 2
flowtag_control tag add -tag 60 -srcIP 0.0.0.0 -next 2

# Chaining Rule
# UDP Flood  (C1:4 (attack? R1:8) egress)
flowtag_control out add -mbId 10 -state 0 -preTag 0 -newTag 4 -next 41
flowtag_control out add -mbId 10 -state 0 -preTag 0 -newTag 8 -next 42
flowtag_control out add -mbId 10 -state 0 -preTag 0 -newTag 12 -next 43
flowtag_control out add -mbId 10 -state 0 -preTag 0 -newTag 16 -next 44
flowtag_control out add -mbId 10 -state 0 -preTag 0 -newTag 20 -next 45
flowtag_control out add -mbId 10 -state 0 -preTag 0 -newTag 24 -next 46
flowtag_control out add -mbId 10 -state 0 -preTag 0 -newTag 28 -next 47
flowtag_control out add -mbId 10 -state 0 -preTag 0 -newTag 32 -next 48
flowtag_control out add -mbId 10 -state 0 -preTag 0 -newTag 36 -next 49
flowtag_control out add -mbId 10 -state 0 -preTag 0 -newTag 40 -next 50
flowtag_control out add -mbId 11 -state 0 -preTag 0 -newTag 56 -next 51
