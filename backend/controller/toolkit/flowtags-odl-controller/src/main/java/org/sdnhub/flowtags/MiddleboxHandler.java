package org.sdnhub.flowtags;

import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.nio.channels.spi.SelectorProvider;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

import org.opendaylight.controller.sal.core.Node;
//import org.openflow.protocol.factory.BasicFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MiddleboxHandler {
	// check protocol_plugin Controller.java nad ControllerIO.java
	// methods run at ControllerIO

	private static final Logger logger = LoggerFactory
			.getLogger(MiddleboxHandler.class);

	private Thread middleboxHandlerThread;
	private boolean running;

	private Selector selector;

	private final SocketChannel socket;
	// private final BasicFactory factory;
	private final AtomicInteger xid;

	private final String instanceName;

	private IMessageReadWrite msgReadWriteService;

	private PriorityBlockingQueue<PriorityMessage> transmitQ;
	private Thread transmitThread;

	/* Added by Toby for control message */
	private FTTags ftTags = null;
	private FTOutputTags ftOutTags = null;
	private FTMiddeBoxes mbes = null;
	private CoreRouters coreRouters = null;
	private HashMap<String, EdgeResponder> edgeSW = null;

	// End

	// public MiddleboxHandler(SocketChannel sc, String name) {
	/* Added by Toby for control message */
	public MiddleboxHandler(SocketChannel sc, String name, FTTags ftTags,
			FTOutputTags ftOutTags, CoreRouters coreRouters, HashMap<String, EdgeResponder> edgeSW, FTMiddeBoxes mbes) {
		this.socket = sc;
		this.instanceName = name;
		/* Added by Toby for control message */
		this.ftTags = ftTags;
		this.ftOutTags = ftOutTags;
		this.coreRouters = coreRouters;
		this.edgeSW = edgeSW;
		this.mbes = mbes;
		// End

		this.xid = new AtomicInteger(this.socket.hashCode());
		// this.factory = new BasicFactory();
	}

	public boolean isRunning() {
		return running;
	}

	private void startHandlerThread() {
		middleboxHandlerThread = new Thread(new Runnable() {
			@Override
			public void run() {
				running = true;
				while (running) {
					try {
						// wait for an incoming connection
						selector.select(0);
						logger.info("Incoming data");

						Iterator<SelectionKey> selectedKeys = selector
								.selectedKeys().iterator();
						while (selectedKeys.hasNext()) {
							SelectionKey skey = selectedKeys.next();
							selectedKeys.remove();
							if (skey.isValid() && skey.isWritable()) {
								logger.info("Resume send messages");
								resumeSend();
							}
							if (skey.isValid() && skey.isReadable()) {

								logger.info("Handling messages");

								handleMessages();
							}
						}
					} catch (Exception e) {
						reportError(e);
					}
				}
			}
		}, instanceName);
		middleboxHandlerThread.start();
	}

	public void handleMessages() {
		List<FTMessage> msgs = null;

		try {
			if (msgReadWriteService != null) {
				msgs = msgReadWriteService.readMessages();
			}
		} catch (Exception e) {
			// reportError(e);
			// LC: Close socket, middlebox connection error
			logger.info("Stopping " + instanceName);
			stop();
			return;
		}

		if (msgs == null) {
			return;
		}

		for (FTMessage msg : msgs) {
			// TODO: LC handle response

			try {

				FTMessage reply = null;
				// TODO: LC, add more modules, add logic using ODL msg platform


				// if its a field query
				if (msg.type == FTType.QUERY) {
					reply = new FTMessage(FTType.QUERY, msg.mbid, 0,
							FTTagfield.TOS.getInt());
				}

				if (msg.type == FTType.INTAG) {

					logger.info("INTAG mbid: {} state: {} tag: {}", msg.mbid,
							msg.state, msg.tag);
					FTFiveTuple ftFiveTuple = this.consumeTag(msg.tag);
					if (ftFiveTuple != null) {
						logger.info("INTAG " + ftFiveTuple.toString());
						reply = new FTMessage(FTType.INTAG, msg.mbid, 0, 1,
								this.ftTags.getFiveTuple(msg.tag));
					} else {
						logger.info("INTAG NULL");
						reply = new FTMessage(FTType.INTAG, msg.mbid, 0, 0);
					}
				}

				if (msg.type == FTType.OUTTAG) {

					logger.info("OUTTAG mbid: {} state: {} tag: {}", msg.mbid,
							msg.state, msg.tag);
					int newTag = this.generateTag(msg.mbid, msg.state, msg.tag);
					logger.info("OUTTAG newTag: {}", newTag);
					reply = new FTMessage(FTType.OUTTAG, msg.mbid, newTag, 1,
							FTProtocol.TCP, "", "", (short) 0, (short) 0);

				}

				/* Added by Toby for control message */
				if (msg.type == FTType.CONTROL) {
					FTControlMessage controlMessage = msg.getControlMessage();
					ArrayList<Integer> args = controlMessage.getArgs();
					CoreRouter router;
					System.out.println("Control Message:"
							+ controlMessage.toString());
					switch (controlMessage.getControlType()) {
					case FTControlMessage.TAG_ADD:
						int next = 5;
						if (args.size() == 7) {
							next = args.get(6);
						} 
						if (this.ftTags.add(
								args.get(0),
								new FTFiveTuple(args.get(1), args.get(2), args
										.get(3), args.get(4), args.get(5)),next)) {
							reply = new FTMessage(new FTControlMessage(
									"TAG ADD OK\n" + this.ftTags.toString()
											+ "\n\n"));
						} else {
							reply = new FTMessage(new FTControlMessage(
									"TAG ADD NG\n" + this.ftTags.toString()
											+ "\n\n"));
						}
						break;
					case FTControlMessage.TAG_DEL:
						if (this.ftTags.del(args.get(0))) {
							this.coreRouters.removeTagRoute(args.get(0));
							reply = new FTMessage(new FTControlMessage(
									"TAG DEL OK\n" + this.ftTags.toString()
											+ "\n\n"));
						} else {
							reply = new FTMessage(new FTControlMessage(
									"TAG DEL NG\n" + this.ftTags.toString()
											+ "\n\n"));
						}
						break;
					case FTControlMessage.TAG_DUMP:
						reply = new FTMessage(new FTControlMessage(
								"TAG DUMP OK\n" + this.ftTags.toString()
										+ "\n\n"));
						break;
					case FTControlMessage.TAG_CLEAR:
						this.ftTags.clear();
						this.coreRouters.removeAllTagRoutes();
						reply = new FTMessage(new FTControlMessage(
								"TAG CLEAR OK\n" + this.ftTags.toString()
										+ "\n\n"));
						break;
					case FTControlMessage.OUTTAG_ADD:
						if (this.ftOutTags.add(args.get(0), args.get(1),
								args.get(2), args.get(3),args.get(4))) {
							if(args.get(3) != 0){
								this.coreRouters.setTagRoute(args.get(3)/4, args.get(4));
							}
							reply = new FTMessage(new FTControlMessage(
									"OUTTAG ADD OK\n"
											+ this.ftOutTags.toString()
											+ "\n\n"));
						} else {
							reply = new FTMessage(new FTControlMessage(
									"OUTTAG ADD NG\n"
											+ this.ftOutTags.toString()
											+ "\n\n"));
						}
						break;
					case FTControlMessage.OUTTAG_DEL:
						if (this.ftOutTags.del(args.get(0), args.get(1),
								args.get(2))) {
							reply = new FTMessage(new FTControlMessage(
									"OUTTAG DEL OK\n"
											+ this.ftOutTags.toString()
											+ "\n\n"));
						} else {
							reply = new FTMessage(new FTControlMessage(
									"OUTTAG DEL NG\n"
											+ this.ftOutTags.toString()
											+ "\n\n"));
						}
						break;
					case FTControlMessage.OUTTAG_DUMP:
						reply = new FTMessage(new FTControlMessage(
								"OUTTAG DUMP OK\n" + this.ftOutTags.toString()
										+ "\n\n"));
						break;
					case FTControlMessage.OUTTAG_CLEAR:
						this.ftOutTags.clear();
						reply = new FTMessage(new FTControlMessage(
								"OUTTAG CLEAR OK\n" + this.ftOutTags.toString()
										+ "\n\n"));
						break;
					case FTControlMessage.MB_ADD:
						if(this.mbes.add(args.get(0), args.get(1), args.get(2),args.get(3),args.get(4))){
							int mbid = args.get(0);
							// Requrired for the mininet, host <-> controller
							if(this.edgeSW.get(this.mbes.getSWID(mbid)) != null){
								this.edgeSW.get(this.mbes.getSWID(mbid)).setMBType(this.mbes.getType(mbid));
							}
							
							if(args.get(3) != 0){
								this.coreRouters.setIpRoute(args.get(1), args.get(3),args.get(4));
							}
						reply = new FTMessage(new FTControlMessage(
								"MB ADD OK\n" + this.mbes.toString()
										+ "\n\n"));
						} else{
						reply = new FTMessage(new FTControlMessage(
								"MB ADD NG\n" + this.mbes.toString()
										+ "\n\n"));
						}
						break;
					case FTControlMessage.MB_DEL:
						if(this.mbes.del(args.get(0))){
							int mbid = args.get(0);
							
							// Requrired for the mininet, host <-> controller
							if(this.edgeSW.get(this.mbes.getSWID(mbid)) != null){
								this.edgeSW.get(this.mbes.getSWID(mbid)).setMBType(null);
							}
						reply = new FTMessage(new FTControlMessage(
								"MB DEL OK\n" + this.mbes.toString()
										+ "\n\n"));
						} else{
						reply = new FTMessage(new FTControlMessage(
								"MB DEL NG\n" + this.mbes.toString()
										+ "\n\n"));
						}
						break;
					case FTControlMessage.MB_DUMP:
						reply = new FTMessage(new FTControlMessage(
								"MB DUMP OK\n" + this.mbes.toString()
										+ "\n\n"));
						break;
					case FTControlMessage.MB_CLEAR:
						for(EdgeResponder edgeSW: this.edgeSW.values()){
							edgeSW.setMBType(null);
							edgeSW.removeAllTagRoutes();
						}
						this.mbes.clear();
						reply = new FTMessage(new FTControlMessage(
								"MB CLEAR OK\n" + this.mbes.toString()
										+ "\n\n"));
						break;
					case FTControlMessage.FW_ADD:
						long swID = (((long)args.get(0)) << 32) + (long)args.get(1);
						int hostID = args.get(2);
						int connectorID = args.get(3);
						router = this.coreRouters.getRouter(swID);
						if(router != null){
							System.out.println("0");
							router.addForwardingTable(hostID, connectorID);
							System.out.println("1");
							reply = new FTMessage(new FTControlMessage(
								"FW ADD OK\n" + router.dumpForwardingTable() 
										+ "\n\n"));
							System.out.println("2");
						} else {
							System.out.println("3");
							reply = new FTMessage(new FTControlMessage(
								"FW ADD NG\nNo Router " + args.get(0) + "\n\n"));
							System.out.println("4");
						}
						break;
					case FTControlMessage.FW_DEL:
						swID = (((long)args.get(0)) << 32) + (long)args.get(1);
						hostID = args.get(2);
						router = this.coreRouters.getRouter(swID);
						if(router != null){
							router.delForwardingTable(hostID);
							reply = new FTMessage(new FTControlMessage(
								"FW DEL OK\n" + router.dumpForwardingTable() 
										+ "\n\n"));
						} else {
							reply = new FTMessage(new FTControlMessage(
								"FW DEL NG\nNo Router " + args.get(0) + "\n\n"));
						}
						break;
					case FTControlMessage.FW_DUMP:
						reply = new FTMessage(new FTControlMessage(
								"FW DUMP OK\n" + this.coreRouters.dumpForwardingTable()+"\n\n"));
						break;
					case FTControlMessage.FW_CLEAR:
						this.coreRouters.clearForwardingTable();
						reply = new FTMessage(new FTControlMessage(
								"FW CLEAR OK\n" + this.coreRouters.dumpForwardingTable()+"\n\n"));
						break;
					case FTControlMessage.TEST_ADD:
						int tag = args.get(0);
						int testGeneratorId = args.get(1);
						int hostId = args.get(2);
						int senderSrcIP = args.get(3);
						int generatorSrcIP = args.get(4);
						System.out.println("Tag:" + tag + " hostID:" + hostId);
						this.coreRouters.setTagRouteWithLocation(tag/4, hostId);
						System.out.println("Tag:" + (tag/4+1) + " testGeneratorId:" + testGeneratorId);
						this.coreRouters.setTagRouteWithLocation(tag/4+1, testGeneratorId);
						this.edgeSW.get(FTUtil.getSWID(hostId)).setTestSenderRule(tag, tag+4,senderSrcIP,generatorSrcIP);
						reply = new FTMessage(new FTControlMessage("Test ADD Success\n\n"));
						break;
					case FTControlMessage.TAG_GENERATE:
						hostId = args.get(0);
						tag = args.get(1);
						int srcIP = args.get(2);
						int dstIP = args.get(3);
						int srcPort = args.get(4);
						int dstPort = args.get(5);
						int proto   = args.get(6);
						if(this.edgeSW.containsKey(FTUtil.getSWID(hostId))){
							this.edgeSW.get(FTUtil.getSWID(hostId)).setExplicitTagGenerate(srcIP, dstIP,srcPort, dstPort, proto,tag);
						}
						reply = new FTMessage(new FTControlMessage("Explicit Tag Generate Success\n\n"));
						break;
					case FTControlMessage.TAG_GENERATE_DEL:
						hostId = args.get(0);
						tag = args.get(1);
						srcIP = args.get(2);
						dstIP = args.get(3);
						System.out.println("DEL ID:"+hostId);
						if(this.edgeSW.containsKey(FTUtil.getSWID(hostId))){
							System.out.println("Try to del");
							this.edgeSW.get(FTUtil.getSWID(hostId)).delExplicitTagGenerate(srcIP, dstIP);
						}
						reply = new FTMessage(new FTControlMessage("Explicit Tag Del Success\n\n"));
						break;
					case FTControlMessage.TAG_GENERATE_CLEAR:
						hostId = args.get(0);
						if(this.edgeSW.containsKey(FTUtil.getSWID(hostId))){
							this.edgeSW.get(FTUtil.getSWID(hostId)).clearExplicitTagGenerate();
						}
						reply = new FTMessage(new FTControlMessage("Explicit Tag Clear Success\n\n"));
						break;
					}
				}
				// End

				if (reply != null) {
					msgReadWriteService.asyncSend(reply);
				}

			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}
	}

	private int generateTag(int mid, int state, int preTag) {
		int newTag = -1;
		newTag = this.ftOutTags.get(mid, state, preTag);
		if(newTag == -1 ){
			newTag = this.ftOutTags.getWithSrcIP(mid, state, preTag);
		}
		
		// Toby: We need to write some error handler
		// We cannot find good tag for generation
		if(newTag == -1){
			newTag = 0;
		}
		
		
		// Toby: This is dirty hack for the evaluation of DDoS Project
		// I'll add mux logic here.
		if( mid == 61 && state == 1){
			// Attack
			newTag = 40;
		} else if ( mid == 61 && state == 0){
			// Non Attack
			newTag = 20;
		}
		return newTag;
	}

	private FTFiveTuple consumeTag(int tag) {
		return this.ftTags.getFiveTuple(tag);
	}

	private void stopInternal() {
		// logger.debug("{} receives stop signal",
		// (isOperational() ? HexString.toHexString(sid) : "unknown"));
		running = false;
		// cancelSwitchTimer();
		try {
			selector.wakeup();
			selector.close();
		} catch (Exception e) {
		}
		try {
			socket.close();
		} catch (Exception e) {
		}
		try {
			msgReadWriteService.stop();
		} catch (Exception e) {
		}
		logger.debug("executor shutdown now");
		// executor.shutdownNow();

		msgReadWriteService = null;
	}

	// private void cancelSwitchTimer() {
	// if (this.periodicTimer != null) {
	// this.periodicTimer.cancel();
	// }
	// }

	public void start() {
		try {
			startTransmitThread();
			setupCommChannel();
			// sendFirstHello();
			startHandlerThread();
		} catch (Exception e) {
			reportError(e);
		}
	}

	/*
	 * Setup communication services
	 */
	private void setupCommChannel() throws Exception {
		this.selector = SelectorProvider.provider().openSelector();
		this.socket.configureBlocking(false);
		this.socket.socket().setTcpNoDelay(true);
		this.msgReadWriteService = getMessageReadWriteService();
	}

	public void stop() {
		stopInternal();

		if (middleboxHandlerThread != null) {
			middleboxHandlerThread.interrupt();
		}
		if (transmitThread != null) {
			transmitThread.interrupt();
		}
	}

	public void resumeSend() {
		try {
			if (msgReadWriteService != null) {
				msgReadWriteService.resumeSend();
			}
		} catch (Exception e) {
			reportError(e);
		}
	}

	class PriorityMessageTransmit implements Runnable {
		@Override
		public void run() {
			running = true;
			while (running) {
				try {
					PriorityMessage pmsg = transmitQ.take();
					msgReadWriteService.asyncSend(pmsg.msg);
					/*
					 * If syncReply is set to true, wait for the response back.
					 */
					if (pmsg.syncReply) {
						// syncMessageInternal(pmsg.msg, pmsg.msg.getXid(),
						// false);
					}
				} catch (InterruptedException ie) {
					reportError(new InterruptedException(
							"PriorityMessageTransmit thread interrupted"));
				} catch (Exception e) {
					reportError(e);
				}
			}
			transmitQ = null;
		}
	}

	private IMessageReadWrite getMessageReadWriteService() throws Exception {
		// String str = System.getProperty("secureChannelEnabled");
		// return ((str != null) && (str.trim().equalsIgnoreCase("true"))) ? new
		// SecureMessageReadWriteService(socket,
		// selector) : new MessageReadWriteService(socket, selector);

		// LC: no secure socket

		return new MessageReadWriteService(socket, selector);
	}

	/*
	 * Setup and start the transmit thread
	 */
	private void startTransmitThread() {
		this.transmitQ = new PriorityBlockingQueue<PriorityMessage>(11,
				new Comparator<PriorityMessage>() {
					@Override
					public int compare(PriorityMessage p1, PriorityMessage p2) {
						if (p2.priority != p1.priority) {
							return p2.priority - p1.priority;
						} else {
							return (p2.seqNum < p1.seqNum) ? 1 : -1;
						}
					}
				});
		this.transmitThread = new Thread(new PriorityMessageTransmit());
		this.transmitThread.start();
	}

	private void reportError(Exception e) {
		// if (!running) {
		// logger.debug("Caught exception {} while switch {} is shutting down. Skip",
		// e.getMessage(),
		// (isOperational() ? HexString.toHexString(sid) : "unknown"));
		// return;
		// }
		logger.debug("Caught exception: ", e);

		// notify core of this error event and disconnect the switch
		// ((Controller) core).takeSwitchEventError(this);

		// clean up some internal states immediately
		stopInternal();
	}
}
