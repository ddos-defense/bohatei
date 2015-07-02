package org.sdnhub.flowtags;

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

public class FTMessage {

	protected static final Logger log = LoggerFactory
			.getLogger(FTMessage.class);

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

	public FTMessage() {
	}

	public FTMessage(FTType type, int mbid, int tag, int state) {

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

	public FTMessage(FTType type, int mbid, int tag, int state, FTFiveTuple ftValue) {
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

	public FTMessage(FTType type, int mbid, int tag, int state,
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

	public FTMessage(FTControlMessage controlMessage) {
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

		type = FTType.getType(inBuffer.getInt()); // +4
		mbid = inBuffer.getInt(); // +4
		tag = inBuffer.getInt(); // +4
		state = inBuffer.getInt(); // +4

		log.info("Incoming type: {} mbid: {} tag: {} state: {} ", type, mbid,
				tag, state);
		System.out.println("Incoming type:" + type + "mbid:" + mbid + " tag:"
				+ tag + " state:" + state);

		/* Added by Toby for control message */
		if (this.type == FTType.CONTROL && inBuffer.remaining() > 0) {
			int controlType = inBuffer.getInt();
			ArrayList<Integer> message = new ArrayList<Integer>();
			while (inBuffer.remaining() > 0) {
				message.add(new Integer(inBuffer.getInt()));
			}
			try {
				this.controlMessage = new FTControlMessage(controlType, message);
			} catch (IllegalArgumentException e) {
				e.printStackTrace();
			}
		}

		// End

		// if the packet is INTAG we need to detect the direction of the
		// response/request
		// if there is a request, then we dont read anything after the header
		// if there is a reply, we read the packet headers.. to figure out if
		// there is a request or reply we check
		// the incoming data length

		/*
		 * LC, Temporal disabled.... if(this.type == FTType.INTAG || (this.type
		 * == FTType.INTAG && inBuffer.remaining() > 12)){
		 * 
		 * protocol = FTProtocol.getType( inBuffer.get() ); // + 1
		 * 
		 * srcIP = inBuffer.getInt(); // + 4 dstIP = inBuffer.getInt(); // + 4
		 * 
		 * srcPT = inBuffer.getShort(); // + 2 dstPT = inBuffer.getShort(); // +
		 * 2
		 * 
		 * log.info("{} {} {} {} {} {}", type, protocol, srcIP, dstIP, srcPT,
		 * dstPT); }
		 */

	}

	public int getLengthU() {
		// TODO Auto-generated method stub
		return 0;
	}

}
