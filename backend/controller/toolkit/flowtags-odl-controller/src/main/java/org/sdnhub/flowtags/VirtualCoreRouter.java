package org.sdnhub.flowtags;

import java.util.ArrayList;
import java.util.List;

import org.opendaylight.controller.sal.action.Action;
import org.opendaylight.controller.sal.action.Output;
import org.opendaylight.controller.sal.core.Node;
import org.opendaylight.controller.sal.core.NodeConnector;
import org.opendaylight.controller.sal.flowprogrammer.Flow;
import org.opendaylight.controller.sal.flowprogrammer.IFlowProgrammerService;
import org.opendaylight.controller.sal.match.Match;
import org.opendaylight.controller.sal.match.MatchType;
import org.opendaylight.controller.switchmanager.ISwitchManager;

public class VirtualCoreRouter extends CoreRouter {
	private NodeConnector hostPort = null;
	private ArrayList<NodeConnector> netPorts = null;
	/*
	 * private NodeConnector corePort1 = null; private NodeConnector corePort2 =
	 * null;
	 */
	private final static short INLINE_PRIORITY = 100;

	public VirtualCoreRouter(Node node, ISwitchManager switchManager,
			IFlowProgrammerService programmer, FTMiddeBoxes mbes) {
		super(node, switchManager, programmer, mbes);
		System.out.println(this.connectors.size());
		this.netPorts = new ArrayList<NodeConnector>();
		/*
		 * this.hostPort = this.connectors.get("1"); this.netPorts = new
		 * ArrayList<NodeConnector>(); this.corePort1 =
		 * this.connectors.get("2"); this.corePort2 = this.connectors.get("3");
		 */
		for (String connectorId : this.connectors.keySet()) {
			if (connectorId.equals("1"))
				this.hostPort = this.connectors.get(connectorId);
			else
				this.netPorts.add(this.connectors.get(connectorId));
		}
		this.setInlineRule();
	}

	private void setInlineRule() {
		Flow flow;
		Match match;
		List<Action> actions;

		for (NodeConnector c : this.netPorts) {
			flow = new Flow();
			match = new Match();
			actions = new ArrayList<Action>();
			match.setField(MatchType.DL_TYPE, (short) 0x0800);
			match.setField(MatchType.NW_TOS, (byte) 0);
			match.setField(MatchType.IN_PORT, c);
			actions.add(new Output(this.hostPort));
			flow.setMatch(match);
			flow.setActions(actions);
			flow.setPriority(VirtualCoreRouter.INLINE_PRIORITY);
			this.addFlowRule(flow);
		}

		/*
		 * flow = new Flow(); match = new Match(); actions = new
		 * ArrayList<Action>(); match.setField(MatchType.DL_TYPE, (short)
		 * 0x0800); match.setField(MatchType.NW_TOS, (byte)0);
		 * match.setField(MatchType.IN_PORT, this.corePort2); actions.add(new
		 * Output(this.hostPort)); flow.setMatch(match);
		 * flow.setActions(actions);
		 * flow.setPriority(VirtualCoreRouter.INLINE_PRIORITY);
		 * this.addFlowRule(flow);
		 */
	}

	private void setRoute() {
	}
}
