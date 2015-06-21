package main

import (
	"bufio"
	"errors"
	"fmt"
	"io/ioutil"
	"os"
	"os/exec"
	"regexp"
	"strconv"
	"strings"
)

const (
	CONTROL_SWITCH     = "control_sw"
	CONTROL_NW_NETMASK = "255.255.255.0"
	CONTROL_IF         = "eth1"
	NET_IF             = "eth4"
	CONTROLLER_ADDRESS = "192.168.123.201:6633"
	//CONTROLLER_ADDRESS     = "127.0.0.1:6633"
	HOST_DOMAIN_PREFIX     = "host"
	HOST_PREFIX            = "h"
	HOST_AGENT_PREFIX      = "ha"
	HOST_AGENT_NET_PORT    = 2
	HOST_AGENT_HOST_PORT   = 1
	SW_PREFIX              = "s"
	VSW_PREFIX             = "v"
	NET                    = "net"
	MIN_VSW_DPID           = 0x100
	MIN_SW_DPID            = 0x10000
	INITIAL_VM_CAPACITY    = 10
	INITIAL_SLICE_CAPACITY = 10
	SPEED_REGEXP           = `^\d+[kmgt]$`
	// Toby: I'm not sure this value is right. I want to make the burst queue size small to drop the packet
	//	BURST_SIZE        = "10mbit"
	LATENCY_FOR_QUEUE = "20ms"
)

var speedReg *regexp.Regexp

type Edge struct {
	First  string
	Second string
	Speed  string
}

func NewEdge(node1, node2 string) (*Edge, error) {
	self := new(Edge)
	switch {
	case isSW(node1) && isSW(node2):
	case isHost(node1) && isHost(node2):
	case isSW(node1) && isHost(node2), isHost(node1) && isSW(node2):
		if isHost(node1) {
			node1, node2 = node2, node1
		}
	case isSW(node1) && isNet(node2), isNet(node1) && isSW(node2):
		if isNet(node1) {
			node1, node2 = node2, node1
		}
	case isHost(node1) && isNet(node2), isNet(node1) && isHost(node2):
		if isNet(node1) {
			node1, node2 = node2, node1
		}
	default:
		return nil, errors.New("Invalid Edge Format")
	}
	self.First = node1
	self.Second = node2
	return self, nil
}

func NewEdgeWithSpeed(node1, node2, speed string) (*Edge, error) {
	self, err := NewEdge(node1, node2)
	if err != nil {
		panic(err)
	}
	if !speedReg.MatchString(speed) {
		return nil, errors.New("speed format error, speed must follow this rule, `^\\d+[kmgt]")
	}
	self.Speed = speed + "bit"
	return self, nil
}

type Topology struct {
	edges         []*Edge
	edgesFromHost map[string][]*Edge
	inlinedHosts  map[string]bool
	swes          map[string]bool
	hosts         map[string]bool
}

func NewTopology() *Topology {
	self := new(Topology)
	self.edgesFromHost = make(map[string][]*Edge)
	self.inlinedHosts = make(map[string]bool)
	self.swes = make(map[string]bool)
	self.hosts = make(map[string]bool)
	return self
}

func (self *Topology) AppendEdnge(edge *Edge) {
	self.edges = append(self.edges, edge)
	/*
		switch {
		case isHost(edge.First):
			host = edge.First
		case isHost(edge.Second):
			host = edge.Second
		}
		if host != "" {
	*/

	checkInlinedHost := func(host string) {
		if _, ok := self.edgesFromHost[host]; !ok {
			self.edgesFromHost[host] = make([]*Edge, 0, INITIAL_SLICE_CAPACITY)
		}
		self.edgesFromHost[host] = append(self.edgesFromHost[host], edge)
		if len(self.edgesFromHost[host]) > 1 {
			self.inlinedHosts[host] = true
		}

	}
	if isHost(edge.First) {
		checkInlinedHost(edge.First)
	}
	if isHost(edge.Second) {
		checkInlinedHost(edge.Second)
	}
}

func (self *Topology) Analyze() {
	tmpEdges := make([]*Edge, 0, INITIAL_SLICE_CAPACITY)
	for inlinedHost, _ := range self.inlinedHosts {
		nodeNum := strings.Replace(inlinedHost, HOST_PREFIX, "", 1)
		vSWName := VSW_PREFIX + nodeNum
		for _, edge := range self.edgesFromHost[inlinedHost] {
			switch {
			case edge.First == inlinedHost:
				edge.First = vSWName
			case edge.Second == inlinedHost:
				edge.Second = vSWName
			}
		}
		edge, err := NewEdge(vSWName, inlinedHost)
		if err != nil {
			panic(err)
		}
		tmpEdges = append(tmpEdges, edge)
	}
	self.edges = append(tmpEdges, self.edges...)
}

func (self *Topology) Debug() {
	for _, edge := range self.edges {
		fmt.Println(edge.First, edge.Second)
	}
}

func (self *Topology) Launch() {
	for _, edge := range self.edges {
		n1 := edge.First
		n2 := edge.Second
		speed := edge.Speed
		switch {
		case isSW(n1) && isSW(n2):
			if !self.isLaunchedSW(n1) {
				self.launchSW(n1)
			}
			if !self.isLaunchedSW(n2) {
				self.launchSW(n2)
			}
			addLink(n1, n2, speed)
		case isSW(n1) && isHost(n2):
			var hostAgentSW string
			if !self.isLaunchedSW(n1) {
				self.launchSW(n1)
			}
			if !self.isLaunchedHost(n2) {
				hostAgentSW = self.launchHost(n2)
			}
			addLink(n1, hostAgentSW, speed)
		case isSW(n1) && isNet(n2):
			if !self.isLaunchedSW(n1) {
				self.launchSW(n1)
			}
			addPortToSwitch(n1, NET_IF)
			ifUP(NET_IF)
		case isHost(n1) && isNet(n2):
			var hostAgentSW string
			if !self.isLaunchedHost(n1) {
				hostAgentSW = self.launchHost(n1)
			}
			addPortToSwitchWithPortNum(hostAgentSW, NET_IF, HOST_AGENT_NET_PORT)
			ifUP(NET_IF)
		default:
			panic(fmt.Errorf("Invalid Edge Format %v %v", n1, n2))
		}
	}
}

func (self *Topology) ConnectController() {
	for sw, _ := range self.swes {
		if !isControlSW(sw) {
			setDPID(sw, createDPID(sw))
			setController(sw)
		}
	}
}

func (self *Topology) isLaunchedSW(sw string) bool {
	_, ok := self.swes[sw]
	return ok
}

func (self *Topology) isLaunchedHost(host string) bool {
	_, ok := self.hosts[host]
	return ok
}

func (self *Topology) launchSW(sw string) {
	addSW(sw)
	self.swes[sw] = true
}

func (self *Topology) launchHost(host string) string {
	hostAgentSW := addHost(host)
	self.swes[hostAgentSW] = true
	self.hosts[host] = true
	return hostAgentSW
}

func runWithOutput(command *exec.Cmd) string {
	stdout, err := command.StdoutPipe()
	stderr, err := command.StderrPipe()
	if err != nil {
		panic(err)
	}
	command.Start()
	result, err := ioutil.ReadAll(stdout)
	if err != nil {
		panic(err)
	}
	errResult, err := ioutil.ReadAll(stderr)
	if err != nil {
		panic(err)
	}
	if len(errResult) != 0 {
		fmt.Println(string(errResult[:]))
	}
	return string(result[:])
}

func run(command *exec.Cmd) {
	if result := runWithOutput(command); result != "" {
		fmt.Println(result)
	}
}

func getExistingOVS() []string {
	command := exec.Command("ovs-vsctl", "list-br")
	result := runWithOutput(command)
	return strings.Split(strings.TrimRight(result, "\n"), "\n")
}

func clearOVS(swes []string) {
	fmt.Println("Clear OVS")
	for _, sw := range swes {
		command := exec.Command("ovs-vsctl", "del-br", sw)
		run(command)
	}
}

func installArpDropRule(sw string) {
	fmt.Println("ARP drop rule at", sw)
	command := exec.Command("ovs-ofctl", "add-flow", sw, "priority=0,arp,actions=drop")
	run(command)
}

func setFailureMode(sw string) {
	fmt.Println("Set Failure mode", sw)
	command := exec.Command("ovs-vsctl", "set-fail-mode", sw, "secure")
	run(command)
}

func getExistingVM() []string {
	command := exec.Command("virsh", "list")
	vms := make([]string, 0, INITIAL_VM_CAPACITY)
	result := runWithOutput(command)
	for _, r := range strings.Split(result, "\n") {
		if strings.Contains(r, "running") {
			infos := strings.Fields(r)
			vms = append(vms, infos[1])
		}
	}
	return vms

}

func clearVM(vms []string) {
	fmt.Println("Clear VM")
	for _, vm := range vms {
		fmt.Println("Destroy", vm)
		command := exec.Command("virsh", "destroy", vm)
		run(command)
	}
}

func getExistingVTLink() []string {
	command := exec.Command("ifconfig", "-a")
	result := runWithOutput(command)
	var vtIf = make([]string, 0, 10)
	for _, r := range strings.Split(result, "\n") {
		if strings.Contains(r, "vt-") {
			vtIf = append(vtIf, (strings.Fields(r))[0])
		}
	}
	return vtIf
}

func clearVTLink(vtLink []string) {
	for _, link := range vtLink {
		command := exec.Command("ip", "link", "del", link)
		run(command)
	}
}

func showOVS() {
	fmt.Println("Show OVS")
	command := exec.Command("ovs-vsctl", "show")
	run(command)
}

func showVM() {
	fmt.Println("show VM")
	command := exec.Command("virsh", "list")
	run(command)
}

func showVTLink() {
	fmt.Println("show VT Link")
	command := exec.Command("ip", "link", "show")
	run(command)
}

// This is very dirty hack to up the interface
// I cannot use ifup simply, we want to solve this
func ifUP(ifName string) {
	command := exec.Command("ifconfig", ifName, "1.1.1.1", "netmask", "255.255.255.0")
	run(command)
	command = exec.Command("ifconfig", ifName, "0.0.0.0")
	run(command)
}

func ifDown(ifName string) {
	command := exec.Command("ifconfig", ifName, "down")
	run(command)
}

func setController(sw string) {
	command := exec.Command("ovs-vsctl", "set-controller", sw, "tcp:"+CONTROLLER_ADDRESS)
	run(command)
}

func setDPID(sw string, dpid string) {
	command := exec.Command("ovs-vsctl", "set", "bridge", sw, "other-config:datapath-id="+dpid)
	run(command)
}

func addPortToSwitch(sw string, port string) {
	command := exec.Command("ovs-vsctl", "add-port", sw, port)
	run(command)
}

func addPortToSwitchWithPortNum(sw string, port string, portNum int) {
	command := exec.Command("ovs-vsctl", "add-port", sw, port, "--", "set", "interface", sw, "ofport="+strconv.FormatInt(int64(portNum), 10))
	run(command)
}

func isSW(node string) bool {
	return strings.HasPrefix(node, SW_PREFIX) || strings.HasPrefix(node, VSW_PREFIX)
}

func isVSW(node string) bool {
	return strings.HasPrefix(node, VSW_PREFIX)
}

func isHostAgent(node string) bool {
	return strings.HasPrefix(node, HOST_AGENT_PREFIX)
}

func isHost(node string) bool {
	return strings.HasPrefix(node, HOST_PREFIX)
}

func isNet(node string) bool {
	return node == NET
}

func isControlSW(node string) bool {
	return node == CONTROL_SWITCH
}

func addLink(n1, n2, speed string) {
	if !((isHostAgent(n1) || isSW(n1)) && (isHostAgent(n2) || isSW(n2))) {
		fmt.Println("Link must be established between switch and switch or switch and host agent\n", n1, n2)
	}
	var command *exec.Cmd
	vt1 := "vt-" + n1 + "-" + n2
	vt2 := "vt-" + n2 + "-" + n1
	fmt.Println("Add Link", n1, n2, vt1, vt2)

	command = exec.Command("ip", "link", "add", vt1, "type", "veth", "peer", "name", vt2)
	run(command)
	fmt.Println("Add Link Done", n1, n2, vt1, vt2)

	//  test for rate limit and delay
	if speed != "" {
		command = exec.Command("tc", "qdisc", "add", "dev", vt1, "root", "tbf", "rate", speed, "burst", "1540", "latency", "1ms")
		run(command)
	}

	fmt.Println("Add Port", n1, vt1)
	switch {
	case isHostAgent(n1):
		addPortToSwitchWithPortNum(n1, vt1, HOST_AGENT_NET_PORT)
	case isSW(n1):
		addPortToSwitch(n1, vt1)
	}
	fmt.Println("Add Port Done", n1, vt1)

	fmt.Println("Add Port", n2, vt2)
	switch {
	case isHostAgent(n2):
		addPortToSwitchWithPortNum(n2, vt2, HOST_AGENT_NET_PORT)
	case isSW(n2):
		addPortToSwitch(n2, vt2)
	}
	fmt.Println("Add Port Done", n2, vt2)

	fmt.Println("Promisc UP", vt1)
	command = exec.Command("ifconfig", vt1, "-broadcast", "-arp", "promisc", "up")
	run(command)
	fmt.Println("Promisc UP Done", vt1)
	fmt.Println("Promisc UP", vt2)
	command = exec.Command("ifconfig", vt2, "-broadcast", "-arp", "promisc", "up")
	run(command)
	fmt.Println("Promisc UP Done", vt2)

}

func addControlNW(address string, localFlag string) {
	fmt.Println("Start Control NW")
	addSW(CONTROL_SWITCH)
	addPortToSwitch(CONTROL_SWITCH, CONTROL_IF)
	command := exec.Command("ifconfig", CONTROL_SWITCH, address, "netmask", CONTROL_NW_NETMASK)
	run(command)
	if localFlag == "remote" {
		fmt.Println("remote controller")
		ifUP(CONTROL_IF)
	} else {
		fmt.Println("local controller")
		ifDown(CONTROL_IF)
	}
}

func addSW(sw string) {
	fmt.Println("Start SW", sw)
	command := exec.Command("ovs-vsctl", "add-br", sw)
	run(command)
	fmt.Println(sw, "is up")
	ifUP(sw)
	fmt.Println(sw, "interface is up")

	if !isControlSW(sw) {
		setDPID(sw, createDPID(sw))
		setController(sw)
		setFailureMode(sw)
		//installArpDropRule(sw)
	}
	//	installArpDropRule(sw)
}

func startVM(hostDomain string) {
	fmt.Println(" Start VM", hostDomain)
	command := exec.Command("virsh", "start", hostDomain)
	run(command)
}

func addHostAgent(hostNum string) string {
	hostAgentSW := HOST_AGENT_PREFIX + hostNum
	fmt.Println(" Add HostAgent", hostAgentSW)
	addSW(hostAgentSW)
	return hostAgentSW
}

func createDPID(node string) string {
	var nodeNum string
	var num int64
	var err error
	var dpid string
	fmt.Println(node)
	switch {
	case isHostAgent(node):
		nodeNum = strings.Replace(node, HOST_AGENT_PREFIX, "", 1)
		num, err = strconv.ParseInt(nodeNum, 10, 64)
	case isSW(node):
		if isVSW(node) {
			nodeNum = strings.Replace(node, VSW_PREFIX, "", 1)
			num, err = strconv.ParseInt(nodeNum, 10, 64)
			num += MIN_VSW_DPID
		} else {
			nodeNum = strings.Replace(node, SW_PREFIX, "", 1)
			fmt.Println(nodeNum)
			num, err = strconv.ParseInt(nodeNum, 10, 64)
			num += MIN_SW_DPID
		}
	}

	if err != nil {
		panic(err)
	}

	switch {
	case (0 <= num) && (num < 0x100):
		dpid = fmt.Sprintf("00000000000000%02x", num)
	case (0x100 <= num) && (num < 0x10000):
		dpid = fmt.Sprintf("000000000000%02x%02x", (num&0xff00)>>8, num&0xff)
	case (0x10000 <= num) && (num < 0x1000000):
		dpid = fmt.Sprintf("0000000000%02x%02x%02x", (num&0xff0000)>>16, (num&0xff00)>>8, num&0xff)
	}

	return dpid
}

func addHost(host string) string {
	fmt.Println("Add Host", host)
	hostNum := strings.Replace(host, HOST_PREFIX, "", 1)
	hostAgentSW := addHostAgent(hostNum)
	hostDomain := HOST_DOMAIN_PREFIX + hostNum
	startVM(hostDomain)
	return hostAgentSW
}

func show() {
	showOVS()
	showVM()
}

func fail() {
	//clearOVS(getExistingOVS())
	show()
}

func main() {
	if len(os.Args) != 4 {
		fmt.Println("usage: start <control ip> <topo file> <local flag>")
		return
	}

	var err error
	if speedReg, err = regexp.Compile(SPEED_REGEXP); err != nil {
		panic(err)
	}

	clearVM(getExistingVM())
	clearOVS(getExistingOVS())
	clearVTLink(getExistingVTLink())
	addControlNW(os.Args[1], os.Args[3])
	topology := NewTopology()

	file, err := os.Open(os.Args[2])
	if err != nil {
		panic(err)
	}
	scanner := bufio.NewScanner(file)
	for scanner.Scan() {
		if strings.Contains(scanner.Text(), "#") {
			continue
		}
		nodes := strings.Fields(scanner.Text())
		switch len(nodes) {
		case 2:
			edge, err := NewEdge(nodes[0], nodes[1])
			if err != nil {
				panic(err)
			}
			topology.AppendEdnge(edge)
		case 3:
			edge, err := NewEdgeWithSpeed(nodes[0], nodes[1], nodes[2])
			if err != nil {
				panic(err)
			}
			topology.AppendEdnge(edge)
		default:
			fmt.Println("Topology error\n", scanner.Text())
			fail()
		}
	}
	topology.Analyze()
	topology.Debug()
	topology.Launch()
	//	topology.ConnectController()
	show()
}
