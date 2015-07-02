package org.sdnhub.flowtags;

import java.lang.*;
import java.io.*;
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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.opendaylight.controller.sal.core.Node;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PSIEventHandler {
	// check protocol_plugin Controller.java nad ControllerIO.java
	// methods run at ControllerIO

	private static final Logger logger = LoggerFactory
			.getLogger(PSIEventHandler.class);

	private Thread middleboxHandlerThread;
	private boolean running;

	private Selector selector;

	private final SocketChannel socket;
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

	public PSIEventHandler(SocketChannel sc) {
		this.socket = sc;
		this.xid = new AtomicInteger(this.socket.hashCode());
		this.instanceName = "abc";
        PSIDAGModifier.getInstance().setSocket(sc);
	}

	public PSIEventHandler(SocketChannel sc, String name, FTTags ftTags,
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
                /* Measurement */
                long trans_num = 0;
                long s_time = 0;
                long a_time = 0;
                long b_time = 0;
                double sum_time = 0;
                while (running) {
                    try {
                        // wait for an incoming connection
                        selector.select(0);
                        logger.info("Incoming data");

                        Iterator<SelectionKey> selectedKeys = selector.selectedKeys().iterator();
                        while (selectedKeys.hasNext()) {
                            SelectionKey skey = selectedKeys.next();
                            selectedKeys.remove();
                            if (skey.isValid() && skey.isWritable()) {
                                logger.info("Resume send messages");
                                resumeSend();
                            }
                            if (skey.isValid() && skey.isReadable()) {

                                logger.info("Handling messages");
                                trans_num += 1;
                                // if (trans_num == 1) s_time = System.currentTimeMillis();
                                // a_time = System.currentTimeMillis();
                                // a_time = System.nanoTime();
                                handleMessages();
                                // b_time = System.currentTimeMillis();
                                // b_time = System.nanoTime();
                                // sum_time += (b_time-a_time);
                                // logger.info("Latency ="+(b_time-a_time));
                                // logger.info("Ave Latency = "+(sum_time/trans_num));
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

    public long trans_num = 0;
    public long s_time = 0;
    public long c_time = 0;
	public void handleMessages() {
		List<PSIMessage> msgs = null;

		try {
			if (msgReadWriteService != null) {
				msgs = msgReadWriteService.readPSIMessages();
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

        //System.out.print("msgs size ="+msgs.size());

        // PSI  Message Handling
        for (PSIMessage msg : msgs){
            // String alertmsg = msg.getAlertMsg();
            // logger.info("PSIEventHandler alertmsg:");
            /*
            // TODO debug alert string
            logger.info("PSIEventHandler alertmsg:"+alertmsg);

            // Parse alert msg

            Pattern psrc = Pattern.compile("src(.+?)");
            Matcher msrc = psrc.matcher(alertmsg);
            Pattern pdst = Pattern.compile("dst(.+?)");
            Matcher mdst = pdst.matcher(alertmsg);
            Pattern pe = Pattern.compile("event(.+?)");
            Matcher me = pe.matcher(alertmsg);

            logger.info("PSIPolicyManager.getInstance().handleEvent(new PSIEvent("+msrc.group()+mdst.group()+me.group());

            PSIPolicyManager.getInstance().handleEvent(new PSIEvent(me.group(), msrc.group(), mdst.group(), me.group()));
            */
            List<String> alertmsglist = new ArrayList<String>();
            alertmsglist = msg.getAlertMsgList();
            // System.out.print("msglist size = "+alertmsglist.size());
            // System.out.print("msglist 0 = "+alertmsglist.get(0));
            for (int i = 0; i<alertmsglist.size(); i++)
            {
                trans_num += 1;
                if (trans_num == 1) s_time = System.currentTimeMillis();
                c_time = System.currentTimeMillis();

                String alertmsg = alertmsglist.get(i);
                // alertmsg = "src< 168 >dst< 192 >event< Event1 >";

                // String[] msgarray = alertmsg.split("DDD");
                Pattern psrc = Pattern.compile("src< (.*?) >");
                Matcher msrc = psrc.matcher(alertmsg);
                /*
                if (msrc.find())
                {
                    System.out.print("msrcEvent(new PSIEvent("+msrc.group(1));
                }
                */
                Pattern pdst = Pattern.compile("dst< (.*?) >");
                Matcher mdst = pdst.matcher(alertmsg);
                Pattern pe = Pattern.compile("event< (.*?) >");
                Matcher me = pe.matcher(alertmsg);
                // System.out.print("alertmsg = "+ alertmsg+ alertmsg.length()+ "\n");
                // if ((alertmsg.length() >= 40)&&(alertmsg.length() <= 50))
                // if ((alertmsg.length() >= 45)&&(alertmsg.length() <= 55))
                if (msrc.find() && mdst.find() && me.find())
                {
                    // System.out.print("PSIPolicyManager.getInstance().handleEvent(new PSIEvent("+msgarray[1]+msgarray[2]+msgarray[3]);
                    // System.out.print("PSIPolicyManager.getInstance().handleEvent(new PSIEvent("+me.group(1)+msrc.group(1)+mdst.group(1));
                    // PSIPolicyManager.getInstance().handleEvent(new PSIEvent(msgarray[3], msgarray[1], msgarray[2], msgarray[3]));
                    PSIPolicyManager.getInstance().handleEvent(new PSIEvent(me.group(1), msrc.group(1), mdst.group(1), me.group()));
                    // PSIPolicyManager.getInstance().handleEvent(new PSIEvent("Event1", "192.168.123.1", "192.168.123.2", "Event1"));
                }
                else{
                    PSIPolicyManager.getInstance().handleEvent(new PSIEvent("Event1", "192.168.123.1", "192.168.123.2", "Event1"));
                }

                // PSIPolicyManager.getInstance().handleEvent(new PSIEvent("Event1", "192.168.123.1", "192.168.123.2", "Event1"));

                if ((trans_num%50000)==1)
                {
                    System.out.print("Receive rate ="+(((float)trans_num)*1000/(c_time-s_time))+"\n");
                }
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
