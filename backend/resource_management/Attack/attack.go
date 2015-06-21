package main

import (
	"encoding/json"
	"fmt"
	"math/rand"
	"os"
	"strconv"
	"time"
)

type Attack struct {
	Name   int
	Volume []AttackVolume
}

type AttackVolume struct {
	Ingress int
	Volume  int
}

func main() {
	attack := new(Attack)
	name, _ := strconv.ParseInt(os.Args[1], 10, 32)
	ingress, _ := strconv.ParseInt(os.Args[2], 10, 32)
	attack.Name = int(name)
	rand.Seed(time.Now().UnixNano())
	attack.Volume = make([]AttackVolume, 0, ingress)
	attackSum := 10 * int(ingress)
	var a int
	for i := 1; i <= int(ingress); i++ {
		if i != int(ingress) {
			a = rand.Intn(attackSum)
			attackSum -= a
		} else {
			a = attackSum
		}
		//		attack.Volume = append(attack.Volume, AttackVolume{Ingress: i, Volume: rand.Intn(1000)})
		attack.Volume = append(attack.Volume, AttackVolume{Ingress: i, Volume: a})
	}
	b, err := json.MarshalIndent(attack, "", "  ")
	if err != nil {
		fmt.Println("error:", err)
	}
	os.Stdout.Write(b)
}
