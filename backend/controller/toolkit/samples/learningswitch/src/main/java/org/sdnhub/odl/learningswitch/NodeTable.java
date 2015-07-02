package org.sdnhub.odl.learningswitch;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.ArrayList;
import java.util.Map;
import java.util.Date;
import java.util.concurrent.ConcurrentHashMap;

import javax.xml.bind.annotation.XmlRootElement;

import org.opendaylight.controller.sal.core.Node;
import org.opendaylight.controller.sal.core.NodeConnector;
import org.opendaylight.controller.sal.packet.Ethernet;
import org.opendaylight.controller.sal.packet.IPv4;
import org.opendaylight.controller.sal.packet.Packet;
import org.opendaylight.controller.sal.packet.RawPacket;
import org.opendaylight.controller.sal.packet.BitBufferHelper;
import org.opendaylight.controller.sal.utils.HexEncode;

import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name="NodeTable")
public class NodeTable {
	Set <NodeConnectorPlus> nodeConnectors;
	
	Map <String, NodeConnectorPlus> table;
	@XmlElement(name="node")
	Node n;
	
	public NodeTable(Node n) {
		this.n = n;
		table = new ConcurrentHashMap<String, NodeConnectorPlus>();
		nodeConnectors = new HashSet<NodeConnectorPlus>();
	}
	
	private String createSourceKey(Packet pkt, LearningSwitchOptions lo) {
		String key = "";

		if ( pkt instanceof Ethernet &&				
				(lo.options & LearningSwitchOptions.SRC_MAC) > 0) {
			
			key += HexEncode.bytesToHexString(((Ethernet)pkt).getSourceMACAddress());
		} 
		if ((lo.options & LearningSwitchOptions.SRC_IPv4) > 0) {
			if ((Packet)pkt instanceof IPv4) {
				key += String.valueOf(((IPv4)pkt).getSourceAddress());
			}
		}
		// Remaining checks not implemented

		return key;
	}
	
	private String createDestKey(Packet pkt, LearningSwitchOptions lo) {
		String key = "";

		if ( pkt instanceof Ethernet &&				
				(lo.options & LearningSwitchOptions.DST_MAC) > 0) {
			key += HexEncode.bytesToHexString(((Ethernet)pkt).getDestinationMACAddress());
		} 
		if ((lo.options & LearningSwitchOptions.DST_IPv4) > 0) {
			if ((Packet)pkt instanceof IPv4) {
				key += String.valueOf(((IPv4)pkt).getDestinationAddress());
			}
		}
		// Remaining checks not implemented

		return key;
	}

	
	public void learnSourceNodeConnector(Packet pkt, NodeConnector incomingNodeConnector, LearningSwitchOptions lo)
	{
		String key = createSourceKey(pkt, lo);
		
		NodeConnectorPlus ncplus = table.get(key);
		if (ncplus == null) {
			ncplus = new NodeConnectorPlus(incomingNodeConnector);
			ncplus.history.add(new Pair<String, Date>(key, new Date()));
			table.put(key, ncplus);
		} else {
			if (!ncplus.nc.equals(incomingNodeConnector)) {
				ncplus.nc = incomingNodeConnector;
			}
			ncplus.history.add(new Pair<String, Date>(key, new Date()));
		}
	}
	
	public NodeConnector findDestinationNodeConnector(Packet pkt, LearningSwitchOptions lo) {
		String key = createDestKey(pkt, lo);
		NodeConnectorPlus ncplus = table.get(key);
		if (ncplus != null) {
			return ncplus.nc;
		} else {
			return null;
		}
	}
	
	@XmlElement(name="table")
	public List<Pair<String, NodeConnectorPlus>> getNodeTable()
	{
		List<Pair<String, NodeConnectorPlus>> t = new ArrayList<Pair<String, NodeConnectorPlus>>();
		for (Map.Entry<String, NodeConnectorPlus> entry : table.entrySet()) {
			Pair<String, NodeConnectorPlus> p = new Pair<String, NodeConnectorPlus>(entry.getKey(), entry.getValue());
			t.add(p);
		}
		return t;
	}
	
	
	public String toString() {
		return table.toString();
	}
	
	public List<NodeConnector> getNodeConnectors()
	{
		List<NodeConnector> list_nc = new ArrayList<NodeConnector>();
		for (NodeConnectorPlus ncp : nodeConnectors) {
			list_nc.add(ncp.nc);
		}
		return list_nc;
	}
	
}
