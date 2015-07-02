package org.sdnhub.flowtags;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.opendaylight.controller.sal.action.Action;
import org.opendaylight.controller.sal.core.Node;
import org.opendaylight.controller.sal.core.NodeConnector;
import org.opendaylight.controller.sal.flowprogrammer.Flow;
import org.opendaylight.controller.sal.flowprogrammer.IFlowProgrammerService;
import org.opendaylight.controller.sal.match.Match;
import org.opendaylight.controller.sal.match.MatchType;
import org.opendaylight.controller.sal.utils.Status;
import org.opendaylight.controller.switchmanager.ISwitchManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public  class OFNode {
	protected Node node;
	protected HashMap<String, NodeConnector> connectors;
	protected IFlowProgrammerService programmer;
	protected ISwitchManager switchmanager;
	protected ArrayList<Flow> installedFlows;
	private static final Logger logger = LoggerFactory.getLogger(OFNode.class);

	public OFNode(Node node, ISwitchManager switchManager,
			IFlowProgrammerService programmer) {
		this.node = node;
		this.connectors = new HashMap<String, NodeConnector>();
		System.out.println(node.getNodeIDString());
		for (NodeConnector c : switchManager.getNodeConnectors(node)) {
			this.connectors.put(c.getNodeConnectorIDString(), c);
			System.out.println(c.getNodeConnectorIDString());
		}
		System.out.println("test-0");
		for(NodeConnector c: this.connectors.values()){
			System.out.println("cid:"+c.getNodeConnectorIdAsString());
		}
		System.out.println("test-1");
		this.programmer = programmer;
		this.switchmanager = switchmanager;
		this.installedFlows = new ArrayList<Flow>();
	}

	public void setFlowProgrammerService(IFlowProgrammerService s) {
		this.programmer = s;
	}

	public void unsetFlowProgrammerService(IFlowProgrammerService s) {
		if (this.programmer == s) {
			this.programmer = null;
		}
	}

	public Node getNode() {
		return this.node;
	}

	// Drop every arp and broadcast message
	protected  boolean setInitialRule( ){
		Flow flow;
		Match match;
		List<Action> blockActions = new ArrayList<Action>();
		
		logger.info("Block ARP");
		flow = new Flow();
		match = new Match();
		blockActions = new ArrayList<Action>();
        /*
		match.setField(MatchType.DL_TYPE, (short) 0x0806);
		flow.setMatch(match);
		flow.setActions(blockActions);
		this.addFlowRule(flow);
        */
		
		logger.info("Block IPv6");
		flow = new Flow();
		match = new Match();
		match.setField(MatchType.DL_TYPE, (short) 0x086dd);
		flow.setMatch(match);
		flow.setActions(blockActions);
		this.addFlowRule(flow);
		
		logger.info("Block BC");
		flow = new Flow();
		match = new Match();
		String broadCast = "255.255.255.255";
		match.setField(MatchType.DL_TYPE, (short) 0x0800);
		try {
			match.setField(MatchType.NW_DST, InetAddress.getByName(broadCast),
					InetAddress.getByName(broadCast));
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
		flow.setMatch(match);
		flow.setActions(blockActions);
		this.addFlowRule(flow);
		
		flow = new Flow();
		match = new Match();
		match.setField(MatchType.DL_TYPE, (short) 0x0800);
		try {
			match.setField(MatchType.NW_DST, InetAddress.getByName("0.0.0.0"),
					InetAddress.getByName(broadCast));
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
		flow.setMatch(match);
		flow.setActions(blockActions);
		this.addFlowRule(flow);
		return true;
	}

	protected boolean addFlowRule(Flow flow) {
		if(this.node == null){
			System.out.println("OFNode addFlowRule node is null");
		}
		if(flow == null){
			System.out.println("OFNode addFlowRule flow is null");
		}
		logger.debug("Install Rule:" + this.node.toString() + ":"
				+ flow.toString());
		Status status = programmer.addFlow(this.node, flow);
		if (!status.isSuccess()) {
			logger.info(
					"SDN Plugin failed to program the flow: {}. The failure is: {}",
					flow, status.getDescription());
			return false;
		} else {
			this.installedFlows.add(flow);
			return true;
		}
	}

	protected boolean removeFlowRule(Flow flow) {
		logger.debug("Remove Rule:" + this.node.toString() + ":"
				+ flow.toString());
		Status status = programmer.removeFlow(this.node, flow);
		if (!status.isSuccess()) {
			logger.warn(
					"SDN Plugin failed to program the flow: {}. The failure is: {}",
					flow, status.getDescription());
			return false;
		} else {
			return true;
		}
	}

	public boolean clearAllFlowRule() {
		logger.info(this.node.toString() + ":Clear");
		Status status = this.programmer.removeAllFlows(this.node);
		if (!status.isSuccess()) {
			logger.warn(
					"SDN Plugin failed to clear flow on {}. The failure is: {}",
					this.node, status.getDescription());
			return false;
		}
		return true;
	}

	public boolean removeAllFlows() {
		logger.info(this.node.toString() + ":Remove all flows");
		for (Flow flow : this.installedFlows) {
			Status status = this.programmer.removeFlow(this.node, flow);
			if (!status.isSuccess()) {
				logger.warn(
						"SDN Plugin failed to clear flow on {}. The failure is: {}",
						this.node, status.getDescription());
				return false;
			}
		}
		return true;
	}

	@Override
	public String toString() {
		return this.node.getNodeIDString();
	}
}
