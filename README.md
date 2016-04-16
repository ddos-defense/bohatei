# Bohatei

Copyright (c) 2014-2016, Carnegie Mellon University. All rights reserved.

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions are met:

(1) Redistributions of source code must retain the above copyright
    notice, this list of conditions and the following disclaimer.

(2) Redistributions in binary form must reproduce the above copyright
    notice, this list of conditions and the following disclaimer in the
    documentation and/or other materials provided with the distribution.

(3) Neither the name of Carnegie Mellon University nor the names of contributors may be used to endorse
    or promote products derived from this software without specific prior
    written permission.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
ARE DISCLAIMED.  IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
POSSIBILITY OF SUCH DAMAGE.

Note that some files in the distribution may carry their own copyright
notices.

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
