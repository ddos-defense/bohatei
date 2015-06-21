package main

import (
	"bufio"
	"container/heap"
	"encoding/json"
	"fmt"
	"io/ioutil"
	"math"
	"os"
	"sort"
	"strconv"
	"strings"
)

const (
	MAX_RACK_NUM        = 100
	MAX_SERVER_CAPACITY = 50
	SERVER_PER_RACK     = 40.0
	DSP_WEIGHT          = 10000
)

/*
type Server struct {
	VMs int
}

type Rack struct {
	Servers [SERVER_PER_RACK]Server
}
*/

var dc map[int]*DC
var led []LED
var l []([]int)
var sortedLED map[int]LEDs
var param Param
var sortedT *Ts
var a_modules map[string]*A_Module
var r_moduels map[string]*R_Module
var logicalChains map[int]*Chain
var allPhysicalMaps map[int]map[int]map[Module]int
var allAttackVolume map[int]map[int]float64

const (
	SLICE_CAP   = 100
	ATTTACK_DIR = "Attack"
	ATTACK_FILE = "Attack"
	CHAIN_DIR   = "Chain"
	CHAIN_FILE  = "Chain"
	DC_DIR      = "DC"
	DC_FILE     = "DC"
	CONFIG      = "config.json"
)

type Info struct {
	vm      int
	traffic float64
}

type LED struct {
	ingress int
	dc      int
	cost    int
}

type T struct {
	ingress int
	attack  int
	volume  float64
}

func (self T) String() string {
	return fmt.Sprintf("Ingress:%d, Attack:%d, Volume:%d", self.ingress, self.attack, self.volume)
}

type Ts []T

func (self Ts) Len() int           { return len(self) }
func (self Ts) Swap(i, j int)      { self[i], self[j] = self[j], self[i] }
func (self Ts) Less(i, j int) bool { return self[i].volume > self[j].volume }

func (self *Ts) Push(x interface{}) {
	*self = append(*self, x.(T))
}

func (self *Ts) Pop() interface{} {
	old := *self
	n := len(old)
	x := old[n-1]
	*self = old[0 : n-1]
	return x
}

type LEDs []LED

func (self LEDs) Len() int {
	return len(self)
}

func (self LEDs) Swap(i, j int) {
	self[i], self[j] = self[j], self[i]
}

func (self LEDs) Less(i, j int) bool {
	return self[i].cost < self[j].cost
}

type Modules []Module

func (self Modules) Len() int {
	return len(self)
}

func (self Modules) Swap(i, j int) {
	self[i], self[j] = self[j], self[i]
}

func (self Modules) Less(i, j int) bool {
	return self[i].Capacity() > self[j].Capacity()
}

type DCCapcity struct {
	Network float64
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

func (self DC) physicalMap(attack int, volume float64) (map[Module]int, int) {
	physicalMap := make(map[Module]int)
	chain := logicalChains[attack]
	var sum int = 0
	for i := 0; i < len(chain.instances); i++ {
		n := int(math.Ceil(volume * chain.trafficFractions[i] / float64(chain.capacities[i])))
		//		num[chain.instances[i].Name()] = n
		physicalMap[chain.instances[i]] = n
		sum += n
	}
	return physicalMap, sum
}

func (self DC) computeCapcityTraffic(attack int, volume float64) float64 {
	computeLimit := math.MaxFloat64
	chain := logicalChains[attack]
	//	var numInstance []int

	/*
		/* This man decide some moudle as 0
		for index, _ := range chain.instances {
			//	numInstance = append(numInstance, int(float64(self.Capacity.VM)*chain.weights[index]))
			numVM := int(float64(self.Capacity.VM) * chain.weights[index])
			traffic := (float64(numVM) * chain.capacities[index]) / chain.trafficFractions[index]
			if computeLimit > traffic {
				computeLimit = traffic
			}
		}
	*/

	// we assign every module as least 1 module
	if self.Capacity.VM < len(chain.instances) {
		return 0.0
	}
	for index, _ := range chain.instances {
		//	numInstance = append(numInstance, int(float64(self.Capacity.VM)*chain.weights[index]))
		//		numVM := int(float64(self.Capacity.VM-len(chain.instances))*chain.weights[index]) + 1
		numVM := math.Ceil(float64(self.Capacity.VM-len(chain.instances))*chain.weights[index]) + 1
		traffic := (float64(numVM) * chain.capacities[index]) / chain.trafficFractions[index]
		if computeLimit > traffic {
			computeLimit = traffic
		}
	}
	return computeLimit
}

func (self *DC) assignTraffic(attack int, volume float64) (float64, map[Module]int) {
	var limit float64

	/*
		var numInstance []int
		computeLimit := math.MaxFloat64
		chain := logicalChains[attack]

		for index, _ := range chain.instances {
			numInstance = append(numInstance, int(float64(self.Capacity.VM)*chain.weights[index]))
			traffic := (float64(numInstance[index]) * chain.capacities[index]) / chain.trafficFractions[index]
			if computeLimit > traffic {
				computeLimit = traffic
			}
		}
	*/
	computeLimit := self.computeCapcityTraffic(attack, volume)

	if self.Capacity.Network < computeLimit {
		limit = self.Capacity.Network
	} else {
		limit = computeLimit
	}

	var physicalMap map[Module]int
	var sumVMNum int
	var remains float64
	if volume < limit {
		// We can handle every attack
		physicalMap, sumVMNum = self.physicalMap(attack, volume)
		self.Capacity.Network -= volume
		self.Capacity.VM -= sumVMNum
		remains = 0.0
	} else {
		if computeLimit > self.Capacity.Network {
			self.Capacity.Network = 0.0
			physicalMap, sumVMNum = self.physicalMap(attack, limit)
			self.Capacity.VM -= sumVMNum
		} else {
			self.Capacity.Network -= limit
			physicalMap, sumVMNum = self.physicalMap(attack, limit)
			self.Capacity.VM -= sumVMNum
		}
		remains = volume - limit
	}
	return remains, physicalMap

}

type Param struct {
	DC      int
	Ingress int
	Attack  int
}

type Attack struct {
	Name   int
	Volume []AttackVolume
}

type AttackVolume struct {
	Ingress int
	Volume  float64
}

type Chain struct {
	name             int // name=1 means program for attack1
	initModule       *A_Module
	instances        []Module
	capacities       []float64
	trafficFractions []float64
	weights          []float64
	sortedInstances  Modules
}

func NewChain(name int) *Chain {
	self := new(Chain)
	self.name = name
	self.initModule = a_modules[fmt.Sprintf("A%d1", name)]
	module := self.initModule
	fraction := 1.0
	var sumWeight float64
	self.instances = append(self.instances, Module(module))
	self.capacities = append(self.capacities, module.capacity)
	self.trafficFractions = append(self.trafficFractions, fraction)
	module.fraction = fraction
	sumWeight += fraction / module.capacity
	sumWeight += module.SetUP_R(Module(self.initModule), fraction, &self.instances, &self.capacities, &self.trafficFractions)
	oldModule := module
	module = module.next_A
	for module != nil {
		module.SetPredecessor(Module(oldModule))
		fraction *= oldModule.ratioNonAttack
		self.instances = append(self.instances, Module(module))
		self.capacities = append(self.capacities, module.capacity)
		self.trafficFractions = append(self.trafficFractions, fraction)
		module.fraction = fraction
		sumWeight += fraction / module.capacity
		sumWeight += module.SetUP_R(Module(module), fraction, &self.instances, &self.capacities, &self.trafficFractions)
		oldModule = module
		module = module.next_A
	}

	for index, _ := range self.instances {
		self.weights = append(self.weights, self.trafficFractions[index]/self.capacities[index]/sumWeight)
	}

	// Debug for Toby
	/*
		fmt.Println("before Sort")
		for i := 0; i < len(self.instances); i++ {
			fmt.Println(self.instances[i].Name())
		}
	*/

	for i := 0; i < len(self.instances); i++ {
		var tmp_A A_Module
		var tmp_R R_Module
		switch self.instances[i].Type() {
		case "A":
			tmp_A = *(self.instances[i].(*A_Module))
			self.sortedInstances = append(self.sortedInstances, Module(tmp_A))
		case "R":
			tmp_R = *(self.instances[i].(*R_Module))
			self.sortedInstances = append(self.sortedInstances, Module(tmp_R))
		}
	}

	sort.Sort(self.sortedInstances)
	/*
		for i := 0; i < len(self.instances); i++ {
			fmt.Println(self.instances[i].Name())
		}
		fmt.Println("sorted instances")
		for i := 0; i < len(self.instances); i++ {
			fmt.Println(self.sortedInstances[i].Name())
		}
	*/

	return self
}

func (self Chain) Output() {
	fmt.Println(self.name)
	for _, module := range self.instances {
		fmt.Printf("%s ", module.Name())
	}
	fmt.Println("")
	fmt.Printf(" %d:%v\n", len(self.capacities), self.capacities)
	fmt.Printf(" %d:%v\n", len(self.trafficFractions), self.trafficFractions)
}

func (self Chain) OutputPredecessor() {
	fmt.Println(self.name)
	for _, module := range self.instances {
		if module.Predecessor() == nil {
			fmt.Println(module.Name(), "<-", "NULL")
		} else {
			fmt.Println(module.Name(), "<-", module.Predecessor().Name())
		}
	}
}

type Module interface {
	elasticScale(volume float64) int
	Type() string
	Capacity() float64
	Name() string
	String() string
	Predecessor() Module
	Fraction() float64
}

type A_Module struct {
	name           string
	capacity       float64
	next_A         *A_Module
	next_r         *R_Module
	ratioAttack    float64
	ratioNonAttack float64
	predecessor    Module
	fraction       float64
}

func (self A_Module) String() string {
	return self.name
}

func (self A_Module) Predecessor() Module {
	return self.predecessor
}

func (self *A_Module) SetPredecessor(predecessor Module) {
	self.predecessor = predecessor
}

func (self A_Module) elasticScale(volume float64) int {
	return int(math.Ceil(volume / self.capacity))
}

func (self A_Module) Type() string {
	return "A"
}

func (self A_Module) Name() string {
	return self.name
}

func (self A_Module) Capacity() float64 {
	return self.capacity
}

func (self A_Module) Fraction() float64 {
	return self.fraction
}

func (self A_Module) SetUP_R(predecessor Module, fraction float64, instances *[]Module, capacities *[]float64, trafficFractions *[]float64) float64 {
	var sumWeight float64
	module := self.next_r
	fraction = self.ratioAttack * fraction
	for module != nil {
		module.SetPredecessor(predecessor)
		*instances = append(*instances, Module(module))
		*capacities = append(*capacities, module.capacity)
		*trafficFractions = append(*trafficFractions, fraction)
		module.fraction = fraction
		sumWeight += fraction / module.capacity
		module = module.next_r
	}
	return sumWeight
}

func (self A_Module) Output() {
	fmt.Println(self.name)
	r_module := self.next_r
	fmt.Println(" ", self.ratioAttack)
	for r_module != nil {
		fmt.Println(" ", r_module.name)
		r_module = r_module.next_r
	}
}

type R_Module struct {
	name        string
	capacity    float64
	next_r      *R_Module
	predecessor Module
	fraction    float64
}

func (self R_Module) String() string {
	return self.name
}

func (self R_Module) Predecessor() Module {
	return self.predecessor
}

func (self *R_Module) SetPredecessor(predecessor Module) {
	self.predecessor = predecessor
}

func (self R_Module) elasticScale(volume float64) int {
	return int(math.Ceil(volume / self.Capacity()))
}

func (self R_Module) Type() string {
	return "R"
}

func (self R_Module) Name() string {
	return self.name
}

func (self R_Module) Capacity() float64 {
	return self.capacity
}

func (self R_Module) Fraction() float64 {
	return self.fraction
}

func readDC() {
	fileInfos, err := ioutil.ReadDir(DC_DIR)
	if err != nil {
		panic(err)
	}
	for _, fileInfo := range fileInfos {
		if strings.Contains(fileInfo.Name(), DC_FILE) {
			var d DC
			file, err := os.Open(DC_DIR + "/" + fileInfo.Name())
			if err != nil {
				panic(err)
			}
			decoder := json.NewDecoder(file)
			decoder.Decode(&d)
			dc[d.Name] = &d
			for _, cost := range d.Costs {
				led = append(led, LED{ingress: cost.Ingress, dc: d.Name, cost: cost.Cost})
			}
		}
	}
}

func readAttack() {
	fileInfos, err := ioutil.ReadDir(ATTTACK_DIR)
	if err != nil {
		panic(err)
	}
	for _, fileInfo := range fileInfos {
		if strings.Contains(fileInfo.Name(), ATTACK_FILE) {
			var attack Attack
			file, err := os.Open(ATTTACK_DIR + "/" + fileInfo.Name())
			if err != nil {
				panic(err)
			}
			decoder := json.NewDecoder(file)
			decoder.Decode(&attack)
			for _, attackVolume := range attack.Volume {
				if attackVolume.Volume != 0 {
					heap.Push(sortedT, T{attack: attack.Name, ingress: attackVolume.Ingress, volume: attackVolume.Volume})
				}
			}
		}
	}
}

func readChain() {
	fileInfos, err := ioutil.ReadDir(CHAIN_DIR)
	if err != nil {
		panic(err)
	}
	for _, fileInfo := range fileInfos {
		if strings.Contains(fileInfo.Name(), CHAIN_FILE) {
			file, err := os.Open(CHAIN_DIR + "/" + fileInfo.Name())
			if err != nil {
				panic(err)
			}
			scanner := bufio.NewScanner(file)
			for scanner.Scan() {
				line := scanner.Text()
				if strings.Contains(line, "#") {
					continue
				}
				info := strings.Fields(line)
				switch len(info) {
				case 2:
					capacity, err := strconv.ParseFloat(info[1], 64)
					if err != nil {
						panic(err)
					}
					switch {
					case strings.HasPrefix(info[0], "A"):
						module, ok := a_modules[info[0]]
						if !ok {
							a_modules[info[0]] = &A_Module{name: info[0], capacity: capacity}
						} else {
							module.capacity = capacity
						}
					case strings.HasPrefix(info[0], "R"):
						module, ok := r_moduels[info[0]]
						if !ok {
							r_moduels[info[0]] = &R_Module{name: info[0], capacity: capacity}
						} else {
							module.capacity = capacity
						}
					}
				case 3:
					ratio, err := strconv.ParseFloat(info[2], 64)
					if err != nil {
						panic(err)
					}
					switch {
					case strings.HasPrefix(info[0], "A"):
						module := getAModule(info[0])
						switch {
						case strings.HasPrefix(info[1], "A"):
							module.next_A = getAModule(info[1])
							module.ratioNonAttack = ratio
						case strings.HasPrefix(info[1], "R"):
							module.next_r = getRModule(info[1])
							module.ratioAttack = ratio
						}
					case strings.HasPrefix(info[0], "R"):
						module := getRModule(info[0])
						switch {
						case strings.HasPrefix(info[1], "R"):
							module.next_r = getRModule(info[1])
						}
					}
				}
			}
		}
	}
}

func getAModule(name string) *A_Module {
	module, ok := a_modules[name]
	if !ok {
		a_modules[name] = &A_Module{name: name}
		return a_modules[name]
	}
	return module
}

func getRModule(name string) *R_Module {
	module, ok := r_moduels[name]
	if !ok {
		r_moduels[name] = &R_Module{name: name}
		return r_moduels[name]
	}
	return module
}

func readConf() {
	file, err := os.Open(CONFIG)
	if err != nil {
		panic(err)
	}
	decoder := json.NewDecoder(file)
	decoder.Decode(&param)
}

func sortLED() {
	for _, led := range led {
		e := led.ingress
		sortedLED[e] = append(sortedLED[e], led)
	}
	for _, leds := range sortedLED {
		sort.Sort(leds)
	}
}

func prepareData() {
	readConf()
	sortedT = new(Ts)
	heap.Init(sortedT)
	dc = make(map[int]*DC)
	led = make([]LED, 0, SLICE_CAP)
	sortedLED = make(map[int]LEDs)
	for e := 1; e <= param.Ingress; e++ {
		sortedLED[e] = *new(LEDs)
	}
	readDC()
	sortLED()
	readAttack()
}

func prepareChain() {
	for i := 1; i <= param.Attack; i++ {
		logicalChains[i] = NewChain(i)
	}
}

func main() {
	a_modules = make(map[string]*A_Module)
	r_moduels = make(map[string]*R_Module)
	logicalChains = make(map[int]*Chain)
	prepareData()
	readChain()
	prepareChain()
	remainFlag := false
	allPhysicalMaps = make(map[int]map[int]map[Module]int)
	allAttackVolume = make(map[int]map[int]float64)
	dspCost := 0.0
	sspCost := 0.0

	for _, dc := range dc {
		allPhysicalMaps[dc.Name] = make(map[int]map[Module]int)
		allAttackVolume[dc.Name] = make(map[int]float64)
		for _, chain := range logicalChains {
			allPhysicalMaps[dc.Name][chain.name] = make(map[Module]int)
		}
	}
	for len(*sortedT) != 0 {
		t := heap.Pop(sortedT).(T)
		e := t.ingress
		remainFlag = true
		for _, led := range sortedLED[e] {
			newT, physicalMap := dc[led.dc].assignTraffic(t.attack, t.volume)
			if newT != t.volume {
				fmt.Println("ingress", e, "DC", led.dc, "attack-type", t.attack, "Total Volume", t.volume, "assigned Volme", t.volume-newT, physicalMap, dc[led.dc].Capacity.Network, dc[led.dc].Capacity.VM)
				for module, num := range physicalMap {
					allPhysicalMaps[led.dc][t.attack][module] += num
				}
				dspCost += (t.volume - newT) * float64(led.cost)
				allAttackVolume[led.dc][t.attack] += t.volume - newT
				t.volume = newT
				if newT != 0 {
					heap.Push(sortedT, t)
				}
				remainFlag = false
				break
			}
		}
		if t.volume != 0 && remainFlag == true {
			fmt.Println("We cannot assign any more for ", t)
		}
	}

	physicalMaps := make(map[int]PhysicalMaps)
	for dcNum, d := range allPhysicalMaps {
		pMaps := PhysicalMaps{}
		dcVM := 0
		fmt.Printf("DC%d\n", dcNum)
		for chainNum, chain := range d {
			fmt.Printf(" Attack%d\n", chainNum)
			pMap := NewPhysicalMap(chainNum, *(logicalChains[chainNum]))
			chainVM := 0
			for module, num := range chain {
				fmt.Println(" ", module.Name(), num)
				pMap.pMap[module] += num
				dcVM += num
				chainVM += num

			}
			pMap.totalVolume = allAttackVolume[dcNum][chainNum]
			pMap.totalVM = chainVM
			pMaps = append(pMaps, *pMap)
			fmt.Printf("  %d\n", chainVM)
		}
		physicalMaps[dcNum] = pMaps
		fmt.Println(dcNum, dcVM)
		fmt.Println("")
	}

	for dcNum, _ := range dc {
		sspCost += dc[dcNum].SSP(physicalMaps[dcNum])
	}
	fmt.Println("Cost:", DSP_WEIGHT*dspCost+sspCost, DSP_WEIGHT*dspCost, sspCost)
}
