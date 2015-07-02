#!/bin/sh

ovs-ofctl del-flows ha1  tcp,tp_dst=80,in_port=1
ovs-ofctl del-flows ha2  tcp,tp_dst=80,in_port=1
ovs-ofctl del-flows ha3  tcp,tp_dst=80,in_port=1
ovs-ofctl del-flows ha4  tcp,tp_dst=80,in_port=1
ovs-ofctl del-flows ha5  tcp,tp_dst=80,in_port=1
ovs-ofctl del-flows ha6  tcp,tp_dst=80,in_port=1
ovs-ofctl del-flows ha7  tcp,tp_dst=80,in_port=1
ovs-ofctl del-flows ha8  tcp,tp_dst=80,in_port=1
ovs-ofctl del-flows ha9  tcp,tp_dst=80,in_port=1
ovs-ofctl del-flows ha10  tcp,tp_dst=80,in_port=1
ovs-ofctl del-flows ha11  tcp,tp_dst=80,in_port=1
ovs-ofctl del-flows ha12  tcp,tp_dst=80,in_port=1
ovs-ofctl del-flows ha13  tcp,tp_dst=80,in_port=1
ovs-ofctl del-flows ha14  tcp,tp_dst=80,in_port=1
ovs-ofctl del-flows ha15  tcp,tp_dst=80,in_port=1
ovs-ofctl del-flows ha16  tcp,tp_dst=80,in_port=1
ovs-ofctl del-flows ha17  tcp,tp_dst=80,in_port=1
ovs-ofctl del-flows ha18  tcp,tp_dst=80,in_port=1
ovs-ofctl del-flows ha19  tcp,tp_dst=80,in_port=1
ovs-ofctl del-flows ha20  tcp,tp_dst=80,in_port=1
