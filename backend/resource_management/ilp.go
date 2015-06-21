package main

import (
	"bufio"
	"bytes"
	"encoding/json"
	"fmt"
	"io/ioutil"
	"math"
	"os"
	"strconv"
	"strings"
)

const (
	SLICE_CAP   = 100
	ATTTACK_DIR = "Attack"
	ATTACK_FILE = "Attack"
	CHAIN_DIR   = "Chain"
	CHAIN_FILE  = "Chain"
	DC_DIR      = "DC"
	DC_FILE     = "DC"
	CONFIG      = "config.json"
	F_ead       = "f_%d_%d_%d"
	T_ad        = "t_%d_%d"
	N_dsai      = "n_%d_%s_%d_%d"
	DSC_d       = "dsc_%d"
	//Q_daivm1sjvm2s2l = "q_%d_%d_%d_%d_%s_%d_%d_%s_%d"
	Q_daivm1sjvm2s2l  = "q_%d%d%d%d%s%d%d%s%d"
	INTRA_SERVER_COST = 1
	INTRA_TOR_COST    = 2
	INTER_COST        = 10
	DSP_WEIGHT        = 10000
	INTRA_s_d         = "intra_s_%d"
	INTRA_d           = "intra_%d"
	INTER_d           = "inter_%d"
	MAX_L             = 10
	MAX_VM            = 200
)

var L map[int]map[int]int
var logicalChains map[int]*Chain
var a_modules map[string]*A_Module
var r_moduels map[string]*R_Module
var dcs map[int]*DC
var attacks map[int]*Attack
var param Param
var dsc map[int]int

type Param struct {
	DC      int
	Ingress int
	Attack  int
}

type Attack struct {
	Name   int
	Volume []AttackVolume
	T      map[int]float64
}

type AttackVolume struct {
	Ingress int
	Volume  float64
}

type DC struct {
	Name     int
	Capacity DCCapcity
	Costs    []Cost
	Racks    []Rack
}

type Server struct {
	VMs     int
	Name    int
	RackNum int
	Chain   int
}

func (self Server) Id() string {
	return fmt.Sprintf("%d_%d", self.RackNum, self.Name)
}

type DCCapcity struct {
	Network float64
	VM      int
}

type Cost struct {
	Ingress int
	Cost    int
}

type Rack struct {
	Name    int
	Servers []Server
}

func prepareChain() {
	for i := 1; i <= param.Attack; i++ {
		logicalChains[i] = NewChain(i)
	}
}

type LogicalEdge struct {
	i, j int
}

type Chain struct {
	name             int // name=1 means program for attack1
	initModule       *A_Module
	instances        []Module
	capacities       []float64
	trafficFractions []float64
	weights          []float64
	logicalEdges     []LogicalEdge
}

func (self Chain) Toby() {
	fmt.Println(self.logicalEdges)
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
	sumWeight += fraction / module.capacity
	sumWeight += module.SetUP_R(fraction, &self.instances, &self.capacities, &self.trafficFractions, &self.logicalEdges)
	oldModule := module
	module = module.next_A
	for module != nil {
		fraction *= oldModule.ratioNonAttack
		self.instances = append(self.instances, Module(module))
		self.capacities = append(self.capacities, module.capacity)
		self.trafficFractions = append(self.trafficFractions, fraction)
		sumWeight += fraction / module.capacity
		sumWeight += module.SetUP_R(fraction, &self.instances, &self.capacities, &self.trafficFractions, &self.logicalEdges)
		oldModule = module
		module = module.next_A
	}

	for index, _ := range self.instances {
		self.weights = append(self.weights, self.trafficFractions[index]/self.capacities[index]/sumWeight)
	}
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

type Module interface {
	elasticScale(volume float64) int
	Type() string
	Capacity() float64
	Name() string
	String() string
}

type A_Module struct {
	name           string
	capacity       float64
	next_A         *A_Module
	next_r         *R_Module
	ratioAttack    float64
	ratioNonAttack float64
}

func (self A_Module) String() string {
	return self.name
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

func (self A_Module) SetUP_R(fraction float64, instances *[]Module, capacities *[]float64, trafficFractions *[]float64, logicalEdges *[]LogicalEdge) float64 {
	var sumWeight float64
	module := self.next_r
	fraction = self.ratioAttack * fraction
	for module != nil {
		*instances = append(*instances, Module(module))
		*capacities = append(*capacities, module.capacity)
		*trafficFractions = append(*trafficFractions, fraction)
		sumWeight += fraction / module.capacity
		module = module.next_r
		*logicalEdges = append(*logicalEdges, LogicalEdge{i: len(*instances) - 2, j: len(*instances) - 1})
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
	name     string
	capacity float64
	next_r   *R_Module
}

func (self R_Module) String() string {
	return self.name
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

func readParam() {
	file, err := os.Open(CONFIG)
	if err != nil {
		panic(err)
	}
	decoder := json.NewDecoder(file)
	decoder.Decode(&param)
}

func readAttack() {
	fileInfos, err := ioutil.ReadDir(ATTTACK_DIR)
	if err != nil {
		panic(err)
	}
	for _, fileInfo := range fileInfos {
		if strings.Contains(fileInfo.Name(), ATTACK_FILE) {
			var attack Attack
			attack.T = make(map[int]float64)
			file, err := os.Open(ATTTACK_DIR + "/" + fileInfo.Name())
			if err != nil {
				panic(err)
			}
			decoder := json.NewDecoder(file)
			decoder.Decode(&attack)
			for _, attackVolume := range attack.Volume {
				attack.T[attackVolume.Ingress] = attackVolume.Volume
			}
			attacks[attack.Name] = &attack
		}
	}
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
			dcs[d.Name] = &d
			for _, cost := range d.Costs {
				L[cost.Ingress][d.Name] = cost.Cost
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

func objective() {
	fmt.Print("min ")
	for e := 1; e <= param.Ingress; e++ {
		for a := 1; a <= param.Attack; a++ {
			for d := 1; d <= param.DC; d++ {
				//fmt.Printf(F_ead+"  %d  %d + ", e, a, d, int(attacks[a].T[e]), L[e][d])
				fmt.Printf("%d"+F_ead+" + ", int(attacks[a].T[e])*L[e][d]*DSP_WEIGHT, e, a, d)
			}
		}
	}
	for d := 1; d <= param.DC-1; d++ {
		fmt.Printf(DSC_d+" + ", d)
	}
	fmt.Printf(DSC_d+"\n", param.DC)
}

func formula2() {
	for e := 1; e <= param.Ingress; e++ {
		for a := 1; a <= param.Attack; a++ {
			for d := 1; d < param.DC; d++ {
				fmt.Printf(F_ead+" + ", e, a, d)
			}
			fmt.Printf(F_ead+" = 1\n", e, a, param.DC)
		}
	}
}

func formula3() {
	for a := 1; a <= param.Attack; a++ {
		for d := 1; d <= param.DC; d++ {
			left := ""
			//fmt.Printf("t_%d_%d - ", a, d)
			left += fmt.Sprintf("t_%d_%d - ", a, d)
			for e := 1; e <= param.Ingress; e++ {
				//fmt.Printf("%d"+F_ead+" - ", int(attacks[a].T[e]), e, a, d)
				left += fmt.Sprintf("%d"+F_ead+" - ", int(attacks[a].T[e]), e, a, d)
			}
			left = strings.TrimRight(left, "- ")
			fmt.Printf("%s = 0\n", left)
		}
	}
}

func formula4() {

	for d := 1; d <= param.DC; d++ {
		for a := 1; a < param.Attack; a++ {
			fmt.Printf(T_ad+" +", a, d)
		}
		fmt.Printf(T_ad+" <= %d", param.Attack, d, int(dcs[d].Capacity.Network))
		fmt.Println("")
	}
}

func formula5() {
	var left string
	for d := 1; d <= param.DC; d++ {
		for a := 1; a <= param.Attack; a++ {
			for i := 0; i < len(logicalChains[a].instances); i++ {
				left = ""
				for _, rack := range dcs[d].Racks {
					for _, server := range rack.Servers {
						left += fmt.Sprintf(N_dsai+" +", d, server.Id(), a, i)
					}
				}
				left = strings.TrimRight(left, "+")
				//fmt.Printf("0 >= "+T_ad+"*%f\n", a, d, logicalChains[a].trafficFractions[i]/logicalChains[a].instances[i].Capacity())
				fmt.Printf("%s -  %f"+T_ad+" >= 0 \n", left, logicalChains[a].trafficFractions[i]/logicalChains[a].instances[i].Capacity(), a, d)
			}
		}
	}
}

func formula5_1() {
	var left string
	for d := 1; d <= param.DC; d++ {
		for a := 1; a <= param.Attack; a++ {
			for i := 0; i < len(logicalChains[a].instances); i++ {
				left = ""
				for _, rack := range dcs[d].Racks {
					for _, server := range rack.Servers {
						left += fmt.Sprintf(N_dsai+" +", d, server.Id(), a, i)
					}
				}
				left = strings.TrimRight(left, "+")
				//fmt.Printf("0 >= "+T_ad+"*%f\n", a, d, logicalChains[a].trafficFractions[i]/logicalChains[a].instances[i].Capacity())
				fmt.Printf("%s -  %f"+T_ad+" < 0.99 \n", left, logicalChains[a].trafficFractions[i]/logicalChains[a].instances[i].Capacity(), a, d)
			}
		}
	}
}

func formula6() {
	for d := 1; d <= param.DC; d++ {
		for _, rack := range dcs[d].Racks {
			for _, server := range rack.Servers {
				left := ""
				for a := 1; a <= param.Attack; a++ {
					for i := 0; i < len(logicalChains[a].instances); i++ {
						//fmt.Printf(N_dsai+" +", d, server.Id(), a, i)
						left += fmt.Sprintf(N_dsai+" +", d, server.Id(), a, i)
					}
				}
				left = strings.TrimRight(left, "+")
				fmt.Printf("%s <= %d\n", left, server.VMs)
			}
		}

	}

}

func formula7() {
	for d := 1; d <= param.DC; d++ {
		fmt.Printf(DSC_d+" - "+"%d"+INTRA_d+" - "+"%d"+INTER_d+" - %d"+INTRA_s_d+" = 0\n", d, INTRA_TOR_COST, d, INTER_COST, d, INTRA_SERVER_COST, d)
		//fmt.Printf(DSC_d+" - "+"%d"+INTRA_d+" - "+"%d"+INTER_d+" = 0\n", d, INTRA_TOR_COST, d, INTER_COST, d)
	}
}

func formula8() {
	for d := 1; d <= param.DC; d++ {
		//left := ""
		//		fmt.Printf(INTRA_d+" - ", d)
		//left += fmt.Sprintf(INTRA_d+" - ", d)
		var buffer bytes.Buffer
		buffer.WriteString(fmt.Sprintf(INTRA_d+" - ", d))
		for a := 1; a <= param.Attack; a++ {
			for _, logicalEdge := range logicalChains[a].logicalEdges {
				for _, rack1 := range dcs[d].Racks {
					for _, server1 := range rack1.Servers {
						for _, rack2 := range dcs[d].Racks {
							for _, server2 := range rack2.Servers {
								if server1.RackNum != server2.RackNum {
									continue
								}
								if server1.Id() == server2.Id() {
									continue
								}
								for vm1 := 1; vm1 <= MAX_VM; vm1++ {
									for vm2 := 1; vm2 <= MAX_VM; vm2++ {
										for l := 1; l <= MAX_L; l++ {
											//left += fmt.Sprintf("%d"+Q_daivm1sjvm2s2l+" - ", 1, d, a, logicalEdge.i, vm1, server1.Id(), logicalEdge.j, vm2, server2.Id(), l)
											//left += fmt.Sprintf("%d"+Q_daivm1sjvm2s2l+" - ", 1, d, a, logicalEdge.i, vm1, server1.Id(), logicalEdge.j, vm2, server2.Id(), l)
											buffer.WriteString(fmt.Sprintf("%d"+Q_daivm1sjvm2s2l+" - ", 1, d, a, logicalEdge.i, vm1, server1.Id(), logicalEdge.j, vm2, server2.Id(), l))
										}
									}
								}
							}
						}
					}
				}

			}
		}
		//	left = strings.TrimRight(left, "- ")
		left := strings.TrimRight(buffer.String(), "- ")
		fmt.Printf("%s = 0\n", left)
	}
}

func formula9() {
	for d := 1; d <= param.DC; d++ {
		//	left := ""
		//fmt.Printf(INTER_d+" = ", d)
		//	left = fmt.Sprintf(INTER_d+" - ", d)
		var buffer bytes.Buffer
		buffer.WriteString(fmt.Sprintf(INTER_d+" - ", d))
		for a := 1; a <= param.Attack; a++ {
			for _, logicalEdge := range logicalChains[a].logicalEdges {
				for _, rack1 := range dcs[d].Racks {
					for _, server1 := range rack1.Servers {
						for _, rack2 := range dcs[d].Racks {
							for _, server2 := range rack2.Servers {
								if server1.RackNum == server2.RackNum {
									continue
								}
								if server1.Id() == server2.Id() {
									continue
								}
								for vm1 := 1; vm1 <= MAX_VM; vm1++ {
									for vm2 := 1; vm2 <= MAX_VM; vm2++ {
										for l := 1; l <= MAX_L; l++ {
											//fmt.Printf("%d"+Q_daivm1sjvm2s2l+" + ", 1, d, a, logicalEdge.i, vm1, server1.Id(), logicalEdge.j, vm2, server2.Id(), l)
											//left += fmt.Sprintf("%d"+Q_daivm1sjvm2s2l+" - ", 1, d, a, logicalEdge.i, vm1, server1.Id(), logicalEdge.j, vm2, server2.Id(), l)
											buffer.WriteString(fmt.Sprintf("%d"+Q_daivm1sjvm2s2l+" - ", 1, d, a, logicalEdge.i, vm1, server1.Id(), logicalEdge.j, vm2, server2.Id(), l))
										}
									}
								}
							}
						}
					}
				}

			}
		}
		//		left = strings.TrimRight(left, "- ")
		left := strings.TrimRight(buffer.String(), "- ")
		fmt.Printf("%s = 0\n", left)
	}
}

func formula9_1() {
	for d := 1; d <= param.DC; d++ {
		//		left := ""
		//fmt.Printf(INTER_d+" = ", d)
		//		left = fmt.Sprintf(INTRA_s_d+" - ", d)
		var buffer bytes.Buffer
		buffer.WriteString(fmt.Sprintf(INTRA_s_d+" - ", d))
		for a := 1; a <= param.Attack; a++ {
			for _, logicalEdge := range logicalChains[a].logicalEdges {
				for _, rack1 := range dcs[d].Racks {
					for _, server1 := range rack1.Servers {
						for _, rack2 := range dcs[d].Racks {
							for _, server2 := range rack2.Servers {
								if server1.Id() != server2.Id() {
									continue
								}
								for vm1 := 1; vm1 <= MAX_VM; vm1++ {
									for vm2 := 1; vm2 <= MAX_VM; vm2++ {
										if vm1 == vm2 {
											continue
										}
										for l := 1; l <= MAX_L; l++ {
											//fmt.Printf("%d"+Q_daivm1sjvm2s2l+" + ", 1, d, a, logicalEdge.i, vm1, server1.Id(), logicalEdge.j, vm2, server2.Id(), l)
											//											left += fmt.Sprintf("%d"+Q_daivm1sjvm2s2l+" - ", 1, d, a, logicalEdge.i, vm1, server1.Id(), logicalEdge.j, vm2, server2.Id(), l)
											buffer.WriteString(fmt.Sprintf("%d"+Q_daivm1sjvm2s2l+" - ", 1, d, a, logicalEdge.i, vm1, server1.Id(), logicalEdge.j, vm2, server2.Id(), l))
										}
									}
								}
							}
						}
					}
				}

			}
		}
		//		left = strings.TrimRight(left, "- ")
		left := strings.TrimRight(buffer.String(), "- ")
		fmt.Printf("%s = 0\n", left)
	}
}

func formula10() {
	for d := 1; d <= param.DC; d++ {
		for a := 1; a <= param.Attack; a++ {
			for j := 0; j < len(logicalChains[a].instances); j++ {
				for vm2 := 1; vm2 <= MAX_VM; vm2++ {
					//					left := ""
					var buffer bytes.Buffer
					for _, rack1 := range dcs[d].Racks {
						for _, server1 := range rack1.Servers {
							for _, rack2 := range dcs[d].Racks {
								for _, server2 := range rack2.Servers {
									for _, logicalEdge := range logicalChains[a].logicalEdges {
										if logicalEdge.j != j {
											continue
										}
										for vm1 := 1; vm1 <= MAX_VM; vm1++ {
											if (server1.Id() == server2.Id()) && (vm1 == vm2) {
												continue
											}
											for l := 1; l <= MAX_L; l++ {
												//fmt.Printf("%d"+Q_daivm1sjvm2s2l+" + ", 1, d, a, logicalEdge.i, vm1, server1.Id(), logicalEdge.j, vm2, server2.Id(), l)
												//left += fmt.Sprintf("%d"+Q_daivm1sjvm2s2l+" +", 1, d, a, logicalEdge.i, vm1, server1.Id(), logicalEdge.j, vm2, server2.Id(), l)
												buffer.WriteString(fmt.Sprintf("%d"+Q_daivm1sjvm2s2l+" +", 1, d, a, logicalEdge.i, vm1, server1.Id(), logicalEdge.j, vm2, server2.Id(), l))
											}
										}
									}
								}
							}
						}
					}
					//					left = strings.TrimRight(left, "+")
					left := strings.TrimRight(buffer.String(), "+")
					if left != "" {
						fmt.Printf("%s <= %d\n", left, int(logicalChains[a].instances[j].Capacity()))
					}
				}
			}
		}
	}
}

func formula11_1() {
	for d := 1; d <= param.DC; d++ {
		for a := 1; a <= param.Attack; a++ {
			for i := 0; i < len(logicalChains[a].instances); i++ {
				for _, rack1 := range dcs[d].Racks {
					for _, server1 := range rack1.Servers {
						//fmt.Printf("%d"+N_dsai+" * %d >= ", d, server1.Id(), a, i, int(logicalChains[a].instances[i].Capacity()))
						//						left := ""
						//fmt.Printf("%d"+N_dsai+" >= ", int(logicalChains[a].instances[i].Capacity()), d, server1.Id(), a, i)
						//						left = fmt.Sprintf("%d"+N_dsai+" - ", int(logicalChains[a].instances[i].Capacity()), d, server1.Id(), a, i)
						var buffer bytes.Buffer
						buffer.WriteString(fmt.Sprintf("%d"+N_dsai+" - ", int(logicalChains[a].instances[i].Capacity()), d, server1.Id(), a, i))
						//						left2 := ""
						for vm1 := 1; vm1 <= MAX_VM; vm1++ {
							for vm2 := 1; vm2 <= MAX_VM; vm2++ {
								for _, logicalEdge := range logicalChains[a].logicalEdges {
									if logicalEdge.j != i {
										continue
									}
									for _, rack2 := range dcs[d].Racks {
										for _, server2 := range rack2.Servers {
											/*
												if server1.Id() == server2.Id() {
													continue
												}
											*/
											for l := 1; l <= MAX_L; l++ {
												//fmt.Printf("%d"+Q_daivm1sjvm2s2l+" + ", 1, d, a, logicalEdge.i, vm1, server1.Id(), logicalEdge.j, vm2, server2.Id(), l)
												//left += fmt.Sprintf("%d"+Q_daivm1sjvm2s2l+" - ", 1, d, a, logicalEdge.i, vm1, server1.Id(), logicalEdge.j, vm2, server2.Id(), l)
												//left2 += fmt.Sprintf("%d"+Q_daivm1sjvm2s2l+" - ", 1, d, a, logicalEdge.i, vm1, server1.Id(), logicalEdge.j, vm2, server2.Id(), l)
												buffer.WriteString(fmt.Sprintf("%d"+Q_daivm1sjvm2s2l+" - ", 1, d, a, logicalEdge.i, vm1, server2.Id(), logicalEdge.j, vm2, server1.Id(), l))
											}
										}
									}
								}
							}
						}
						if buffer.Len() != 0 {
							left := strings.TrimRight(buffer.String(), "- ")
							//fmt.Printf("%s >= 0 \n", left)
							fmt.Printf("%s >= 0 \n", left)
						}
					}
				}
			}
		}
	}
}

func formula11_1_1() {
	for d := 1; d <= param.DC; d++ {
		for a := 1; a <= param.Attack; a++ {
			for i := 0; i < len(logicalChains[a].instances); i++ {
				for _, rack1 := range dcs[d].Racks {
					for _, server1 := range rack1.Servers {
						//fmt.Printf("%d"+N_dsai+" * %d >= ", d, server1.Id(), a, i, int(logicalChains[a].instances[i].Capacity()))
						//						left := ""
						//fmt.Printf("%d"+N_dsai+" >= ", int(logicalChains[a].instances[i].Capacity()), d, server1.Id(), a, i)
						//						left = fmt.Sprintf("%d"+N_dsai+" - ", int(logicalChains[a].instances[i].Capacity()), d, server1.Id(), a, i)
						var buffer bytes.Buffer
						buffer.WriteString(fmt.Sprintf("%d"+N_dsai+" - ", int(logicalChains[a].instances[i].Capacity()), d, server1.Id(), a, i))
						//						left2 := ""
						for vm1 := 1; vm1 <= MAX_VM; vm1++ {
							for vm2 := 1; vm2 <= MAX_VM; vm2++ {
								for _, logicalEdge := range logicalChains[a].logicalEdges {
									if logicalEdge.i != i {
										continue
									}
									for _, rack2 := range dcs[d].Racks {
										for _, server2 := range rack2.Servers {
											/*
												if server1.Id() == server2.Id() {
													continue
												}
											*/
											for l := 1; l <= MAX_L; l++ {
												//fmt.Printf("%d"+Q_daivm1sjvm2s2l+" + ", 1, d, a, logicalEdge.i, vm1, server1.Id(), logicalEdge.j, vm2, server2.Id(), l)
												//left += fmt.Sprintf("%d"+Q_daivm1sjvm2s2l+" - ", 1, d, a, logicalEdge.i, vm1, server1.Id(), logicalEdge.j, vm2, server2.Id(), l)
												//left2 += fmt.Sprintf("%d"+Q_daivm1sjvm2s2l+" - ", 1, d, a, logicalEdge.i, vm1, server1.Id(), logicalEdge.j, vm2, server2.Id(), l)
												buffer.WriteString(fmt.Sprintf("%d"+Q_daivm1sjvm2s2l+" - ", 1, d, a, logicalEdge.i, vm1, server1.Id(), logicalEdge.j, vm2, server2.Id(), l))
											}
										}
									}
								}
							}
						}
						if buffer.Len() != 0 {
							left := strings.TrimRight(buffer.String(), "- ")
							//fmt.Printf("%s >= 0 \n", left)
							fmt.Printf("%s >= 0 \n", left)
						}
					}
				}
			}
		}
	}
}

func formula11() {
	for d := 1; d <= param.DC; d++ {
		for a := 1; a <= param.Attack; a++ {
			for i := 0; i < len(logicalChains[a].instances); i++ {
				for _, rack1 := range dcs[d].Racks {
					for _, server1 := range rack1.Servers {
						//fmt.Printf("%d"+N_dsai+" * %d >= ", d, server1.Id(), a, i, int(logicalChains[a].instances[i].Capacity()))
						left := ""
						//fmt.Printf("%d"+N_dsai+" >= ", int(logicalChains[a].instances[i].Capacity()), d, server1.Id(), a, i)
						left = fmt.Sprintf("%d"+N_dsai+" - ", int(logicalChains[a].instances[i].Capacity()), d, server1.Id(), a, i)
						left2 := ""
						for vm1 := 1; vm1 <= MAX_VM; vm1++ {
							for vm2 := 1; vm2 <= MAX_VM; vm2++ {
								for _, logicalEdge := range logicalChains[a].logicalEdges {
									if logicalEdge.i != i {
										continue
									}
									for _, rack2 := range dcs[d].Racks {
										for _, server2 := range rack2.Servers {
											/*
												if server1.Id() == server2.Id() {
													continue
												}
											*/
											for l := 1; l <= MAX_L; l++ {
												//fmt.Printf("%d"+Q_daivm1sjvm2s2l+" + ", 1, d, a, logicalEdge.i, vm1, server1.Id(), logicalEdge.j, vm2, server2.Id(), l)
												//left += fmt.Sprintf("%d"+Q_daivm1sjvm2s2l+" - ", 1, d, a, logicalEdge.i, vm1, server1.Id(), logicalEdge.j, vm2, server2.Id(), l)
												left2 += fmt.Sprintf("%d"+Q_daivm1sjvm2s2l+" - ", 1, d, a, logicalEdge.i, vm1, server1.Id(), logicalEdge.j, vm2, server2.Id(), l)
											}
										}
									}
								}
							}
						}
						if left2 != "" {
							left = strings.TrimRight(left+left2, "- ")
							//fmt.Printf("%s >= 0 \n", left)
							fmt.Printf("%s = 0 \n", left)
						}
					}
				}
			}
		}
	}
}

func formula12() {
	for d := 1; d <= param.DC; d++ {
		for a := 1; a <= param.Attack; a++ {
			for i := 0; i < len(logicalChains[a].instances); i++ {
				for _, rack1 := range dcs[d].Racks {
					for _, server1 := range rack1.Servers {
						left := ""
						//fmt.Printf("%d"+N_dsai+" =< ", int(logicalChains[a].instances[i].Capacity()), d, server1.Id(), a, i)
						//left = fmt.Sprintf("%d"+N_dsai+" - ", int(logicalChains[a].instances[i].Capacitycity()), d, server1.Id(), a, i)
						//left = fmt.Sprintf("%d"+N_dsai+" - ", int(logicalChains[a].instances[i].Capacitycity()), d, server1.Id(), a, i)
						left = fmt.Sprintf("%d"+N_dsai+" - ", int(logicalChains[a].instances[i].Capacity()), d, server1.Id(), a, i)
						left2 := ""
						for vm1 := 1; vm1 <= MAX_VM; vm1++ {
							for vm2 := 1; vm2 <= MAX_VM; vm2++ {
								for _, logicalEdge := range logicalChains[a].logicalEdges {
									if logicalEdge.i != i {
										continue
									}
									for _, rack2 := range dcs[d].Racks {
										for _, server2 := range rack2.Servers {
											if server1.Id() == server2.Id() {
												continue
											}
											for l := 1; l <= MAX_L; l++ {
												//fmt.Printf("%d"+Q_daivm1sjvm2s2l+" + ", 1, d, a, logicalEdge.i, vm1, server1.Id(), logicalEdge.j, vm2, server2.Id(), l)
												left2 += fmt.Sprintf("%d"+Q_daivm1sjvm2s2l+" - ", 1, d, a, logicalEdge.i, vm1, server1.Id(), logicalEdge.j, vm2, server2.Id(), l)
											}
										}
									}
								}
							}
						}
						if left2 != "" {
							left = strings.TrimRight(left+left2, "- ")
							fmt.Printf("%s <= 1\n", left)
						}
					}
				}
			}
		}
	}
}
func formula12_1() {
	for d := 1; d <= param.DC; d++ {
		for a := 1; a <= param.Attack; a++ {
			for i := 0; i < len(logicalChains[a].instances); i++ {
				for _, rack1 := range dcs[d].Racks {
					for _, server1 := range rack1.Servers {
						left := ""
						//						left = fmt.Sprintf("%d"+N_dsai+" - ", int(logicalChains[a].instances[i].Capacitycity()), d, server1.Id(), a, i)
						left = fmt.Sprintf("%d"+N_dsai+" - ", int(logicalChains[a].instances[i].Capacity()), d, server1.Id(), a, i)
						//fmt.Printf("%d"+N_dsai+" =< ", int(logicalChains[a].instances[i].Capacity()), d, server1.Id(), a, i)
						//						left = fmt.Sprintf("%d"+N_dsai+" - ", int(logicalChains[a].instances[i].Capacitycity()), d, server1.Id(), a, i)
						//left = fmt.Sprintf("%d"+N_dsai+" - ", int(logicalChains[a].instances[i].Capacitycity()), d, server1.Id(), a, i)
						//	left = fmt.Sprintf("%d"+N_dsai+" - ", int(logicalChains[a].instances[i].Capacity()), d, server1.Id(), a, i)
						var buffer bytes.Buffer
						//						left2 := ""
						for vm1 := 1; vm1 <= MAX_VM; vm1++ {
							for vm2 := 1; vm2 <= MAX_VM; vm2++ {
								for _, logicalEdge := range logicalChains[a].logicalEdges {
									if logicalEdge.j != i {
										continue
									}
									for _, rack2 := range dcs[d].Racks {
										for _, server2 := range rack2.Servers {
											/*
												if server1.Id() == server2.Id() {
													continue
												}
											*/
											for l := 1; l <= MAX_L; l++ {
												//fmt.Printf("%d"+Q_daivm1sjvm2s2l+" + ", 1, d, a, logicalEdge.i, vm1, server1.Id(), logicalEdge.j, vm2, server2.Id(), l)
												//left2 += fmt.Sprintf("%d"+Q_daivm1sjvm2s2l+" - ", 1, d, a, logicalEdge.i, vm1, server1.Id(), logicalEdge.j, vm2, server2.Id(), l)
												buffer.WriteString(fmt.Sprintf("%d"+Q_daivm1sjvm2s2l+" - ", 1, d, a, logicalEdge.i, vm1, server2.Id(), logicalEdge.j, vm2, server1.Id(), l))
											}
										}
									}
								}
							}
						}
						//						if left2 != "" {
						if buffer.Len() != 0 {
							left += strings.TrimRight(buffer.String(), "- ")
							fmt.Printf("%s <= 0.99\n", left)
							//fmt.Printf("%s <= 1\n", left)
						}
					}
				}
			}
		}
	}
}

func formula12_1_1() {
	for d := 1; d <= param.DC; d++ {
		for a := 1; a <= param.Attack; a++ {
			for i := 0; i < len(logicalChains[a].instances); i++ {
				for _, rack1 := range dcs[d].Racks {
					for _, server1 := range rack1.Servers {
						left := ""
						//						left = fmt.Sprintf("%d"+N_dsai+" - ", int(logicalChains[a].instances[i].Capacitycity()), d, server1.Id(), a, i)
						left = fmt.Sprintf("%d"+N_dsai+" - ", int(logicalChains[a].instances[i].Capacity()), d, server1.Id(), a, i)
						//fmt.Printf("%d"+N_dsai+" =< ", int(logicalChains[a].instances[i].Capacity()), d, server1.Id(), a, i)
						//						left = fmt.Sprintf("%d"+N_dsai+" - ", int(logicalChains[a].instances[i].Capacitycity()), d, server1.Id(), a, i)
						//left = fmt.Sprintf("%d"+N_dsai+" - ", int(logicalChains[a].instances[i].Capacitycity()), d, server1.Id(), a, i)
						//	left = fmt.Sprintf("%d"+N_dsai+" - ", int(logicalChains[a].instances[i].Capacity()), d, server1.Id(), a, i)
						var buffer bytes.Buffer
						//						left2 := ""
						for vm1 := 1; vm1 <= MAX_VM; vm1++ {
							for vm2 := 1; vm2 <= MAX_VM; vm2++ {
								for _, logicalEdge := range logicalChains[a].logicalEdges {
									if logicalEdge.i != i {
										continue
									}
									for _, rack2 := range dcs[d].Racks {
										for _, server2 := range rack2.Servers {
											/*
												if server1.Id() == server2.Id() {
													continue
												}
											*/
											for l := 1; l <= MAX_L; l++ {
												//fmt.Printf("%d"+Q_daivm1sjvm2s2l+" + ", 1, d, a, logicalEdge.i, vm1, server1.Id(), logicalEdge.j, vm2, server2.Id(), l)
												//left2 += fmt.Sprintf("%d"+Q_daivm1sjvm2s2l+" - ", 1, d, a, logicalEdge.i, vm1, server1.Id(), logicalEdge.j, vm2, server2.Id(), l)
												buffer.WriteString(fmt.Sprintf("%d"+Q_daivm1sjvm2s2l+" - ", 1, d, a, logicalEdge.i, vm1, server1.Id(), logicalEdge.j, vm2, server2.Id(), l))
											}
										}
									}
								}
							}
						}
						//						if left2 != "" {
						if buffer.Len() != 0 {
							left += strings.TrimRight(buffer.String(), "- ")
							//fmt.Printf("%s <= 0.99\n", left)
							fmt.Printf("%s <= 1\n", left)
						}
					}
				}
			}
		}
	}
}

func formula_flow_conservation() {
	for d := 1; d <= param.DC; d++ {
		for a := 1; a <= param.Attack; a++ {
			for i := 0; i < len(logicalChains[a].instances); i++ {
				for vm1 := 1; vm1 <= MAX_VM; vm1++ {
					//	left = fmt.Sprintf("%d"+N_dsai+" - ", int(logicalChains[a].instances[i].Capacity()), d, server1.Id(), a, i)
					var left bytes.Buffer
					var right bytes.Buffer
					for _, rack1 := range dcs[d].Racks {
						for _, server1 := range rack1.Servers {
							for _, rack2 := range dcs[d].Racks {
								for _, server2 := range rack2.Servers {
									for vm2 := 1; vm2 <= MAX_VM; vm2++ {
										for l := 1; l <= MAX_L; l++ {
											for _, logicalEdge := range logicalChains[a].logicalEdges {
												if logicalEdge.j == i {
													left.WriteString(fmt.Sprintf("%d"+Q_daivm1sjvm2s2l+" + ", 1, d, a, logicalEdge.i, vm2, server2.Id(), logicalEdge.j, vm1, server1.Id(), l))
												} else if logicalEdge.i == i {
													right.WriteString(fmt.Sprintf("%d"+Q_daivm1sjvm2s2l+" - ", 1, d, a, logicalEdge.i, vm1, server1.Id(), logicalEdge.j, vm2, server2.Id(), l))
												}
											}
										}
									}
								}
							}
						}
					}
					if left.Len() != 0 && right.Len() != 0 {
						fmt.Println(strings.TrimRight(left.String(), "+ "), "-", strings.TrimRight(right.String(), "- "), " = 0 ")
					}
				}
			}
		}
	}
}

func formula_first_node() {
	i := 0
	for d := 1; d <= param.DC; d++ {
		for a := 1; a <= param.Attack; a++ {
			for _, rack1 := range dcs[d].Racks {
				for _, server1 := range rack1.Servers {
					var left bytes.Buffer
					var right bytes.Buffer
					left.WriteString(fmt.Sprintf("%d"+N_dsai, int(logicalChains[a].instances[i].Capacity()), d, server1.Id(), a, i))
					for _, logicalEdge := range logicalChains[a].logicalEdges {
						if logicalEdge.i != i {
							continue
						}
						for _, rack2 := range dcs[d].Racks {
							for _, server2 := range rack2.Servers {
								for vm1 := 1; vm1 <= MAX_VM; vm1++ {
									for vm2 := 1; vm2 <= MAX_VM; vm2++ {
										for l := 1; l <= MAX_L; l++ {
											right.WriteString(fmt.Sprintf("%d"+Q_daivm1sjvm2s2l+" - ", 1, d, a, i, vm1, server1.Id(), logicalEdge.j, vm2, server2.Id(), l))
										}
									}
								}
							}
						}
					}
					fmt.Println(left.String(), "-", strings.TrimRight(right.String(), "- "), ">", 0)
				}
			}
		}
	}
}

func formula13() {
	for d := 1; d <= param.DC; d++ {
		for a := 1; a <= param.Attack; a++ {
			for _, logicalEdge := range logicalChains[a].logicalEdges {
				for _, rack1 := range dcs[d].Racks {
					for _, server1 := range rack1.Servers {
						for vm1 := 1; vm1 <= MAX_VM; vm1++ {
							for l := 1; l <= MAX_L; l++ {
								var buffer bytes.Buffer
								buffer.WriteString(fmt.Sprintf("%d"+Q_daivm1sjvm2s2l+"= 0", 1, d, a, logicalEdge.i, vm1, server1.Id(), logicalEdge.j, vm1, server1.Id(), l))
								fmt.Println(buffer.String())
							}
						}
					}
				}
			}
		}
	}
}

func bounds() {
	for d := 1; d <= param.DC; d++ {
		for e := 1; e <= param.Ingress; e++ {
			for a := 1; a <= param.Attack; a++ {
				fmt.Printf("0<="+F_ead+"<=1\n", e, a, d)
			}
		}
	}
}

func general() {
	fmt.Println("general")
	for d := 1; d <= param.DC; d++ {
		for a := 1; a <= param.Attack; a++ {
			for _, rack := range dcs[d].Racks {
				for _, server := range rack.Servers {
					for i := 0; i < len(logicalChains[a].instances); i++ {
						fmt.Printf(N_dsai+"\n", d, server.Id(), a, i)
					}
				}
			}
		}
	}
}

func binary() {
	fmt.Println("binary")
	for d := 1; d <= param.DC; d++ {
		for a := 1; a <= param.Attack; a++ {
			for _, logicalEdge := range logicalChains[a].logicalEdges {
				for vm1 := 1; vm1 <= MAX_VM; vm1++ {
					for vm2 := 1; vm2 <= MAX_VM; vm2++ {
						for _, rack1 := range dcs[d].Racks {
							for _, server1 := range rack1.Servers {
								for _, rack2 := range dcs[d].Racks {
									for _, server2 := range rack2.Servers {
										for l := 1; l <= MAX_L; l++ {
											fmt.Printf(Q_daivm1sjvm2s2l+"\n", d, a, logicalEdge.i, vm1, server1.Id(), logicalEdge.j, vm2, server2.Id(), l)

										}
									}
								}
							}
						}
					}
				}
			}
		}
	}
}

func setup() {
	a_modules = make(map[string]*A_Module)
	r_moduels = make(map[string]*R_Module)
	dcs = make(map[int]*DC)
	attacks = make(map[int]*Attack)
	logicalChains = make(map[int]*Chain)
	dsc = make(map[int]int)
	readParam()
	L = make(map[int]map[int]int)
	for e := 1; e <= param.Ingress; e++ {
		L[e] = make(map[int]int)
	}
	readDC()
	readAttack()
	readChain()
	prepareChain()
}

func main() {
	setup()
	objective()
	fmt.Println("subject to")
	formula2()
	formula3()
	//	fmt.Println("4")
	formula4()
	//	fmt.Println("5")
	formula5()
	//	formula5_1()
	formula6()
	formula7()
	formula8()
	//	fmt.Println("9")
	formula9()
	formula9_1()
	//	fmt.Println("10")
	formula10()
	//	fmt.Println("11")
	formula11_1()
	//	formula11_1_1()
	//fmt.Println("12")
	formula12_1()
	//	formula12_1_1()
	formula_flow_conservation()
	formula_first_node()
	formula13()
	fmt.Println("Bounds")
	bounds()
	fmt.Println("")
	general()
	fmt.Println("")
	binary()
	fmt.Println("End")
}
