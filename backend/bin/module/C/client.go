package main

import (
	"bufio"
	"fmt"
	"net"
	"os"
)

func main() {
	strEcho := "Halo\n\n"
	servAddr := "192.168.123.202:6666"
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

	_, err = conn.Write([]byte(strEcho))
	if err != nil {
		println("Write to server failed:", err.Error())
		os.Exit(1)
	}

	b := bufio.NewReader(conn)
	for {
		line, err := b.ReadBytes('\n')
		if len(line) == 0 {
			break
		}
		if err != nil { // EOF, or worse
			break
		}
		fmt.Println(string(line))
	}
	conn.Close()
}
