# Bohatei

This repository contains a **first** version of the components described in the Bohatei paper, as well as a web-based User Interface. 
The backend folder consists of :
* an implementation of the FlowTags framework for the OpenDaylight controller 
* an implementation of the resource management algorithms 
* a topology file that was used to simulate an ISP topology
* scripts that facilitate functions such as spawning, tearing down and retrieving the topology.
* scripts that automate and coordinate the components required for the usecases examined.

The frontend folder contains the required files for the web interface. 

For the experiments performed, we used a set of VM images that contain implementations of the strategy graphs for each type of attack (SYN Flood, UDP Flood, DNS Amplification and Elephant Flow). Those images will become available at a later stage. The tools that were used for those strategy graphs are the following:
* [Bro] (https://www.bro.org/index.html)
* [Snort] (https://www.snort.org/)
* [Balancer] (http://www.inlab.de/balance.html)
* Iptables
* Iperf
* Custom scripts to simulate the attacks
