package main

import (
	"bufio"
	//"bytes"
	"fmt"
	"io/ioutil"
	"net"
	"os"
	"os/exec"
	"strconv"
	"strings"
	"time"
)

const (
	/*
		SYNFLOOD_THREAHOLD  = 300000
		UDPFLOOD_THREASHOLD = 400.0
		DNSFLOOOD_THRESHOLD = 200000
		ELEPHANT_THRESHOLD  = 40
	*/
	SYNFLOOD_THREAHOLD  = 100000
	UDPFLOOD_THREASHOLD = 50.0
	DNSFLOOOD_THRESHOLD = 80000
	ELEPHANT_THRESHOLD  = 20
)

var logFile string

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

func getFile() string {
	files, err := ioutil.ReadDir("/toby/netflow")
	var targetFile os.FileInfo
	if err != nil {
		panic(err)
	}
	for _, file := range files {
		if strings.Contains(file.Name(), "current") {
			if file.Size() > 300 {
				logFile = file.Name()
				return logFile
			} else {
				continue
			}

		}
		if targetFile == nil {
			targetFile = file
		}
		if targetFile.ModTime().Before(file.ModTime()) {
			targetFile = file
		}
	}

	if logFile == "" || (targetFile != nil && logFile != targetFile.Name()) {
		logFile = targetFile.Name()
		return logFile
	} else {
		return ""
	}
	/*
		for _, file := range files {
			if strings.Contains(file.Name(), "current") {
				logFile = file.Name()
			}
		}
	*/
	return logFile
}

func main() {

	for {
		//		file := "/toby/netflow/nfcapd.201411020121"
		file := getFile()
		if file == "" {
			time.Sleep(1 * time.Second)
			continue
		}
		fmt.Println(file)
		var synFloodPacketCount int64 = 0
		var udpPacketCount int64 = 0
		var udpByteCount float64 = 0
		var dnsPacketCount int64 = 0.0
		var dnsBytetCount float64 = 0.0
		//var proto, flags, srcIP, srcPort, dstIP, dstPort, byteNum, packet, bps string
		var proto, flags, srcPort, dstPort, packet string
		var packetNum int64
		var byteNum float64
		var maxPacketNum int64
		var maxByteNum float64
		var err error
		cmd := exec.Command("nfdump", "-r", "/toby/netflow/"+file, "-o", "fmt:%ts %td %pr %sa %sp -> %da %dp %flg %pkt %byt %fl %bps")
		result := runWithOutput(cmd)
		info := strings.Split(result, "\n")
		for _, line := range info {
			data := strings.Fields(line)
			if len(data) == 0 || !strings.HasPrefix(data[0], "2") {
				continue
			}
			proto = data[3]
			flags = data[9]
			//srcIP = data[4]
			srcPort = data[5]
			//dstIP = data[7]
			dstPort = data[8]
			//			byteCount = data[11]
			byteNum, err = strconv.ParseFloat(data[11], 64)
			if err != nil {
				panic(err)
			}

			if data[12] != "M" {
				byteNum /= 1024 * 1024
			}

			packet = data[10]
			packetNum, err = strconv.ParseInt(packet, 10, 32)
			if err != nil {
				panic(err)
			}
			//bps = data[13]
			if flags == "....S." {
				synFloodPacketCount += packetNum
			}
			if proto == "UDP" {
				udpPacketCount += packetNum
				udpByteCount += byteNum
			}
			if proto == "UDP" && (srcPort == "53" || dstPort == "53") {
				dnsPacketCount += packetNum
				dnsBytetCount += byteNum
			}

			if maxPacketNum < packetNum {
				maxPacketNum = packetNum
			}

			if maxByteNum < byteNum {
				maxByteNum = byteNum
			}
		}

		fmt.Println(synFloodPacketCount, udpPacketCount, udpByteCount, dnsPacketCount, dnsBytetCount, maxByteNum)
		switch {
		case maxByteNum > ELEPHANT_THRESHOLD:
			send(fmt.Sprintf("ElephantAttack:%f", maxByteNum))
		case synFloodPacketCount > SYNFLOOD_THREAHOLD:
			send(fmt.Sprintf("SYNFLOOD:%d", synFloodPacketCount))
		case dnsPacketCount > DNSFLOOOD_THRESHOLD:
			send(fmt.Sprintf("DNSFlood:%d", dnsPacketCount))
		case udpByteCount > UDPFLOOD_THREASHOLD:
			send(fmt.Sprintf("UDPFlood:%f", udpByteCount))
		}
	}
}

func send(command string) {
	servAddr := "192.168.123.201:6666"
	tcpAddr, err := net.ResolveTCPAddr("tcp", servAddr)
	if err != nil {
		println("ResolveTCPAddr failed:", err.Error())
		os.Exit(1)
	}

	conn, err := net.DialTCP("tcp", nil, tcpAddr)
	if err != nil {
		println("Dial failed:", err.Error())
		os.Exit(1)
	}

	_, err = conn.Write([]byte(command + "\n\n"))
	if err != nil {
		println("Write to server failed:", err.Error())
		os.Exit(1)
	}

	b := bufio.NewReader(conn)
	for {
		line, err := b.ReadBytes('\n')
		if len(line) == 1 {
			break
		}
		if err != nil { // EOF, or worse
			break
		}
		fmt.Println(string(line))
	}
	fmt.Println(command)
	cmd := exec.Command("date")
	run(cmd)
	fmt.Println("next")
	conn.Close()
}
