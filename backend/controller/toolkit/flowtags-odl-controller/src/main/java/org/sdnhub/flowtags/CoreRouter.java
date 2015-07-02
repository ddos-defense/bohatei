package org.sdnhub.flowtags;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.opendaylight.controller.sal.action.Action;
import org.opendaylight.controller.sal.action.Output;
import org.opendaylight.controller.sal.action.SetDlDst;
import org.opendaylight.controller.sal.core.Node;
import org.opendaylight.controller.sal.core.NodeConnector;
import org.opendaylight.controller.sal.flowprogrammer.Flow;
import org.opendaylight.controller.sal.flowprogrammer.IFlowProgrammerService;
import org.opendaylight.controller.sal.match.Match;
import org.opendaylight.controller.sal.match.MatchType;
import org.opendaylight.controller.switchmanager.ISwitchManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class CoreRouter extends OFNode {
	
	static short defaultPriority = 0;
	private static final Logger logger = LoggerFactory
			.getLogger(CoreRouter.class);
	private HashMap <Integer, Flow> tagRoute;
	private ArrayList<Flow> ipRoute;
	private HashMap <Integer, NodeConnector> forwardingTable;
	private FTMiddeBoxes mbes = null;

	public CoreRouter(Node node, ISwitchManager switchManager,
			IFlowProgrammerService programmer, FTMiddeBoxes mbes) {
		super(node, switchManager, programmer);
		if( this.setInitialRule() != true ){
			logger.info(this.node.toString() + " is not well initialised");
		}
		this.tagRoute = new HashMap<Integer, Flow>();
		this.ipRoute = new ArrayList<Flow>();
		this.forwardingTable = new HashMap<Integer, NodeConnector>();
		this.mbes = mbes;
	}

	@Override
	protected boolean setInitialRule() {
		/*
		if( ! this.addFlowRule(this.initailRule("10.0.200.0", "255.255.255.0",  "00:00:00:00:00:01", "2"))){
			return false;
		}
		*/
		super.setInitialRule();
		/*
		if( ! this.addFlowRule(this.initailRule("10.0.0.0", "255.255.0.0",  "00:00:00:00:00:01", "1"))){
			return false;
		}
		if( ! this.addFlowRule(this.initailRule("10.1.0.0", "255.255.0.0", "00:00:00:00:00:02", "2"))){
			return false;
		}
		if( ! this.addFlowRule(this.initailRule("172.16.0.0", "255.255.255.0",  "00:00:00:00:00:03", "3"))){
			return false;
		}
		if( ! this.addFlowRule(this.initailRule("172.16.1.0", "255.255.255.0",  "00:00:00:00:00:04", "4"))){
			return false;
		}
		if( ! this.addFlowRule(this.initailRule("192.168.0.0", "255.255.255.0",  "00:00:00:00:00:05", "5"))){
			return false;
		}
		if( ! this.natTest()){
			return false;
		}
		*/
		return true;
	}


	public boolean addFlow(String dst, String mask, String inPort, String dstMac,
			 String outPort, short priority) {
		if(! this.addFlowRule(this.initailRule(dst, mask, inPort, dstMac, outPort, priority))){
			return false;
		}
        return true;
    }

	public boolean natTest(){
		if(! this.addFlowRule(this.initailRule("192.168.0.0", "255.255.0.0", "1","00:00:00:00:00:03","3"))){
			return false;
		}
		if(! this.addFlowRule(this.initailRule("172.16.0.0", "255.255.0.0", "1","00:00:00:00:00:03","3"))){
			return false;
		}
		if(! this.addFlowRule(this.initailRule("192.168.0.0", "255.255.0.0", "2","00:00:00:00:00:03","3"))){ 
			return false;
		}
		if(! this.addFlowRule(this.initailRule("172.16.0.0", "255.255.0.0", "2","00:00:00:00:00:03","3"))){ 
			return false;
		}
		if(! this.addFlowRule(this.initailRule("192.168.0.0", "255.255.0.0", "3","00:00:00:00:00:04","4"))){ 
			return false;
		}
		if(! this.addFlowRule(this.initailRule("172.16.0.0", "255.255.0.0", "3","00:00:00:00:00:04","4"))){ 
			return false;
		}
		if(! this.addFlowRule(this.initailRule("10.0.0.0", "255.0.0.0", "5","00:00:00:00:00:04","4"))){
			return false;
		}
		/*
		Flow flow = new Flow();
		Match match = new Match();
		List<Action> actions = new ArrayList<Action>();
		match.setField(MatchType.IN_PORT, this.connectors.get("1"));
		actions.add(new SetDlDst( this.getMacAddressBytes("00:00:00:00:00:03")));
		actions.add(new Output(this.connectors.get("3")));
		flow.setActions(actions);
		flow.setPriority((short)10);
		flow.setMatch(match);
		if( ! this.addFlowRule(flow)){
			return false;
		}
		
		flow = new Flow();
		match = new Match();
		flow.setPriority((short)10);
		match.setField(MatchType.IN_PORT, this.connectors.get("2"));
		flow.setActions(actions);
		flow.setMatch(match);
		if( ! this.addFlowRule(flow)){
			return false;
		}
		
		flow = new Flow();
		match = new Match();
		actions = new ArrayList<Action>();
		flow.setPriority((short)10);
		match.setField(MatchType.IN_PORT, this.connectors.get("3"));
		actions.add(new SetDlDst( this.getMacAddressBytes("00:00:00:00:00:04")));
		actions.add(new Output(this.connectors.get("4")));
		flow.setActions(actions);
		flow.setMatch(match);
		if( ! this.addFlowRule(flow)){
			return false;
		}
		*/
		
		
		if( ! this.addFlowRule(this.initailRule("20.0.0.0", "255.255.255.0",  "00:00:00:00:00:03", "3"))){
			return false;
		}
		
		if( ! this.addFlowRule(this.initailRule("30.0.0.0", "255.255.255.0",  "00:00:00:00:00:04", "4"))){
			return false;
		}
		return true;
	}
	public boolean setIpRoute(int hostId, int dstIP, int mask){
		Flow flow = new Flow();
		Match match = new Match();
		List<Action> actions = new ArrayList<Action>();
		logger.info("here");
		try {
			logger.info("1");
			match.setField(MatchType.DL_TYPE, (short) 0x0800);
			logger.info("2");
			match.setField(MatchType.NW_DST,FTUtil.InetAddressFromInt(dstIP), FTUtil.InetAddressFromInt(mask));
			logger.info("3");
			match.setField(MatchType.NW_TOS, (byte)(0));
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
			logger.info("4");
		NodeConnector outPort = this.forwardingTable.get(hostId);
		if(outPort == null){
			logger.info(this.node.getNodeIDString() + " doesn't have fw table for " + hostId);
			return false;
		}
		actions.add(new Output(outPort));
		flow.setMatch(match);
		flow.setActions(actions);
		if( this.addFlowRule(flow)){
			this.ipRoute.add(flow);
			return true;
		}
		return false;
	}
	
	public boolean setTagRoute(int tag, int nextMB){
		Flow flow = new Flow();
		Match match = new Match();
		List<Action> actions = new ArrayList<Action>();
		match.setField(MatchType.DL_TYPE, (short) 0x0800);
		match.setField(MatchType.NW_TOS, (byte)(tag));
		
		System.out.println("Next:"+ nextMB);
		// next == =1 means drop
		if(nextMB != -1){
			int nextHostID = this.mbes.getHostID(nextMB);
			System.out.println("NextHost:"+ nextHostID);
			NodeConnector outPort = this.forwardingTable.get(nextHostID);
			if(outPort == null){
				System.out.println("Outport is null");
				return false;
			}
			actions.add(new Output(outPort));
		}
		flow.setMatch(match);
		flow.setActions(actions);
		flow.setPriority((short)100);
		this.tagRoute.put(new Integer(tag), flow);
		return this.addFlowRule(flow);
	}
	
	public boolean setTagRouteWithLocation(int tag, int hostId){
		Flow flow = new Flow();
		Match match = new Match();
		List<Action> actions = new ArrayList<Action>();
		match.setField(MatchType.DL_TYPE, (short) 0x0800);
		match.setField(MatchType.NW_TOS, (byte)(tag));
		
		// next == =1 means drop
		if(hostId != -1){
			System.out.println("NextHost:"+ hostId);
			NodeConnector outPort = this.forwardingTable.get(hostId);
			if(outPort == null){
				System.out.println("Outport is null");
				return false;
			}
			actions.add(new Output(outPort));
		}
		flow.setMatch(match);
		flow.setActions(actions);
		flow.setPriority((short)100);
		this.tagRoute.put(new Integer(tag), flow);
		return this.addFlowRule(flow);
	}
	
	public boolean removeTagRoute(int tag){
		Integer key = new Integer(tag);
		if(this.tagRoute.containsKey(key)){
			return this.removeFlowRule(this.tagRoute.get(key));
		}
		return false;
	}
	
	public boolean removeAllTagRoutes(){
		for(Integer tag: this.tagRoute.keySet()){
			this.removeFlowRule(this.tagRoute.get(tag));
		}
		return true;
	}
	
	/*
	public boolean setClassificationRoute(int chainTag, String targetClassification){
		String outPort=null;
		switch(targetClassification){
		case "00:00:00:00:00:03":
			outPort = "3";
			break;
		case "00:00:00:00:00:04":
			outPort = "4";
			break;
		case "00:00:00:00:00:05":
			outPort = "5";
			break;
		case "00:00:00:00:00:06":
			outPort = "6";
			break;
		}
		logger.info(chainTag + ":" + targetClassification + ":" + outPort);
		return this.addFlowRule(this.classificationRule(chainTag, targetClassification, outPort));
	}
	*/
	
	private Flow initailRule(String dst, String mask, String inPort, String dstMac,
			 String outPort) {
		Flow flow = new Flow();
		Match match = new Match();
		List<Action> actions = new ArrayList<Action>();
		match.setField(MatchType.DL_TYPE, (short) 0x0800);
		match.setField(MatchType.IN_PORT, this.connectors.get(inPort));
		match.setField(MatchType.NW_TOS, (byte)0);
		try {
			match.setField(MatchType.NW_DST, InetAddress.getByName(dst),
					InetAddress.getByName(mask));
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
		actions.add(new SetDlDst(this.getMacAddressBytes(dstMac)));
		actions.add(new Output(this.connectors.get(outPort)));

		flow.setMatch(match);
		flow.setPriority((short)10);
		flow.setActions(actions);
		return flow;
	}

	private Flow initailRule(String dst, String mask, String inPort, String dstMac,
			 String outPort, short priority) {
		Flow flow = new Flow();
		Match match = new Match();
		List<Action> actions = new ArrayList<Action>();
		match.setField(MatchType.DL_TYPE, (short) 0x0800);
		match.setField(MatchType.IN_PORT, this.connectors.get(inPort));
		match.setField(MatchType.NW_TOS, (byte)0);
		try {
			match.setField(MatchType.NW_DST, InetAddress.getByName(dst),
					InetAddress.getByName(mask));
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
		actions.add(new SetDlDst(this.getMacAddressBytes(dstMac)));
		actions.add(new Output(this.connectors.get(outPort)));

		flow.setMatch(match);
		flow.setPriority(priority);
		flow.setActions(actions);
		return flow;
	}
	
	private Flow initailRule(String dst, String mask, String dstMac,
			String dstPort) {
		Flow flow = new Flow();
		Match match = new Match();
		List<Action> actions = new ArrayList<Action>();
		match.setField(MatchType.DL_TYPE, (short) 0x0800);
		match.setField(MatchType.NW_TOS, (byte)0);
		try {
			match.setField(MatchType.NW_DST, InetAddress.getByName(dst),
					InetAddress.getByName(mask));
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
		actions.add(new SetDlDst(this.getMacAddressBytes(dstMac)));
		actions.add(new Output(this.connectors.get(dstPort)));

		flow.setMatch(match);
		flow.setActions(actions);
		return flow;
	}
	
	public void addForwardingTable(int mbid, int connectorID){
		this.forwardingTable.put(new Integer(mbid), this.connectors.get(Integer.toString(connectorID)));
	}
	
	public void delForwardingTable(int mbid){
		this.forwardingTable.remove(new Integer(mbid));
	}
	
	public void clearForwardingTable(){
		this.forwardingTable.clear();
		for(Flow flow: this.ipRoute){
			this.removeFlowRule(flow);
		}
	}
	
	public String dumpForwardingTable(){
		StringBuffer bb = new StringBuffer();
		for(Integer mbid: this.forwardingTable.keySet()){
			bb.append(mbid.toString() + ":" + this.forwardingTable.get(mbid).getNodeConnectorIDString()+"\n");
		}
		return bb.toString();
	}

	private byte[] getMacAddressBytes(String macAddress) {
		String[] macAddressParts = macAddress.split(":");
		byte[] macAddressBytes = new byte[6];
		for (int i = 0; i < 6; i++) {
			Integer hex = Integer.parseInt(macAddressParts[i], 16);
			macAddressBytes[i] = hex.byteValue();
		}
		return macAddressBytes;
	}
	

}
