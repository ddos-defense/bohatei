package main

import (
	"bufio"
	"fmt"
	"io/ioutil"
	"net"
	"os/exec"
	"strconv"
	"strings"
)

const PORT = 6666

var preAttack string
var attackType string

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

func main() {
	server, err := net.Listen("tcp", ":"+strconv.Itoa(PORT))
	if server == nil {
		panic(err)
	}
	conns := clientConns(server)
	for {
		go handleConn(<-conns)
	}
}

func clientConns(listener net.Listener) chan net.Conn {
	ch := make(chan net.Conn)
	i := 0
	go func() {
		for {
			client, _ := listener.Accept()
			if client == nil {
				fmt.Printf("couldn't accept")
				continue
			}
			i++
			fmt.Printf("%d: %v <-> %v\n", i, client.LocalAddr(), client.RemoteAddr())
			ch <- client
		}
	}()
	return ch
}

func handleConn(client net.Conn) {
	b := bufio.NewReader(client)
	//	var attackType string
	//	var volume float64
	for {
		line, err := b.ReadBytes('\n')
		if len(line) == 1 {
			break
		}
		if err != nil { // EOF, or worse
			break
		}
		info := strings.Split(string(line), ":")
		/*
			volume, err := strconv.ParseFloat(info[1], 64)
			if err != nil {
				continue
			}
		*/
		preAttack = attackType
		attackType = info[0]
	}

	var cmd *exec.Cmd
	if preAttack != attackType {
		fmt.Println(preAttack, "->", attackType)
		clear()
		switch attackType {
		case "SYNFLOOD":
			cmd = exec.Command("sh", "/testbed/bin/eval/syn/syn.sh")
		case "DNSFlood":
			cmd = exec.Command("sh", "/testbed/bin/eval/dns/dns.sh")
		case "UDPFlood":
			cmd = exec.Command("sh", "/testbed/bin/eval/udpflood/udp.sh")
		case "ElephantAttack":
			cmd = exec.Command("sh", "/testbed/bin/eval/elephant/elephant.sh")
		}
	}
	if cmd != nil {
		runWithOutput(cmd)
	}
	fmt.Println("next")
	client.Write([]byte("ok\n\n"))
}

func clear() {
	var cmd *exec.Cmd
	switch preAttack {
	case "SYNFLOOD":
		fmt.Println("clear SYNFLOOD")
		cmd = exec.Command("sh", "/testbed/bin/eval/syn/clear.sh")
	case "DNSFlood":
		fmt.Println("clear DNSFLOOD")
		cmd = exec.Command("sh", "/testbed/bin/eval/dns/clear.sh")
	case "UDPFlood":
		fmt.Println("clear UDPFLOOD")
		cmd = exec.Command("sh", "/testbed/bin/eval/udpflood/clear.sh")
	case "ElephantAttack":
		fmt.Println("clear ELEPHANT ATTACK")
		cmd = exec.Command("sh", "/testbed/bin/eval/elephant/clear.sh")
	}

	if cmd != nil {
		run(cmd)
	}
}
