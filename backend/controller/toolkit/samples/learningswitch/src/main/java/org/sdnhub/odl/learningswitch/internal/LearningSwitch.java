
package org.sdnhub.odl.learningswitch.internal;

import org.sdnhub.odl.learningswitch.ILearningSwitch;
import org.sdnhub.odl.learningswitch.LearningSwitchOptions;
import org.sdnhub.odl.learningswitch.MacTable;
import org.sdnhub.odl.learningswitch.MacTable.MacPortTableElem;
import org.sdnhub.odl.learningswitch.Table;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ConcurrentHashMap;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.Map;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;


import org.opendaylight.controller.sal.utils.EtherTypes;
import org.opendaylight.controller.sal.utils.HexEncode;
import org.opendaylight.controller.sal.utils.Status;
import org.opendaylight.controller.sal.action.Action;
import org.opendaylight.controller.sal.action.Output;
import org.opendaylight.controller.sal.core.ConstructionException;
import org.opendaylight.controller.sal.core.Node;
import org.opendaylight.controller.sal.core.NodeConnector;
import org.opendaylight.controller.sal.core.Property;
import org.opendaylight.controller.sal.core.UpdateType;
import org.opendaylight.controller.sal.flowprogrammer.Flow;
import org.opendaylight.controller.sal.flowprogrammer.IFlowProgrammerService;
import org.opendaylight.controller.sal.match.Match;
import org.opendaylight.controller.sal.match.MatchField;
import org.opendaylight.controller.sal.match.MatchType;
import org.opendaylight.controller.sal.packet.BitBufferHelper;
import org.opendaylight.controller.sal.packet.Ethernet;
import org.opendaylight.controller.sal.packet.IDataPacketService;
import org.opendaylight.controller.sal.packet.IListenDataPacket;
import org.opendaylight.controller.sal.packet.IPv4;
import org.opendaylight.controller.sal.packet.TCP;
import org.opendaylight.controller.sal.packet.UDP;
import org.opendaylight.controller.sal.packet.Packet;
import org.opendaylight.controller.sal.packet.PacketResult;
import org.opendaylight.controller.sal.packet.RawPacket;
import org.opendaylight.controller.switchmanager.IInventoryListener;
import org.opendaylight.controller.switchmanager.ISwitchManager;

public class LearningSwitch implements IListenDataPacket, ILearningSwitch, IInventoryListener {
    protected static final Logger logger = LoggerFactory.getLogger(LearningSwitch.class);
    private IDataPacketService dataPacketService = null;
    private ISwitchManager switchManager = null;
    private IFlowProgrammerService programmer = null;
    
    private Table table = null;
    private LearningSwitchOptions src_opts = null, dst_opts = null;

    private String function = "switch";

    void init() {
        logger.info("Initializing Simple application");
        table = new Table();
        src_opts = new LearningSwitchOptions();
        src_opts.options  |= LearningSwitchOptions.SRC_MAC;
        dst_opts = new LearningSwitchOptions();
        dst_opts.options  |= LearningSwitchOptions.DST_MAC;
    }
    void start() {
        logger.info("Simple application starting");
    }

    void stop() {
        logger.info("Simple application stopping");
    }

    void setDataPacketService(IDataPacketService s) {
        this.dataPacketService = s;
    }

    void unsetDataPacketService(IDataPacketService s) {
        if (this.dataPacketService == s) {
            this.dataPacketService = null;
        }
    }

    public void setFlowProgrammerService(IFlowProgrammerService s)
    {
        this.programmer = s;
    }

    public void unsetFlowProgrammerService(IFlowProgrammerService s) {
        if (this.programmer == s) {
            this.programmer = null;
        }
    }

    public void notifyNode(Node node, UpdateType type,
            Map<String, Property> propMap) {
    	if (type == UpdateType.ADDED)
    		this.table.addNode(node);
    	else if (type == UpdateType.REMOVED)
    		this.table.deleteNode(node);

    }

    public void notifyNodeConnector(NodeConnector nodeConnector,
            UpdateType type, Map<String, Property> propMap) {

//    	if (type == UpdateType.ADDED)
//            this.macToPortTable.initNodeConnector(nodeConnector);
//        else if (type == UpdateType.REMOVED)
//            this.macToPortTable.clearNodeConnector(nodeConnector);

    }

    void setSwitchManager(ISwitchManager s) {
        logger.debug("SwitchManager set");
        this.switchManager = s;
    }

    void unsetSwitchManager(ISwitchManager s) {
        if (this.switchManager == s) {
            logger.debug("SwitchManager removed!");
            this.switchManager = null;
        }
    }

    private void floodPacket(RawPacket inPkt) {
    	NodeConnector incoming_connector = inPkt.getIncomingNodeConnector();
        Node incoming_node = incoming_connector.getNode();

        Set<NodeConnector> nodeConnectors =
                this.switchManager.getUpNodeConnectors(incoming_node);

        for (NodeConnector nc : nodeConnectors) {
            if (!nc.equals(incoming_connector) &&
            		(nc.getType() != NodeConnector.NodeConnectorIDType.SWSTACK)) {
                try {
                    RawPacket destPkt = new RawPacket(inPkt);
                    destPkt.setOutgoingNodeConnector(nc);
                    this.dataPacketService.transmitDataPacket(destPkt);
                } catch (ConstructionException e2) {
                    continue;
                }
            }
        }
    }

    @Override
    public PacketResult receiveDataPacket(RawPacket inPkt) {
        if (inPkt == null) {
            return PacketResult.IGNORED;
        }

        Packet formattedPak = this.dataPacketService.decodeDataPacket(inPkt);
        if (!(formattedPak instanceof Ethernet)) {
            return PacketResult.IGNORED;
        }

        //Ignore LLDP packets. They should never be flooded out
        Ethernet etherPak = (Ethernet)formattedPak;
        if (etherPak.getEtherType() == EtherTypes.LLDP.intValue())
            return PacketResult.IGNORED;

        // Hub implementation
        if (function.equals("hub")) {
            floodPacket(inPkt);
        } else {
            NodeConnector incoming_connector = inPkt.getIncomingNodeConnector();
            
            try {
    			this.table.learnSourceFields((Packet)etherPak, incoming_connector, src_opts);
//    			logger.info("Table entries: {}" + this.table.toString());
    		} catch (NoSuchFieldException e) {
    			// TODO Auto-generated catch block
    			logger.error("Error learning source fields: {}", e.toString());
    		}
            
            NodeConnector outgoing_connector = null;
            try {
    			outgoing_connector = this.table.getDestinationNodeConnector((Packet)etherPak, incoming_connector.getNode(), dst_opts);
    		} catch (NoSuchFieldException e) {
    			// TODO Auto-generated catch block
    			logger.error("Error learning source fields: {}", e.toString());
    		}
                        
            if (outgoing_connector == null) {
                floodPacket(inPkt);
            } else {
                if (!programFlow(etherPak, incoming_connector,
                            outgoing_connector)) {
                    return PacketResult.IGNORED;
                }
            }
        }
        return PacketResult.CONSUME;
    }

    private boolean programFlow(Ethernet etherPak,
            NodeConnector incoming_connector,
            NodeConnector outgoing_connector) {
        byte[] dstMAC = etherPak.getDestinationMACAddress();

        Match match = new Match();
        match.setField(new MatchField(MatchType.IN_PORT, incoming_connector) );
        match.setField(new MatchField(MatchType.DL_DST, dstMAC.clone()));

        List<Action> actions = new ArrayList<Action>();
        actions.add(new Output(outgoing_connector));

        Flow f = new Flow(match, actions);
        f.setIdleTimeout((short)5);

        // Modify the flow on the network node
        Node incoming_node = incoming_connector.getNode();
        Status status = programmer.addFlow(incoming_node, f);

        if (!status.isSuccess()) {
            logger.warn("SDN Plugin failed to program the flow: {}. The failure is: {}",
                    f, status.getDescription());
            return false;
        } else {
            return true;
        }
    }

    @Override
    public Table  getData()
    {
        //return macToPortTable.getMap();
    	return this.table;
    }
    
    @Override
    public void deleteData()
    {
    	this.table.clear();
    }
    
    
    @Override
    public String getFunction() {
    	return this.function;
    }
    
    @Override
    public Boolean setFunction(String fn) {
    	Boolean ret = Boolean.TRUE;
    	if (fn.equals("hub")) {
    		function = "hub";
    	} else if (fn.equals("switch")) {
    		function = "switch";
    	} else { 
    		ret = Boolean.FALSE;
    	}
    	return ret;
    }
}
