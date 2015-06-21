package main

import (
	"fmt"
	"io/ioutil"
	"os/exec"
	"strings"
)

const (
	INITIAL_VM_CAPACITY = 10
)

func run(command *exec.Cmd) string {
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
	if errResult != nil {
		fmt.Println(string(errResult[:]))
	}
	return string(result[:])
}

func getExistingOVS() []string {
	command := exec.Command("ovs-vsctl", "list-br")
	result := run(command)
	return strings.Split(strings.TrimRight(result, "\n"), "\n")
}

func getExistingVM() []string {
	command := exec.Command("virsh", "list")
	vms := make([]string, 0, INITIAL_VM_CAPACITY)
	result := run(command)
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
		if result := run(command); result != "" {
			fmt.Println(result)
		}
	}
}

func clearOVS(swes []string) {
	fmt.Println("Clear OVS")
	for _, sw := range swes {
		command := exec.Command("ovs-vsctl", "del-br", sw)
		if result := run(command); result != "" {
			fmt.Println(result)
		}
	}
}

func getExistingVTLink() []string {
	command := exec.Command("ifconfig", "-a")
	result := run(command)
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
		if result := run(command); result != "" {
			fmt.Println(result)
		}
	}
}

func show() {
	showOVS()
	showVM()
}

func showOVS() {
	fmt.Println("Show OVS")
	command := exec.Command("ovs-vsctl", "show")
	if result := run(command); result != "" {
		fmt.Println(result)
	}
}

func showVM() {
	fmt.Println("show VM")
	command := exec.Command("virsh", "list")
	if result := run(command); result != "" {
		fmt.Println(result)
	}
}

func main() {
	clearVM(getExistingVM())
	clearOVS(getExistingOVS())
	clearVTLink(getExistingVTLink())
	show()
}
