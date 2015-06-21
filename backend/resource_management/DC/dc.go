package main

import (
	"encoding/json"
	"fmt"
	"math"
	"math/rand"
	"os"
	"sort"
	"strconv"
	"time"
)

const (
	MAX_RACK_NUM        = 2
	MAX_SERVER_CAPACITY = 10
	SERVER_PER_RACK     = 10
	DC_CAPACITY         = 40
)

type Server struct {
	VMs     int
	Name    int
	RackNum int
}

type Rack struct {
	Servers [SERVER_PER_RACK]Server
	Name    int
}

type DCCapcity struct {
	Network int
	VM      int
}

type Cost struct {
	Ingress int
	Cost    int
}

type DC struct {
	Name     int
	Capacity DCCapcity
	Costs    []Cost
	Racks    []Rack
}

type PhysicalMaps []PhysicalMap

func (self PhysicalMaps) Len() int {
	return len(self)
}

func (self PhysicalMaps) Swap(i, j int) {
	self[i], self[j] = self[j], self[i]
}

func (self PhysicalMaps) Less(i, j int) bool {
	return self[i].totalVM < self[j].totalVM
}

func (self DC) SSP(physicalMaps PhysicalMaps) {
	sort.Sort(physicalMaps)
	for _, physicalMap := range physicalMaps {
		fmt.Println(physicalMap.totalVM, physicalMap.name)
	}
}

type Module interface {
	elasticScale(volume float64) int
	Type() string
	Capacity() float64
	Name() string
	String() string
}

type PhysicalMap struct {
	name    int
	pMap    map[Module]int
	totalVM int
}

func main() {
	dc := new(DC)
	rand.Seed(time.Now().UnixNano())
	name, _ := strconv.ParseInt(os.Args[1], 10, 32)
	ingress, _ := strconv.ParseInt(os.Args[2], 10, 32)
	dc.Name = int(name)
	dc.Capacity = *new(DCCapcity)
	dc.Capacity.Network = 1000
	dc.Capacity.VM = DC_CAPACITY
	dc.Costs = make([]Cost, 0, ingress)
	dc.Racks = make([]Rack, 0, 0)
	var remainVM = dc.Capacity.VM
	for i := 0; i < int(math.Ceil(float64(dc.Capacity.VM)/(MAX_SERVER_CAPACITY*SERVER_PER_RACK))); i++ {
		var rack Rack
		for index, _ := range rack.Servers {
			rack.Servers[index].Name = index
			rack.Servers[index].RackNum = i + 1
			if remainVM == 0 {
				continue
			}
			if remainVM > MAX_SERVER_CAPACITY {
				rack.Servers[index].VMs = MAX_SERVER_CAPACITY
				remainVM -= MAX_SERVER_CAPACITY
			} else {
				rack.Servers[index].VMs = remainVM
				remainVM = 0
			}
			//rack.Servers[index].VMs = rand.Intn(MAX_SERVER_CAPACITY)
		}
		rack.Name = i + 1
		dc.Racks = append(dc.Racks, rack)
	}

	for i := 1; i <= int(ingress); i++ {
		dc.Costs = append(dc.Costs, Cost{Ingress: i, Cost: rand.Intn(100)})
	}

	b, err := json.MarshalIndent(dc, "", "  ")
	if err != nil {
		fmt.Println("error:", err)
	}
	os.Stdout.Write(b)
}
