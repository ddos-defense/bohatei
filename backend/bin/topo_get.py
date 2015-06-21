#!/usr/bin/python
import httplib2
import json
from collections import defaultdict
from heapq import *

#baseUrl = 'http://0.0.0.0:2020/controller/nb/v2/'
baseUrl = 'http://192.168.123.201:8080/controller/nb/v2/'
containerName = 'default/'

h = httplib2.Http(".cache")
h.add_credentials('admin', 'admin')

def getSWID(id):
	ids = id.split(":")
	swid = ""
	for id in ids:
		swid += id
	return swid


def dijkstra(edges, f, t):
    g = defaultdict(list)
    for l,r,c in edges:
        g[l].append((c,r))

    q, seen = [(0,f,())], set()
    while q:
        (cost,v1,path) = heappop(q)
        if v1 not in seen:
            seen.add(v1)
            path = (v1, path)
            if v1 == t: return (cost, path)

            for c, v2 in g.get(v1, ()):
                if v2 not in seen:
                    heappush(q, (cost+c, v2, path))

    return float("inf")

def getNext(edges, org):
    if edges[1][0] == org:
        return edges[0]
    else:
        return getNext(edges[1], org)

if __name__ == "__main__":
# Get all the edges/links
    resp, content = h.request(baseUrl + 'topology/' + containerName, "GET")
    edgeProperties = json.loads(content)
    odlEdges = edgeProperties['edgeProperties']
    swes = {}
    edges = []
    cores = {}
    hosts = {}
    for edge in odlEdges:
        n1 = getSWID(edge['edge']['tailNodeConnector']['node']['id'])
        n2 = getSWID(edge['edge']['headNodeConnector']['node']['id'])
        n1_inf = edge['edge']['tailNodeConnector']['id']
        n2_inf = edge['edge']['headNodeConnector']['id']
        if not n1 in swes:
            swes[n1] = {}
        swes[n1][n2] = n1_inf
        if not n2 in swes:
            swes[n2] = {}
        swes[n2][n1] = n2_inf
        edges.append((n1,n2,1))
        if int(n1,16) < 0x100:
            hosts[n1] = 1
        else:
            cores[n1] = 1

        if int(n2,16) < 0x100:
            hosts[n2] = 1
        else:
            cores[n2] = 1
     

    fh = open("/testbed/bin/fw.sh", "w")
    fh.write("#!/bin/sh\n")
    fh.write("flowtag_control fw clear\n")
    for core in cores.keys():
        for host in hosts.keys():
            path = dijkstra(edges, core, host)
            if path == float("inf"): 
                print core + ":"+host + " cannot connect"
            fh.write("flowtag_control fw add -swId 0x"+ core + " -hostId "+str(int(host,16)) + " -connectorId " + swes[core][getNext(path, core)] + "\n")
    fh.close()
