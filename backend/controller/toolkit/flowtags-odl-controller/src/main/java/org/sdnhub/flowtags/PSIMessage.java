package org.sdnhub.flowtags;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PSIMessage {

	protected static final Logger log = LoggerFactory
			.getLogger(PSIMessage.class);

	/* Added by Toby for control message */
	private static Charset charset = StandardCharsets.US_ASCII;
	public static CharsetEncoder encoder = charset.newEncoder();
	private static final int bufferSize = 1024 * 1024;

	public static int REQUIRED_SIZE = 16;
	// public static int REQUIRED_SIZE = 8;

	FTType type;
	int mbid;
	int tag;
	int state;

	FTProtocol protocol;
	int srcIP; // max 16 bytes
	int dstIP; // max 16 bytes

	short srcPT;
	short dstPT;

	private FTControlMessage controlMessage = null;
    
    // PSI
    private String alertMsg;
    private List<String> alertMsgList = new ArrayList<String>();

	public PSIMessage() {
	}

	public PSIMessage(FTType type, int mbid, int tag, int state) {

		this.type = type;
		this.mbid = mbid;
		this.tag = tag;
		this.state = state;

		this.protocol = null;
		this.srcIP = 0;
		this.dstIP = 0;
		this.srcPT = 0;
		this.dstPT = 0;
	}

	public PSIMessage(FTType type, int mbid, int tag, int state, FTFiveTuple ftValue) {
		this.type = type;
		this.mbid = mbid;
		this.tag = tag;
		this.state = state;
		this.srcIP = ftValue.getNwSrc();
		this.dstIP = ftValue.getNwDst();
		this.srcPT = (short) ftValue.getTpSrcPort();
		this.dstPT = (short) ftValue.getTpDstPort();
		switch (ftValue.getNwProto()) {
		case 0x06:
			this.protocol = FTProtocol.TCP;
			break;
		case 0x11:
			this.protocol = FTProtocol.UDP;
			break;
		}
	}

	public PSIMessage(FTType type, int mbid, int tag, int state,
			FTProtocol protocol, String srcIP, String dstIP, short srcPt,
			short dstPt) {

		this.type = type;
		this.mbid = mbid;
		this.tag = tag;
		this.state = state;
		this.protocol = protocol;

		try {
			InetAddress bar;
			int value;

			bar = InetAddress.getByName(srcIP);
			value = ByteBuffer.wrap(bar.getAddress()).getInt();
			this.srcIP = value;

			bar = InetAddress.getByName(dstIP);
			value = ByteBuffer.wrap(bar.getAddress()).getInt();
			this.dstIP = value;

		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		this.srcPT = srcPt;
		this.dstPT = dstPt;
	}

	public PSIMessage(FTControlMessage controlMessage) {
		this.type = FTType.CONTROL_REPLY;
		this.state = 9;
		this.controlMessage = controlMessage;
	}

	public FTControlMessage getControlMessage() {
		return this.controlMessage;
	}

	public void writeTo(ByteBuffer outBuffer) {
		// TODO Auto-generated method stub

		outBuffer.putInt(this.type.getInt());
		outBuffer.putInt(this.mbid);
		outBuffer.putInt(this.tag);
		outBuffer.putInt(this.state);
		

		// LC, only INTAG is needed, the out tag should only return the tag
		// value
		// the end switch should replace with the proper values

		if (this.type == FTType.INTAG) {

			if (this.protocol != null) {
				outBuffer.put(this.protocol.getByte());
			} else {
				outBuffer.put((byte) 0);
			}

			outBuffer.putShort(this.srcPT);
			outBuffer.putShort(this.dstPT);

			outBuffer.putInt(this.srcIP);
			outBuffer.putInt(this.dstIP);
		}

		/* Added by Toby for control message */
		if (this.type == FTType.CONTROL_REPLY) {
			ByteBuffer bb = ByteBuffer.wrap(this.controlMessage.getReply()
					.getBytes());
			bb.order(ByteOrder.BIG_ENDIAN);
			outBuffer.put(bb);
		}
		
		//Added by Toby
		outBuffer.put((byte)0x0a);
		outBuffer.put((byte)0x0a);

	}

	public void readFrom(ByteBuffer inBuffer) {
		log.info("PSI Message readFrom");
        // String v = new String(inBuffer.array());
        // System.out.println("PSI alert String:" + v);	
		// alertMsg = inBuffer.toString();
		alertMsg = Charset.forName("UTF-8").decode(inBuffer).toString();
        // log.info("PSI alert string: {}", inBuffer.toString());
        // log.info("PSI alert v: {}", Charset.forName("UTF-8").decode(inBuffer));
        
        // System.out.print("               alertMsg == "+alertMsg);
        // System.out.print("alertMsg = "+ alertMsg);

        //alertMsgList.add(alertMsg);
        
        String[] splited = alertMsg.split("MSG");
        for (String s: splited){
            alertMsgList.add(s);
        }
        
	}

    public List<String> getAlertMsgList()
    {
        return alertMsgList;
    }

    public String getAlertMsg()
    {
        return alertMsg;
    }

	public int getLengthU() {
		// TODO Auto-generated method stub
		return 0;
	}

}
