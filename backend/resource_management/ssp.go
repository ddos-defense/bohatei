package main

import (
	"container/heap"
	"fmt"
	"sort"
)

const (
	INTRA_SERVER_COST = 1
	INTRA_RACK_COST   = 2
	INTER_RACK_COST   = 10
)

type PhysicalMaps []PhysicalMap

type ServerPool []*Server

func (self ServerPool) Len() int      { return len(self) }
func (self ServerPool) Swap(i, j int) { self[i], self[j] = self[j], self[i] }
func (self ServerPool) Less(i, j int) bool {
	return self[i].VMs > self[j].VMs
}

func (self *ServerPool) Push(x interface{}) {
	*self = append(*self, x.(*Server))
}

func (self *ServerPool) Pop() interface{} {
	old := *self
	n := len(old)
	x := old[n-1]
	*self = old[0 : n-1]
	return x
}

type RackPool []*Rack

func (self RackPool) Len() int      { return len(self) }
func (self RackPool) Swap(i, j int) { self[i], self[j] = self[j], self[i] }
func (self RackPool) Less(i, j int) bool {
	return self[i].AvailableCapacity() > self[j].AvailableCapacity()
}

func (self *RackPool) Push(x interface{}) {
	*self = append(*self, x.(*Rack))
}

func (self *RackPool) Pop() interface{} {
	old := *self
	n := len(old)
	x := old[n-1]
	*self = old[0 : n-1]
	return x
}

type Server struct {
	VMs        int
	Name       int
	RackNum    int
	Chain      int
	AssginedVM map[string]int
}

type Rack struct {
	Name int
	//	Servers [SERVER_PER_RACK]Server
	Servers []Server
}

func (self Rack) AvailableCapacity() int {
	capacity := 0
	for index, _ := range self.Servers {
		capacity += self.Servers[index].VMs
	}
	return capacity
}

func (self *Rack) AssignVM(totalVM int, name int, assignedServer *([]Rack)) int {
	assignedRack := Rack{Name: self.Name}
	var remainVM int
	if totalVM > self.AvailableCapacity() {
		remainVM = totalVM - self.AvailableCapacity()
	} else {
		remainVM = 0
	}
	tmp := totalVM - remainVM
	var vm int
	for index, _ := range self.Servers {
		self.Servers[index].Chain = name
		if tmp > self.Servers[index].VMs {
			tmp -= self.Servers[index].VMs
			vm = self.Servers[index].VMs
			self.Servers[index].VMs = 0
			//assignedRack.Servers[self.Servers[index].Name] = Server{Name: self.Servers[index].Name, VMs: vm, RackNum: self.Servers[index].RackNum, Chain: name}
			assignedRack.Servers = append(assignedRack.Servers, Server{Name: self.Servers[index].Name, VMs: vm, RackNum: self.Servers[index].RackNum, Chain: name})
			//			fmt.Println(" ", self.Servers[index].RackNum, "_", self.Servers[index].Name, vm)
		} else {
			self.Servers[index].VMs -= tmp
			vm = tmp
			//			fmt.Println(" ", self.Servers[index].RackNum, "_", self.Servers[index].Name, vm)
			//assignedRack.Servers[self.Servers[index].Name] = Server{Name: self.Servers[index].Name, VMs: vm, RackNum: self.Servers[index].RackNum, Chain: name}
			assignedRack.Servers = append(assignedRack.Servers, Server{Name: self.Servers[index].Name, VMs: vm, RackNum: self.Servers[index].RackNum, Chain: name})
			break
		}
	}
	*assignedServer = append(*assignedServer, assignedRack)
	return remainVM
}

type PhysicalMap struct {
	name               int
	pMap               map[Module]int
	totalVM            int
	logicalChain       Chain
	assignedServer     []Rack
	assingedModuleName map[string]bool
	assignedModule     map[Module][]*Server
	totalVolume        float64
}

func NewPhysicalMap(name int, logicalChain Chain) *PhysicalMap {
	self := new(PhysicalMap)
	self.name = name
	self.pMap = make(map[Module]int)
	self.logicalChain = logicalChain
	self.assingedModuleName = make(map[string]bool)
	//	self.assignedModule = make(map[string][]*Server)
	self.assignedModule = make(map[Module][]*Server)
	for _, module := range self.logicalChain.sortedInstances {
		//	self.assignedModule[module.Name()] = make([]*Server, 0, SLICE_CAP)
		self.assignedModule[module] = make([]*Server, 0, SLICE_CAP)
	}
	return self
}

func (self PhysicalMap) OutputAssignedServers() {
	fmt.Println(self.name, self.totalVM)
	for _, rack := range self.assignedServer {
		fmt.Println("", "Rack", rack.Name)
		for _, server := range rack.Servers {
			if server.VMs != 0 {
				fmt.Println(" ", "Server", server.Name, server.VMs)
			}
		}
	}
}

func (self *PhysicalMap) AssignmentDone(current Module) {
	self.assingedModuleName[current.Name()] = true
}

func (self PhysicalMap) NextModule(current Module) Module {
	if current != nil && current.Predecessor() != nil {
		if _, ok := self.assingedModuleName[current.Predecessor().Name()]; !ok {
			return current.Predecessor()
		}
	}
	if len(self.logicalChain.sortedInstances) == 0 {
		return nil
	}
	module := self.logicalChain.sortedInstances[0]
	self.logicalChain.sortedInstances = self.logicalChain.sortedInstances[1:]
	for {
		if _, ok := self.assingedModuleName[module.Name()]; !ok {
			return module
		} else {
			if len(self.logicalChain.sortedInstances) == 0 {
				return nil
			}
			module = self.logicalChain.sortedInstances[0]
			self.logicalChain.sortedInstances = self.logicalChain.sortedInstances[1:]
		}
	}
}

func (self *PhysicalMap) AssignModuleToServer(current Module) {
	requiredVM := self.RequiredVM(current)
	remainVM := requiredVM
	serverPool := *(self.createServerPool())
	var rackPool RackPool
	var tmpVM int
	var assignedVM int
	var rack *Rack
	var server *Server
	heap.Init(&rackPool)
	for index, _ := range self.assignedServer {
		heap.Push(&rackPool, &(self.assignedServer[index]))
	}
	if len(serverPool) == 0 {
		fmt.Println("We cannot assign", current.Name())
		return
	}
	fmt.Println("Remain:", remainVM, current.Name())
	for remainVM > 0 {
		server = heap.Pop(&serverPool).(*Server)
		if server.VMs > remainVM {
			assignedVM = remainVM
			server.VMs -= remainVM
			remainVM = 0
			if server.VMs != 0 {
				heap.Push(&serverPool, server)
			}
			self.AssignServer(current, server, assignedVM)
		} else {
			heap.Push(&serverPool, server)
			for remainVM > 0 {
				rack = heap.Pop(&rackPool).(*Rack)
				if rack.AvailableCapacity() <= remainVM {
					tmpVM = rack.AvailableCapacity()
					remainVM -= rack.AvailableCapacity()
				} else {
					tmpVM = remainVM
					remainVM = 0
				}
				//				fmt.Println(tmpVM, rack.AvailableCapacity())
				for tmpVM > 0 {
					for s := 0; s < len(rack.Servers); s++ {
						if rack.Servers[s].VMs == 0 {
							continue
						}
						if tmpVM > rack.Servers[s].VMs {
							assignedVM = rack.Servers[s].VMs
							tmpVM -= rack.Servers[s].VMs
							rack.Servers[s].VMs = 0
							self.AssignServer(current, &(rack.Servers[s]), assignedVM)
						} else {
							assignedVM = tmpVM
							rack.Servers[s].VMs -= tmpVM
							tmpVM = 0
							self.AssignServer(current, &(rack.Servers[s]), assignedVM)
							break
						}
					}
				}
			}
			if rack.AvailableCapacity() != 0 {
				heap.Push(&rackPool, rack)
			}
			serverPool = (*self.createServerPool())
		}
	}
	return
}

func (self *PhysicalMap) AssignServer(current Module, server *Server, assignedVM int) {
	if assignedVM == 0 {
		panic("No VM Assign")
	}
	if server.AssginedVM == nil {
		server.AssginedVM = make(map[string]int)
	}
	server.AssginedVM[current.Name()] = assignedVM
	//	self.assignedModule[current.Name()] = append(self.assignedModule[current.Name()], server)
	self.assignedModule[current] = append(self.assignedModule[current], server)
}

func (self PhysicalMap) RequiredVM(current Module) int {
	var requiredVM int
	for module, num := range self.pMap {
		if module.Name() == current.Name() {
			requiredVM = num
			break
		}
	}
	return requiredVM
}

func (self PhysicalMap) createServerPool() *ServerPool {
	var pool ServerPool
	heap.Init(&pool)
	for i, _ := range self.assignedServer {
		for j, _ := range self.assignedServer[i].Servers {
			if self.assignedServer[i].Servers[j].VMs != 0 {
				heap.Push(&pool, &(self.assignedServer[i].Servers[j]))
			}
		}
	}
	return &pool
}

func (self PhysicalMaps) Len() int {
	return len(self)
}

func (self PhysicalMaps) Swap(i, j int) {
	self[i], self[j] = self[j], self[i]
}

func (self PhysicalMaps) Less(i, j int) bool {
	return self[i].totalVM > self[j].totalVM
}

func (self *DC) createServerPool() *ServerPool {
	var pool ServerPool
	heap.Init(&pool)
	for index, _ := range self.Racks {
		for i, _ := range self.Racks[index].Servers {
			if self.Racks[index].Servers[i].VMs != 0 {
				heap.Push(&pool, &(self.Racks[index].Servers[i]))
			}
		}
	}
	return &pool
}

func (self *DC) SSP(physicalMaps PhysicalMaps) float64 {
	sort.Sort(physicalMaps)
	totalCost := 0.0
	fmt.Println(self.Name)
	var serverPool ServerPool
	var rackPool RackPool
	heap.Init(&serverPool)
	heap.Init(&rackPool)
	for index, _ := range self.Racks {
		heap.Push(&rackPool, &(self.Racks[index]))
		for i, _ := range self.Racks[index].Servers {
			heap.Push(&serverPool, &(self.Racks[index].Servers[i]))
		}
	}
	var assignedPhysicalMap PhysicalMaps

	for _, pMap := range physicalMaps {
		if pMap.totalVM == 0 {
			fmt.Printf("DC %d dones't handle attack %d\n", self.Name, pMap.name)
			continue
		}
		if len(serverPool) == 0 {
			panic("No Server available")
		}
		server := heap.Pop(&serverPool).(*Server)

		if pMap.totalVM <= server.VMs {
			fmt.Printf("We can store evry VM for tree %d # of vms %d in rack %d server %d\n", pMap.name, pMap.totalVM, server.RackNum, server.Name)
			//			fmt.Println("  ", server.RackNum, "_", server.Name, pMap.totalVM)
			pMap.assignedServer = append(pMap.assignedServer, Rack{Name: server.RackNum})
			//			pMap.assignedServer[0].Servers[server.Name] = Server{Name: server.Name, RackNum: server.RackNum, Chain: pMap.name, VMs: pMap.totalVM}
			pMap.assignedServer[0].Servers = append(pMap.assignedServer[0].Servers, Server{Name: server.Name, RackNum: server.RackNum, Chain: pMap.name, VMs: pMap.totalVM})
			server.VMs -= pMap.totalVM
			server.Chain = pMap.name
			heap.Push(&serverPool, server)
		} else {
			heap.Push(&serverPool, server)
			remainVM := pMap.totalVM
			orgVM := pMap.totalVM
			for remainVM != 0 {
				rack := heap.Pop(&rackPool).(*Rack)
				remainVM = rack.AssignVM(orgVM, pMap.name, &(pMap.assignedServer))
				fmt.Printf("We stored VM for tree %d # of vms %d/%d in rack %d \n", pMap.name, orgVM-remainVM, pMap.totalVM, rack.Name)
				orgVM = remainVM
				if rack.AvailableCapacity() != 0 {
					heap.Push(&rackPool, rack)
				}
			}
			serverPool = *(self.createServerPool())
		}
		assignedPhysicalMap = append(assignedPhysicalMap, pMap)
	}

	for _, pMap := range assignedPhysicalMap {
		cost := 0.0
		module := pMap.NextModule(nil)
		for module != nil {
			fmt.Println("Next is", module.Name(), module.Capacity())
			pMap.AssignModuleToServer(module)
			fmt.Println(module.Name(), "is done")
			pMap.AssignmentDone(module)
			module = pMap.NextModule(module)
		}

		for module, servers := range pMap.assignedModule {
			switch module.Type() {
			case "A":
				//					next_A := module.(A_Module).next_A
				next_R := module.(A_Module).next_r
				if next_R == nil {
					continue
				}
				cost += self.calcCost(pMap, module, next_R, servers)

				next_A := module.(A_Module).next_A
				if next_A == nil {
					continue
				}
				cost += self.calcCost(pMap, module, next_A, servers)
			case "R":
				next_R := module.(R_Module).next_r
				if next_R == nil {
					continue
				}
				cost += self.calcCost(pMap, module, next_R, servers)
			}
		}
		fmt.Println(pMap.name, "is totallly assigned", cost)
		totalCost += cost
	}
	return totalCost
}

func (self DC) calcCost(pMap PhysicalMap, current, next Module, servers []*Server) float64 {
	cost := 0.0
	bandwidth := pMap.totalVolume * next.Fraction()
	var numLocal, numRemoete int
	for m, num := range pMap.pMap {
		if m.Name() == current.Name() {
			numLocal = num
		}
		if m.Name() == next.Name() {
			numRemoete = num
		}
	}
	bandwidth /= float64((numLocal * numRemoete))
	for _, localServer := range servers {
		fmt.Println("local", localServer.RackNum, localServer.Name)
		nextModule := Module(nil)
		for m, _ := range pMap.assignedModule {
			if m.Name() == next.Name() {
				nextModule = m
				break
			}
		}
		if nextModule == nil {
			panic("Why Next module is nil")
		}
		for _, remoteServer := range pMap.assignedModule[nextModule] {
			if localServer.RackNum == remoteServer.RackNum {
				if localServer.Name == remoteServer.Name {
					fmt.Println("INTRA SERVER")
					cost += bandwidth * INTRA_SERVER_COST * float64(localServer.AssginedVM[current.Name()]*remoteServer.AssginedVM[next.Name()])
				} else {
					fmt.Println("INTRA RACK")
					cost += bandwidth * INTRA_RACK_COST * float64(localServer.AssginedVM[current.Name()]*remoteServer.AssginedVM[next.Name()])
				}
			} else {
				fmt.Println("INTER RACK")
				cost += bandwidth * INTER_RACK_COST * float64(localServer.AssginedVM[current.Name()]*remoteServer.AssginedVM[next.Name()])
			}
		}
	}
	return cost
}
