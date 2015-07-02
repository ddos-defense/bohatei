package org.sdnhub.flowtags;

import java.math.BigInteger;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import org.opendaylight.controller.sal.action.Action;
import org.opendaylight.controller.sal.action.Drop;
import org.opendaylight.controller.sal.action.Loopback;
import org.opendaylight.controller.sal.action.SetDlDst;
import org.opendaylight.controller.sal.action.SetNwDst;
import org.opendaylight.controller.sal.action.SetNwSrc;
import org.opendaylight.controller.sal.action.SetNwTos;
import org.opendaylight.controller.sal.action.Output;
import org.opendaylight.controller.sal.action.SetTpDst;
import org.opendaylight.controller.sal.action.SetTpSrc;
import org.opendaylight.controller.sal.action.SetVlanId;
import org.opendaylight.controller.sal.core.Node;
import org.opendaylight.controller.sal.core.NodeConnector;
import org.opendaylight.controller.sal.flowprogrammer.Flow;
import org.opendaylight.controller.sal.flowprogrammer.IFlowProgrammerService;
import org.opendaylight.controller.sal.match.Match;
import org.opendaylight.controller.sal.match.MatchType;
import org.opendaylight.controller.switchmanager.ISwitchManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EdgeResponder extends OFNode {
	static short classificationRoutePriority = 10;
	static short attackBlockPriroity = 300;
	private NodeConnector hostPort;
	private NodeConnector netPort;
	private static final Logger logger = LoggerFactory
			.getLogger(EdgeResponder.class);
	private FTMiddeBoxes.TYPE mbType = null;
	private ArrayList<Flow> inOutFlow = null;
	private ArrayList<Flow> tagRoute = null;
	private ArrayList<Flow> explicitFlow = null;

	public EdgeResponder(Node node, ISwitchManager switchManager,
			IFlowProgrammerService programmer, FTMiddeBoxes.TYPE mbType) {
		super(node, switchManager, programmer);
		EdgeResponder.classificationRoutePriority = 10;
		EdgeResponder.attackBlockPriroity = 300;
		this.mbType = mbType;
		this.explicitFlow = new ArrayList<Flow>();
		this.update(null);
		/*
		 * this.inOutFlow = new ArrayList<Flow>(); this.hostPort =
		 * this.connectors.get("1"); this.netPort = this.connectors.get("2"); if
		 * (this.setInitialRule() != true) { logger.info(this.node.toString() +
		 * " is not well initialised"); } this.tagRoute = new ArrayList<Flow>();
		 */
	}

	public void update(NodeConnector nodeConnector) {
		if (nodeConnector != null) {
			this.connectors.put(nodeConnector.getNodeConnectorIDString(),
					nodeConnector);
		}
		if (this.connectors.get("1") == null
				|| this.connectors.get("2") == null) {
			return;
		} else {
			this.hostPort = this.connectors.get("1");
			this.netPort = this.connectors.get("2");
			this.inOutFlow = new ArrayList<Flow>();
			if (this.setInitialRule() != true) {
				logger.info(this.node.toString() + " is not well initialised");
			}
			this.tagRoute = new ArrayList<Flow>();
		}
	}

	public NodeConnector getInPort() {
		return this.hostPort;
	}

	public NodeConnector getOutPort() {
		return this.netPort;
	}

	public void setMBType(FTMiddeBoxes.TYPE type) {
		this.mbType = type;
		this.setMBRule();
	}

	public FTMiddeBoxes.TYPE getMBType() {
		return this.mbType;
	}

	private boolean setMBRule() {
		Flow flow = new Flow();
		Match match = new Match();
		List<Action> actions = new ArrayList<Action>();
		logger.info("NodeId:"+this.node.getNodeIDString());
		for (Flow iOFlow : this.inOutFlow) {
			this.removeFlowRule(iOFlow);
		}
		this.inOutFlow.clear();
		match.setField(MatchType.IN_PORT, this.hostPort);
		match.setField(MatchType.DL_TYPE, (short) 0x0800);

		/*
		  // Dirty Hack:This is required for forward every traffic to first NAT
		  if(this.mbType==null || this.mbType == FTMiddeBoxes.TYPE.FT_CONSUME && this.node.getNodeIDString().equals(FTUtil.getSWID(1)))
			  actions.add(new SetNwTos((byte)4));
			  */

	 //if this mbType is monitor, just simply drop the tags
		if(this.mbType == FTMiddeBoxes.TYPE.FT_MONITOR){
			actions.add(new SetNwTos((byte)0));
		}
		actions.add(new Output(this.netPort));
		flow.setMatch(match);
		flow.setPriority((short) 0);
		flow.setActions(actions);
		if (this.addFlowRule(flow) != true) {
			return false;
		}
		this.inOutFlow.add(flow);

		flow = new Flow();
		match = new Match();
		actions = new ArrayList<Action>();

		match.setField(MatchType.IN_PORT, this.netPort);
		match.setField(MatchType.DL_TYPE, (short) 0x0800);

		if (this.mbType != FTMiddeBoxes.TYPE.FT_NAT) {
			// If the mbType is NAT, we need to generate and consume.
			// Every packet flow goes to controller
			match.setField(MatchType.IN_PORT, this.netPort);
			match.setField(MatchType.DL_TYPE, (short) 0x0800);
			if (this.mbType == FTMiddeBoxes.TYPE.FT_NON_CONSUME) {
				// If the mbType is NON_CONSUME, we only need to consume
				// Packet with not 0 tag comes to controller
				match.setField(MatchType.NW_TOS, (byte) 0);
			}
			// If the mbType is consume or null(Edge), Just simply forward every
			// packet
			//
			actions.add(new SetDlDst(this.getMacAddressFromDPID()));
			actions.add(new Output(this.hostPort));
			flow.setMatch(match);
			flow.setPriority((short) 0);
			flow.setActions(actions);
			if (this.addFlowRule(flow) != true) {
				return false;
			}
			this.inOutFlow.add(flow);
		}
		return true;
	}

	public boolean setInitialRule() {
		super.setInitialRule();
		return this.setMBRule();
	}

	public void removeAllTagRoutes() {
		for (Flow flow : this.tagRoute) {
			this.removeFlowRule(flow);
		}
		this.tagRoute.clear();
	}

	private void debug() {
		Flow flow = new Flow();
		Match match = new Match();
		List<Action> actions = new ArrayList<Action>();
		match.setField(MatchType.DL_TYPE, (short) 0x0800);
		match.setField(MatchType.NW_TOS, (byte) (8));
		flow.setMatch(match);
		flow.setPriority((short) 10);
		flow.setActions(actions);
		this.addFlowRule(flow);
	}

	public boolean setNatGenerateRule(FTFiveTuple org, int orgTag, int newTag) {
		Flow flow = new Flow();
		Match match = new Match();
		List<Action> actions = new ArrayList<Action>();
		try {
			match.setField(MatchType.DL_TYPE, (short) 0x0800);
			match.setField(MatchType.NW_SRC, InetAddress
					.getByAddress(BigInteger.valueOf(org.getNwSrc())
							.toByteArray()), InetAddress
					.getByName("255.255.255.255"));
			match.setField(MatchType.NW_DST, InetAddress
					.getByAddress(BigInteger.valueOf(org.getNwDst())
							.toByteArray()), InetAddress
					.getByName("255.255.255.255"));
			logger.info("NAT NW_PROTO:" + org.getNwProto());
			if (org.getNwProto() != 0x01) {
				// TCP or UDP
				logger.info("NW_PROTO:" + org.getNwProto());
				match.setField(MatchType.TP_SRC, new Short((short)org.getTpSrcPort()));
				match.setField(MatchType.TP_DST, new Short((short)org.getTpDstPort()));
			}
			match.setField(MatchType.NW_TOS,  new Byte((byte)orgTag));
			match.setField(MatchType.NW_PROTO, new Byte((byte)org.getNwProto()));
			match.setField(MatchType.IN_PORT, this.netPort);
		} catch (UnknownHostException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		if (newTag != 0 && newTag != -1) {
			actions.add(new SetNwTos(new Byte((byte)(newTag))));
		}
		// Dirty Hack for First NAT
		if (orgTag == 1 && newTag == -1 ){
			actions.add(new SetNwTos(new Byte((byte)newTag)));
		}
		actions.add(new SetDlDst(this.getMacAddressFromDPID()));
		actions.add(new Output(this.hostPort));
		flow.setMatch(match);
		flow.setActions(actions);
		System.out.println("NAT GENERAGE");
		if (this.addFlowRule(flow)) {
			this.tagRoute.add(flow);
		}
		return true;
	}

	public boolean setConsumeRule(FTFiveTuple org, FTFiveTuple nextTuple,
			int tag, int newTag) {
		this.setConsumeFiveTupleRule(org, nextTuple, true, tag, newTag);
		FTFiveTuple reverseOrg = new FTFiveTuple(org.getNwDst(),
				org.getNwSrc(), org.getTpDstPort(), org.getTpSrcPort(),
				org.getNwProto());
		FTFiveTuple reverseNextTuple = new FTFiveTuple(nextTuple.getNwDst(),
				nextTuple.getNwSrc(), nextTuple.getTpDstPort(),
				nextTuple.getTpSrcPort(), nextTuple.getNwProto());
		this.setConsumeFiveTupleRule(reverseNextTuple, reverseOrg, false, tag,
				newTag);
		this.setConsumeFiveTupleRule(nextTuple, org, false, tag, newTag);
		return true;
	}

	private boolean setConsumeFiveTupleRule(FTFiveTuple org,
			FTFiveTuple modified, boolean toMB, int tag, int newTag) {
		Flow flow = new Flow();
		Match match = new Match();
		List<Action> actions = new ArrayList<Action>();
		match.setField(MatchType.DL_TYPE, (short) 0x0800);
		logger.info("Org: " + org.toString());
		logger.info("Mod: " + modified.toString());
		try {
			match.setField(MatchType.NW_SRC, InetAddress
					.getByAddress(BigInteger.valueOf(org.getNwSrc())
							.toByteArray()), InetAddress
					.getByName("255.255.255.255"));
			match.setField(MatchType.NW_DST, InetAddress
					.getByAddress(BigInteger.valueOf(org.getNwDst())
							.toByteArray()), InetAddress
					.getByName("255.255.255.255"));
			logger.info("CONSUME NW_PROTO:" + org.getNwProto());
			if (org.getNwProto() != 0x01) {
				// TCP or UDP
				match.setField(MatchType.TP_SRC, (short) org.getTpSrcPort());
				match.setField(MatchType.TP_DST, (short) org.getTpDstPort());
			}
			match.setField(MatchType.NW_PROTO, (byte) org.getNwProto());
			System.out.println("HEREERRE:" + toMB);
			if (toMB) {
				logger.info("CONSUME NW TOS:" + tag);
				match.setField(MatchType.NW_TOS, (byte) (tag / 4));
			}
		} catch (UnknownHostException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		if (modified.getNwSrc() != 0) {
			try {
				actions.add(new SetNwSrc(InetAddress.getByAddress(BigInteger
						.valueOf(modified.getNwSrc()).toByteArray())));
			} catch (UnknownHostException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		if (modified.getNwDst() != 0) {
			try {
				actions.add(new SetNwDst(InetAddress.getByAddress(BigInteger
						.valueOf(modified.getNwDst()).toByteArray())));
			} catch (UnknownHostException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		if (modified.getTpSrcPort() != 0) {
			short modifiedSrcPort = (short) modified.getTpSrcPort();
			actions.add(new SetTpSrc(modifiedSrcPort & 0xffff));
		}

		if (modified.getTpDstPort() != 0) {
			short modifiedDstPort = (short) modified.getTpDstPort();
			actions.add(new SetTpDst(modifiedDstPort & 0xffff));
		}

		if (toMB) {

			actions.add(new SetDlDst(this.getMacAddressFromDPID()));
			// actions.add(new SetNwTos((byte) (newTag/4)));
			byte bNewTag = (byte)(newTag);
			System.out.println("CONSUME");
			actions.add(new SetNwTos( (bNewTag & 0xff)));
			actions.add(new Output(this.hostPort));
		} else {
			actions.add(new Output(this.netPort));
		}

		flow.setMatch(match);
		flow.setActions(actions);
		flow.setPriority((short) 10);
		if (this.addFlowRule(flow)) {
			this.tagRoute.add(flow);
		}
		return true;
	}

	public boolean setTestSenderRule(int testTag, int returnTag,
			int testGeneratorSrcIP, int testSrcIP) {
		Flow flow = new Flow();
		Match match = new Match();
		List<Action> actions = new ArrayList<Action>();
		try {
			System.out.println("TestTag:" + testTag + " returnTag:" + returnTag
					+ " testSrcIP:" + FTUtil.IPAddressStringFromInt(testSrcIP)
					+ " testGenIP:"
					+ FTUtil.IPAddressStringFromInt(testGeneratorSrcIP));
		} catch (UnknownHostException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		match.setField(MatchType.DL_TYPE, (short) 0x0800);
		match.setField(MatchType.NW_TOS, (byte) (testTag / 4));
		try {
			actions.add(new SetNwTos((byte) 0));
			actions.add(new SetNwSrc(FTUtil.InetAddressFromInt(testSrcIP)));
			actions.add(new Loopback());
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		flow.setMatch(match);
		flow.setActions(actions);
		flow.setPriority((short) 200);
		this.addFlowRule(flow);

		flow = new Flow();
		match = new Match();
		actions = new ArrayList<Action>();
		try {
			match.setField(MatchType.DL_TYPE, (short) 0x0800);
			match.setField(MatchType.NW_DST,
					(FTUtil.InetAddressFromInt(testSrcIP)));
			actions.add(new SetNwTos((byte) returnTag));
			actions.add(new SetNwDst(FTUtil
					.InetAddressFromInt(testGeneratorSrcIP)));
			actions.add(new Loopback());
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		flow.setMatch(match);
		flow.setActions(actions);
		flow.setPriority((short) 200);
		this.addFlowRule(flow);

		return true;
	}

	public boolean setExplicitTagGenerate(int srcIP, int dstIP, int tpSrcPort,
			int tpDstPort, int proto, int tag) {
		Flow flow = new Flow();
		Match match = new Match();
		List<Action> actions = new ArrayList<Action>();
		match.setField(MatchType.DL_TYPE, (short) 0x0800);
		try {
			if(srcIP != 0 ){
				match.setField(MatchType.NW_SRC, FTUtil.InetAddressFromInt(srcIP));
			} 
			if(dstIP != 0){
			match.setField(MatchType.NW_DST, FTUtil.InetAddressFromInt(dstIP));
			}
			match.setField(MatchType.IN_PORT, this.hostPort);
			if(proto != 0){
				match.setField(MatchType.NW_PROTO, (byte) proto);
			}
			if(tpSrcPort != 0){
			match.setField(MatchType.TP_SRC, (short)tpSrcPort);
			}
			if(tpDstPort != 0){
			match.setField(MatchType.TP_DST, (short)tpDstPort);
			}
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if(tag != 0){
			flow.setPriority((short) 150);
			actions.add(new SetNwTos((byte) tag));
			actions.add(new Output(this.netPort));
		} else {
			flow.setPriority((short) 200);
			
		}
		flow.setMatch(match);
		flow.setActions(actions);
		this.explicitFlow.add(flow);
		return this.addFlowRule(flow);
	}

	public boolean delExplicitTagGenerate(int srcIP, int dstIP) {
		try {
			Flow f = null;
			for (Flow flow : this.explicitFlow) {
				Match match = flow.getMatch();
				if (match.getField(MatchType.NW_SRC).toString().contains(FTUtil.IPAddressStringFromInt(srcIP))
						&& match.getField(MatchType.NW_DST).toString().contains(FTUtil.IPAddressStringFromInt(dstIP))){
					f = flow;
					break;
				}
			}
			if (f != null) {
				this.removeFlowRule(f);
				this.explicitFlow.remove(f);
			}
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return true;
	}

	public void clearExplicitTagGenerate() {
		for (Flow flow : this.explicitFlow) {
			this.removeFlowRule(flow);
		}
		this.explicitFlow.clear();
	}

	private byte[] getMacAddressFromDPID() {
		String dpid = this.node.getNodeIDString();
		String[] dpids = dpid.split(":");
		StringBuffer bb = new StringBuffer();
		for (int index = 2; index < dpids.length - 1; index++) {
			bb.append(dpids[index]);
			bb.append(":");
		}
		bb.append(dpids[dpids.length - 1]);
		return FTUtil.MacAddressBytesFromString(bb.toString());
	}
}
