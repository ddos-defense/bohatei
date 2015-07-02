package org.sdnhub.flowtags;

import java.io.File;
import java.io.IOException;
import java.math.BigInteger;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.net.SocketOption;
import java.net.StandardSocketOptions;
import java.net.UnknownHostException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.channels.spi.SelectorProvider;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import org.opendaylight.controller.sal.core.Node;
import org.opendaylight.controller.sal.core.NodeConnector;
import org.opendaylight.controller.sal.core.Property;
import org.opendaylight.controller.sal.core.UpdateType;
import org.opendaylight.controller.sal.flowprogrammer.IFlowProgrammerService;
import org.opendaylight.controller.sal.packet.Ethernet;
import org.opendaylight.controller.sal.packet.IPv4;
import org.opendaylight.controller.sal.packet.IDataPacketService;
import org.opendaylight.controller.sal.packet.IListenDataPacket;
import org.opendaylight.controller.sal.packet.Packet;
import org.opendaylight.controller.sal.packet.PacketResult;
import org.opendaylight.controller.sal.packet.RawPacket;
import org.opendaylight.controller.sal.packet.TCP;
import org.opendaylight.controller.sal.packet.UDP;
import org.opendaylight.controller.sal.utils.GlobalConstants;
import org.opendaylight.controller.switchmanager.IInventoryListener;
import org.opendaylight.controller.switchmanager.ISwitchManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

//import com.mysql.jdbc.log.Log;

public class Flowtags implements IInventoryListener, IFlowtags, IListenDataPacket {

	protected static final Logger log = LoggerFactory.getLogger(Flowtags.class);

	private String ROOT = GlobalConstants.STARTUPHOME.toString();
	public String rulesetFile = ROOT + "proactive-ruleset.xml";

	//
	private boolean running;
	private Thread flowtagsThread;
	private Thread psiThread;

	private int flowtags_port = 2020;
	private int psi_port = 2021;
	private ServerSocketChannel serverSocket;
	private ServerSocketChannel serverSocket2;
	private Selector selector;
	private Selector selector2;
	private InetAddress controllerIP = new InetSocketAddress(0).getAddress();; // means
																				// any
	private SelectionKey serverSelectionKey;
	private SelectionKey serverSelectionKey2;

	private AtomicInteger middleboxInstanceNumber;

	/* Added by Toby for control message */
	private FTTags ftTags = null;
	private FTOutputTags ftOutTags = null;
	private FTMiddeBoxes mbes = null;
	private CoreRouter coreRouter = null;
	private HashMap<String, EdgeResponder> edgeSW = null;
	private CoreRouters coreRouters = null;

	// private List<MiddleboxHandler> mbInstances = new
	// ArrayList<MiddleboxHandler>();

	private ConcurrentHashMap<Integer, MiddleboxHandler> middleboxes;

	// ------------------------------------
	private IFlowProgrammerService programmer = null;
	private ISwitchManager switchManager = null;
	private IDataPacketService dataPacketService = null;

	void setDataPacketService(IDataPacketService s) {
		this.dataPacketService = s;
	}

	void unsetDataPacketService(IDataPacketService s) {
		if (this.dataPacketService == s) {
			this.dataPacketService = null;
		}
	}

	public void setFlowProgrammerService(IFlowProgrammerService s) {
		log.debug("Programmer is set");
		this.programmer = s;
	}

	public void unsetFlowProgrammerService(IFlowProgrammerService s) {
		if (this.programmer == s) {
			log.debug("Programmer is removed");
			this.programmer = null;
		}
	}

	void setSwitchManager(ISwitchManager s) {
		log.debug("SwitchManager set");
		this.switchManager = s;
	}

	void unsetSwitchManager(ISwitchManager s) {
		if (this.switchManager == s) {
			log.debug("SwitchManager removed!");
			this.switchManager = null;
		}
	}

	// ------------------------------------

	void init() {
		log.info("Init flowtags");
        
		this.middleboxInstanceNumber = new AtomicInteger(0);
		this.middleboxes = new ConcurrentHashMap<Integer, MiddleboxHandler>();

        // create psi policy manager instance
        PSIPolicyManager.getInstance();
        // PSIDAGModifier.getInstance().execCMD("/testbed/bin/topo_start 192.168.123.201 /testbed/topology/topo_demo1.txt local");     
	}

	public void start() {
		log.info("Starting flowtags");
		this.running = true;
		this.ftTags = new FTTags();
		this.ftOutTags = new FTOutputTags(ftTags);
		this.mbes = new FTMiddeBoxes();
		this.edgeSW = new HashMap<String, EdgeResponder>();
		this.coreRouters = new CoreRouters();

		flowtagsThread = new Thread(new Runnable() {
			@Override
			public void run() {
				try {

					if (!startAcceptConnections()) {
						return;
					}
					log.info(
							"FlowTags is now listening on {}:{}",
							(controllerIP == null) ? "any" : controllerIP
									.getHostAddress(), flowtags_port);

					while (running) {

						selector.select(5000);
						Iterator<SelectionKey> selectedKeys = selector
								.selectedKeys().iterator();

						while (selectedKeys.hasNext()) {
							SelectionKey skey = selectedKeys.next();
							selectedKeys.remove();
							if (skey.isValid() && skey.isAcceptable()) {
								// ((Controller) listener).handleNewConnection(
								// selector, serverSelectionKey);
								log.info("new incoming connection");
								handleNewConnection(selector,
										serverSelectionKey);

							}
						}
					}

					// serverSocket.close();
					// log.info("Flowtags Server Stopped");

				} catch (SocketException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}, "FlowtagsI/O Thread");

		// flowtagsThread.start();
		// log.info("Flowtag Start");
        
		psiThread = new Thread(new Runnable() {
			@Override
			public void run() {
				try {

					if (!startAcceptConnections(psi_port)) {
						return;
					}
					log.info(
							"PSI is now listening on {}:{}",
							(controllerIP == null) ? "any" : controllerIP
									.getHostAddress(), psi_port);

					while (running) {

						selector2.select(5000);
						Iterator<SelectionKey> selectedKeys2 = selector2
								.selectedKeys().iterator();

						while (selectedKeys2.hasNext()) {
							SelectionKey skey = selectedKeys2.next();
							selectedKeys2.remove();
							if (skey.isValid() && skey.isAcceptable()) {
								// ((Controller) listener).handleNewConnection(
								// selector, serverSelectionKey);
								log.info("new incoming connection");
								handleNewConnectionPSI(selector2, serverSelectionKey2);

							}
						}
					}

					// serverSocket.close();
					// log.info("Flowtags Server Stopped");

				} catch (SocketException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}, "PSI I/O Thread");

		//psiThread.start();
		//log.info("PSI Start");
        
		flowtagsThread.start();
		log.info("Flowtag Start");
		psiThread.start();
		log.info("PSI Start");

	}

	private void handleNewConnection(Selector selector, SelectionKey serverSelectionKey) {
		ServerSocketChannel ssc = (ServerSocketChannel) serverSelectionKey.channel();
		SocketChannel sc = null;
		try {
			sc = ssc.accept();
			// create new switch
			int i = this.middleboxInstanceNumber.addAndGet(1);
			String instanceName = "MiddleboxHandler-" + i;
			MiddleboxHandler middleboxHandler = new MiddleboxHandler(sc,
					instanceName, this.ftTags, this.ftOutTags,
					this.coreRouters, this.edgeSW, this.mbes);
			middleboxHandler.start();

			middleboxes.put(i, middleboxHandler);

			if (sc.isConnected()) {
				log.info("Middlebox:{} is connected to the Controller", sc
						.socket().getRemoteSocketAddress().toString()
						.split("/")[1]);
			}

		} catch (IOException e) {
			return;
		}
	}


	private void handleNewConnectionPSI(Selector selector, SelectionKey serverSelectionKey) {
		ServerSocketChannel ssc = (ServerSocketChannel) serverSelectionKey.channel();
		SocketChannel sc = null;
		try {
			sc = ssc.accept();
            log.info("ssc.accept()");
			// create new switch
            
			int i = this.middleboxInstanceNumber.addAndGet(1);
			String instanceName = "MiddleboxHandler-" + i;

			PSIEventHandler middleboxHandler = new PSIEventHandler(sc);
			middleboxHandler.start();

			// middleboxes.put(i, middleboxHandler);
            
			if (sc.isConnected()) {
				log.info("Snort:{} is connected to the Controller", sc
						.socket().getRemoteSocketAddress().toString()
						.split("/")[1]);
			}

		} catch (IOException e) {
			return;
		}
    }

	private boolean startAcceptConnections() {
		if (running) {
			try {
				// obtain a selector
				selector = SelectorProvider.provider().openSelector();
				// create the listening socket
				serverSocket = ServerSocketChannel.open();
				serverSocket.configureBlocking(false);

				serverSocket
						.setOption(StandardSocketOptions.SO_REUSEADDR, true);

				serverSocket.socket().bind(
						new java.net.InetSocketAddress(controllerIP,
								flowtags_port));
				serverSocket.socket().setReuseAddress(true);
				// register this socket for accepting incoming
				// connections
				serverSelectionKey = serverSocket.register(selector,
						SelectionKey.OP_ACCEPT);
			} catch (IOException e) {
				log.error(
						"Flowtags Failed to listen on {}:{}, exit",
						(controllerIP == null) ? "" : controllerIP
								.getHostAddress(), flowtags_port);
				return false;
			}
			return true;
		}
		return false;
	}

    /*
     * PSI: start accept connection for a port
     * */
	private boolean startAcceptConnections(int port) {
		if (running) {
			try {
				// obtain a selector
				selector2 = SelectorProvider.provider().openSelector();
				// create the listening socket
				serverSocket2 = ServerSocketChannel.open();
				serverSocket2.configureBlocking(false);

				serverSocket2
						.setOption(StandardSocketOptions.SO_REUSEADDR, true);

				serverSocket2.socket().bind(
						new java.net.InetSocketAddress(controllerIP,
								port));
				serverSocket2.socket().setReuseAddress(true);
				// register this socket for accepting incoming
				// connections
				serverSelectionKey2 = serverSocket2.register(selector2,
						SelectionKey.OP_ACCEPT);
			} catch (IOException e) {
				log.error(
						"PSI Failed to listen on {}:{}, exit",
						(controllerIP == null) ? "" : controllerIP
								.getHostAddress(), port);
				return false;
			}
			return true;
		}
		return false;
    }

	public void stop() throws IOException {

		log.info("Disconnecting Middleboxes");
		for (Iterator<Entry<Integer, MiddleboxHandler>> it = middleboxes
				.entrySet().iterator(); it.hasNext();) {

			Entry<Integer, MiddleboxHandler> entry = it.next();

			MiddleboxHandler mb = entry.getValue();

			if (mb.isRunning()) {

				log.info("Disconnecting " + entry.getKey());

				mb.stop();
			}

			it.remove();
		}

		log.info("Flowtags Server Stopping");

		// Added by Toby
		this.coreRouters.removeAllFlows();
		for (EdgeResponder edgeSW : this.edgeSW.values()) {
			edgeSW.removeAllFlows();
		}

		if (this.running == true) {
			this.running = false;
			this.serverSocket.socket().close(); // LC
		}
		this.selector.wakeup();
		this.serverSocket.close();
	}

	private void readconfig() {

		File fconfig = new File(rulesetFile);

		if (fconfig.exists() == false || fconfig.canRead() == false) {
			log.error("Could not open: " + rulesetFile);
			return;
		}
		//
		// JAXBContext jaxbContext;
		// try {
		//
		// jaxbContext = JAXBContext.newInstance(ListProgramFlow.class);
		//
		// Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
		// ruleset = (ListProgramFlow) jaxbUnmarshaller.unmarshal(fconfig);
		//
		// } catch (JAXBException e) {
		// // TODO Auto-generated catch block
		// e.printStackTrace();
		// }
	}

	@Override
	public void notifyNode(Node node, UpdateType type,
			Map<String, Property> propMap) {
		// TODO Auto-generated method stub
		EdgeResponder edgeSW = null;
		FTMiddeBoxes.TYPE mbType = null;
		String nodeId = node.getNodeIDString();
	    log.info("String nodeId ="+nodeId);
	    if (type == UpdateType.CHANGED) {
            log.info("String nodeId ="+nodeId);
        }
		if (type == UpdateType.ADDED) {
			log.info("Sw added ="+nodeId);
			FTUtil.RouterType routerType = FTUtil.getRouterType(nodeId);
			if (routerType == FTUtil.RouterType.Core) {
				if (!this.coreRouters.hasRouter(nodeId)) {
					log.info("CoreRouter Added " + nodeId);
					this.coreRouters.add(nodeId, new CoreRouter(node,
							this.switchManager, this.programmer, this.mbes));
                    /* PSI add CoreRouter */
					log.info("CoreRouter Size  = " + this.coreRouters.getSize());
                    PSIDAGModifier.getInstance().setCoreRouters(this.coreRouters);
                    PSIDAGModifier.getInstance().swBootStrap(nodeId);
				}
			} else if (routerType == FTUtil.RouterType.VirtualCore) {
				if (!this.coreRouters.hasRouter(nodeId)) {
					log.info("Virtual CoreRouter Added " + nodeId);
					this.coreRouters.add(nodeId, new VirtualCoreRouter(node,
							this.switchManager, this.programmer, this.mbes));
				}
			} else if (routerType == FTUtil.RouterType.HostAgent) {
				if (! this.edgeSW.containsKey(nodeId)) {
					log.info("HostAgent Added " + nodeId);
					mbType = this.mbes.getType(node.getNodeIDString());
					this.edgeSW.put(nodeId, new EdgeResponder(node,
							this.switchManager, this.programmer, mbType));
				}
			}

			/*
			 * if (node.getNodeIDString().equals("00:00:00:00:00:00:00:64")) {
			 * this.coreRouter = new CoreRouter(node, this.switchManager,
			 * this.programmer); } else { log.info("Edge:" +
			 * node.getNodeIDString()); mbType =
			 * this.mbes.getType(node.getNodeIDString());
			 * this.edgeSW.put(node.getNodeIDString(), new EdgeResponder(node,
			 * this.switchManager, this.programmer, mbType)); }
			 */

		} else if (type == UpdateType.REMOVED) {
			log.info("Sw removed");
			if(this.coreRouters.hasRouter(nodeId)){
				this.coreRouters.removeRouter(nodeId);
			} else if(this.edgeSW.containsKey(nodeId)){
				this.edgeSW.remove(nodeId);
			}

		}

	}

	@Override
	public void notifyNodeConnector(NodeConnector nodeConnector,
			UpdateType type, Map<String, Property> propMap) {
		// TODO Auto-generated method stub
        
        //log.info("nodeConnector.getNode().getNodeIDString()"+ nodeConnector.getNode().getNodeIDString());
		if(type == UpdateType.ADDED &&  this.edgeSW.containsKey(nodeConnector.getNode().getNodeIDString())){
			this.edgeSW.get(nodeConnector.getNode().getNodeIDString()).update(nodeConnector);
		}
        //Commented this line - uncommented the above 
        //this.edgeSW.get(nodeConnector.getNode().getNodeIDString()).update(nodeConnector);
		//

	}

	@Override
	public String echo(String in) {
		// TODO Auto-generated method stub
		return "Echo " + in + " from Class";
	}

	@Override
	public PacketResult receiveDataPacket(RawPacket inPkt) {
		Packet packet = this.dataPacketService.decodeDataPacket(inPkt);
		if (!(packet instanceof Ethernet)) {
			return PacketResult.IGNORED;
		}

		if (((Ethernet) packet).getEtherType() != 0x0800) {
			return PacketResult.IGNORED;
		}

		log.info(inPkt.getIncomingNodeConnector().getNode().getNodeIDString());
		EdgeResponder targetEdge = this.edgeSW.get(inPkt
				.getIncomingNodeConnector().getNode().getNodeIDString());
		IPv4 ipv4Hdr = (IPv4) ((Ethernet) packet).getPayload();
		int tos = ipv4Hdr.getDiffServ() * 4;
		int srcIP = ipv4Hdr.getSourceAddress();
		int dstIP = ipv4Hdr.getDestinationAddress();
		int srcPort = 0;
		int dstPort = 0;
		int nwProto = (int) ipv4Hdr.getProtocol();
		TCP tcpHdr = null;
		UDP udpHdr = null;
		switch (nwProto) {
		case 0x01:
			// ICMP
			break;
		case 0x06:
			// TCP
			tcpHdr = (TCP) ipv4Hdr.getPayload();
			srcPort = (int) tcpHdr.getSourcePort();
			dstPort = (int) tcpHdr.getDestinationPort();
			break;
		case 0x11:
			// UDP
			udpHdr = (UDP) ipv4Hdr.getPayload();
			srcPort = (int) udpHdr.getSourcePort();
			dstPort = (int) udpHdr.getDestinationPort();
			break;
		}
		int newTag = 0;
		FTFiveTuple orgTuple = new FTFiveTuple(srcIP, dstIP, srcPort, dstPort,
				nwProto);
		log.info(tos + ":" + orgTuple.toString());
		FTFiveTuple tagTuple;

		if (targetEdge.getMBType() == FTMiddeBoxes.TYPE.FT_NAT) {
			
			// Install NAT Genrerate Rule
			int mbid = this.mbes.getMBID(inPkt.getIncomingNodeConnector()
					.getNode().getNodeIDString());
			if (mbid == -1) {
				log.info("No mb is installed "
						+ inPkt.getIncomingNodeConnector().getNode()
								.getNodeIDString());
			}
			int state = 0;
			// Dirty hack for first Nat
			if (tos == 0 || tos == 0x04) {
				newTag = this.ftOutTags.getWithSrcIP(mbid, state, srcIP);
				log.info("New Tag via SrcIP " + newTag);
			} else {
				newTag = this.ftOutTags.get(mbid, state, tos);
				log.info("New Tag via PreTag " + newTag);
			}
		}

		log.info("FlowTags Tos:" + tos);
		if (tos == 0 || this.ftTags.contains(tos)) {
			// tos == 4 is requird for first chaning. Just dirty hack
			// We need to elimitate this dirty hack
			if (tos != 4 && tos != 0) {
				// Install Consume Rule
				tagTuple = this.ftTags.getFiveTuple(tos);
				FTFiveTuple nextTuple = orgTuple.getNextTuple(tagTuple);
				targetEdge.setConsumeRule(orgTuple, nextTuple, tos, newTag);
				try {
					log.info(nextTuple.toString());
					ipv4Hdr.setSourceAddress(InetAddress
							.getByAddress(BigInteger.valueOf(
									nextTuple.getNwSrc()).toByteArray()));
					ipv4Hdr.setDestinationAddress(InetAddress
							.getByAddress(BigInteger.valueOf(
									nextTuple.getNwDst()).toByteArray()));
					switch (nwProto) {
					case 0x06:
						// TCP
						tcpHdr.setSourcePort((short) nextTuple.getTpSrcPort());
						tcpHdr.setDestinationPort((short) nextTuple
								.getTpDstPort());
						break;
					case 0x11:
						// UDP
						udpHdr.setSourcePort((short) nextTuple.getTpSrcPort());
						udpHdr.setDestinationPort((short) nextTuple
								.getTpDstPort());
						break;
					}
					log.info("hoge");
				} catch (UnknownHostException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			} else if (targetEdge.getMBType() == FTMiddeBoxes.TYPE.FT_NAT) {
				// Dirty Hack;
				if(tos == 0x04){
					tos = 0x01;
				}
				targetEdge.setNatGenerateRule(orgTuple, tos, newTag);
			}
			if (newTag != -1) {
				ipv4Hdr.setDiffServ((byte) (newTag / 4));
			}
			RawPacket newPacket = this.dataPacketService
					.encodeDataPacket(packet);
			newPacket.setOutgoingNodeConnector(targetEdge.getInPort());
			this.dataPacketService.transmitDataPacket(newPacket);
		}
		return PacketResult.CONSUME;
	}
}
