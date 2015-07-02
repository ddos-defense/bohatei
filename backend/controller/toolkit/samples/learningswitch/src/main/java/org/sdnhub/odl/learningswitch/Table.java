package org.sdnhub.odl.learningswitch;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.Set;
import java.io.Serializable;
import java.util.concurrent.ConcurrentHashMap; 

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.opendaylight.controller.sal.core.ConstructionException;
import org.opendaylight.controller.sal.core.Node;
import org.opendaylight.controller.sal.core.NodeConnector;
import org.opendaylight.controller.sal.packet.Ethernet;
import org.opendaylight.controller.sal.packet.Packet;
import org.sdnhub.odl.learningswitch.NodeTable;
import org.sdnhub.odl.learningswitch.LearningSwitchOptions;

@XmlRootElement(name="LearningTable")
@XmlAccessorType(XmlAccessType.NONE)
public class Table {
	
	Map <Node, NodeTable> table;
	
	public Table()
	{
		table = new ConcurrentHashMap<Node, NodeTable>();
	}
	
	public void addNode(Node n)
	{
		NodeTable nt = (NodeTable) table.get(n);
		if (nt == null) {
			NodeTable new_nt = new NodeTable(n);
			table.put(n,  new_nt);
		}
	}
	
	public void deleteNode(Node n)
	{
		NodeTable nt = (NodeTable) table.get(n);
		if (nt != null) {
			table.remove(n);
		}
	}
	
	public void learnSourceFields(Packet pkt, NodeConnector incomingNodeConnector, LearningSwitchOptions lo) throws NoSuchFieldException
	{
		NodeTable nt = (NodeTable) table.get(incomingNodeConnector.getNode());
		if (nt == null) {
			throw new NoSuchFieldException("This node was not found in the node table");
		}
		
		nt.learnSourceNodeConnector(pkt, incomingNodeConnector, lo);
	}
	
	public NodeConnector getDestinationNodeConnector(Packet pkt,  Node incomingNode, LearningSwitchOptions lo) throws NoSuchFieldException
	{
		NodeTable nt = (NodeTable) table.get(incomingNode);
		if (nt == null) {
			throw new NoSuchFieldException("This node was not found in the node table");
		}

		return nt.findDestinationNodeConnector(pkt, lo);
	}
	public String toString() {
		return this.table.toString();
	}
	
	public void clear()
	{
		table.clear();
	}
	
	@XmlElement(name="table")
	public List<NodeTable> getTable()
	{
		List<NodeTable> l = new ArrayList<NodeTable>();
		for (Map.Entry<Node, NodeTable> e : table.entrySet()) {
			l.add(e.getValue());
		}
		return l;
	}
	
}
