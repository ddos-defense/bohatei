package net.dougharris.dns;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.StringTokenizer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.dougharris.utility.PacketInputStream;

public class RFC1035 {
	protected static final Logger log = LoggerFactory
			.getLogger(RFC1035.class);

	static protected HashMap<Integer, String> typeNames = new HashMap<Integer, String>();
	
	// http://en.wikipedia.org/wiki/List_of_DNS_record_types
	
	static {
		typeNames.put(new Integer(1), "A");
		typeNames.put(new Integer(2), "NS");
		typeNames.put(new Integer(5), "CNAME");
		typeNames.put(new Integer(6), "SOA");
		typeNames.put(new Integer(11), "WKS");
		typeNames.put(new Integer(12), "PTR");
		typeNames.put(new Integer(13), "HINFO");
		typeNames.put(new Integer(15), "MX");
		typeNames.put(new Integer(16), "TXT");
	}
	
	private PacketInputStream inputStream = null;
	
	public ResourceRecord[] ansArray;
	public ResourceRecord[] autArray;
	public ResourceRecord[] addArray;
	
	StringBuffer reportBuffer = new StringBuffer();
	short id;
	public boolean qr;
	int opcode;
	boolean aa;
	boolean tc;
	boolean rd;
	boolean ra;
	int rcode;
	short flags;
	short queryCount;
	short answerCount;
	short authorityCount;
	short additionalCount;
	
	public RFC1035(byte[] b) {
		this(b, 0, b.length);
	}

	public RFC1035(PacketInputStream i) {
		this.inputStream = i;
	}

	public RFC1035(byte[] b, int offset, int length) {
		try {
			this.inputStream = new PacketInputStream(b, offset, length);
		} catch (Exception x) {
			System.exit(2);
		}
	}

	public static byte[] createQueryPacket(String name, String type,
			boolean recurse) throws IOException {
		int intType;
		try {
			intType = Integer.parseInt(type);
		} catch (NumberFormatException x) {
			intType = RFC1035.getTypeNumber(type);
			if (intType == -1) {
				System.err.println("No type " + type);
				System.exit(2);
			}
		}
		return createQueryPacket(intType, name, recurse);
	}
	
	public short getCountAnswer(){
		
		return answerCount;
	}
	
	public static byte[] createQueryPacket(int type, String name,
			boolean recurse) throws IOException {
		ByteArrayOutputStream ob = new ByteArrayOutputStream();
		DataOutputStream o = new DataOutputStream(ob);
		String n;
		o.writeShort(1); // Id
		if (recurse) {
			o.writeShort(256); // Recursion Desired
		} else {
			o.writeShort(0); // No Recursion Desired
		}
		o.writeShort(1); // Questions
		o.writeShort(0); // Answers
		o.writeShort(0); // Authority
		o.writeShort(0); // Additional
		StringTokenizer k = new StringTokenizer(name, ".");
		while (k.hasMoreTokens()) {
			n = k.nextToken();
			o.writeByte(n.length());
			for (int j = 0; j < n.length(); j++) {
				o.writeByte(n.charAt(j));
			}
		}
		o.writeByte(0); // Root name
		o.writeShort(type);
		o.writeShort(1); // class IN
		for (int j = o.size(); j < 512; j++) {
			o.writeByte(0);
		}
		o.close();
		return ob.toByteArray();
	}

	/**
	 * definitely must be instance, since it sets instance variables
	 */
	public RFC1035 parse() throws Exception {
		ArrayList<ResourceRecord> recordList = new ArrayList<ResourceRecord>();

		id = inputStream.readShort();
		flags = inputStream.readShort();
		queryCount = inputStream.readShort();
		answerCount = inputStream.readShort();
		authorityCount = inputStream.readShort();
		additionalCount = inputStream.readShort();
		
		populate_flags();
		
		for (int j = 0; j < queryCount; j++) {
			String name = decompressRFC1035Name(inputStream);   // name
			log.info("name query {}", name);
			
			int n = inputStream.readUnsignedShort(); 		    // type
			//reportBuffer.append("Query:" + j + " for type " + n + "\n");
			n = inputStream.readUnsignedShort();                // class
		}
		
		log.info("query {} answer {} aut {} add {}", queryCount, answerCount,authorityCount,additionalCount);
		
		
		ResourceRecord rr;
		//reportBuffer.append(answerCount + " answer records--\n");
		for (int j = 0; j < answerCount; j++) {
			rr = readResourceRecord(inputStream);
			
			if (rr != null){
				recordList.add(rr);
			}
		}
		recordList.toArray(ansArray = new ResourceRecord[recordList.size()]);
		recordList.clear();

		//reportBuffer.append(authorityCount + " authority records--\n");
		for (int j = 0; j < authorityCount; j++) {
			rr = readResourceRecord(inputStream);
			if (rr != null){
				recordList.add(rr);
			}
		}
		recordList.toArray(autArray = new ResourceRecord[recordList.size()]);
		recordList.clear();

		//reportBuffer.append(additionalCount + " additional records--\n");
		for (int j = 0; j < additionalCount; j++) {
			rr = readResourceRecord(inputStream);
			if (rr != null){
				recordList.add(rr);
			}
		}
		recordList.toArray(addArray = new ResourceRecord[recordList.size()]);
		recordList.clear();
		return this;
	}

	private final static String[] classNames = new String[] { "", "IN" };

	public static String getRclassString(int cls) {
		return classNames[cls];
	}

	public static String getTypeString(int type) {
		return (String) typeNames.get(new Integer(type));
	}

	public static ResourceRecord getTypeInstance(int type) {
		String className = "net.dougharris.dns." + getTypeString(type)
				+ "ResourceRecord";
		ResourceRecord instance = null;
		try {
			instance = (ResourceRecord) Class.forName(className).newInstance();
		} catch (ClassNotFoundException x) {
			System.err.println(x.getClass().getName() + ":" + x.getMessage());// JDH
		} catch (InstantiationException x) {
			System.err.println(x.getClass().getName() + ":" + x.getMessage());// JDH
		} catch (IllegalAccessException x) {
			System.err.println(x.getClass().getName() + ":" + x.getMessage());// JDH
		}
		return instance;
	}

	public static int getTypeNumber(String typeName) {
		int reply = -1;
		typeName = typeName.toUpperCase();
		Map.Entry entry;
		String entryName;
		Integer entryNumber;
		Iterator j = typeNames.entrySet().iterator();
		while (j.hasNext()) {
			entry = (Map.Entry) j.next();
			entryName = (String) (entry.getValue());
			entryNumber = (Integer) (entry.getKey());
			if (entryName.equals(typeName)) {
				reply = entryNumber.intValue();
			}
		}
		return reply;
	}

	ResourceRecord readResourceRecord(PacketInputStream i) throws IOException {
		String name = decompressRFC1035Name(i);
		ResourceRecord result = null;
		
		if (name == null) {
			log.error("readResourceRecord: bad decompression of name");// JDHEXITFIX
		}else{
			
			int type = i.readUnsignedShort();
			int rClass = i.readUnsignedShort();
			int ttl = i.readInt();
			int length = i.readUnsignedShort();
			
			if (typeNames.containsKey(type) == true){
				
				result = getTypeInstance(type);
				
				if (result == null) {
					log.error("bad type {} for {} ", type, name);// JDH
					
					return null;
					
				}else{
					result.setName(name);
					result.setType(type);
					result.setRclass(rClass);
					result.setTTL(ttl);
					result.setLength(length);
					/*
					 * Now switch on the type number and fill the packets
					 */
					// byte[] b = readData(length);
					// result.setData(b);
					switch (type) {
					case 2: /* NS */
						((NSResourceRecord) result).setNSName(decompressRFC1035Name(i));
						break;
			
					case 5: /* CNAME */
						((CNAMEResourceRecord) result).setCNAME(decompressRFC1035Name(i));
						break;
			
					case 6: /* SOA */
						SOAResourceRecord rr = (SOAResourceRecord) result;
						rr.setMname(decompressRFC1035Name(i));
						rr.setRname(decompressRFC1035Name(i));
						rr.setSerial(i.readUnsignedInt());
						rr.setRefresh(i.readUnsignedInt());
						rr.setRetry(i.readUnsignedInt());
						rr.setExpire(i.readUnsignedInt());
						rr.setMinimum(i.readUnsignedInt());
						break;
			
					case 12: /* PTR */
						((PTRResourceRecord) result).setPTRName(decompressRFC1035Name(i));
						break;
			
					case 15: /* MX */
						((MXResourceRecord) result).setPreference(i.readUnsignedShort());
						((MXResourceRecord) result).setExchanger(decompressRFC1035Name(i));
						break;
			
					case 1: /* A */
					default:
						byte[] b = readData(i, length);
						result.setData(b);
						break;
					}
				}
			}
		}
		
		return result;
	}

	public byte[] readData(PacketInputStream i, int n) throws IOException {
		byte[] data = new byte[n];
		i.readFully(data);
		return data;
	}

	private String decompressRFC1035Name(PacketInputStream i) throws IOException {
		String name = new String();
		int n = 0;
		while (true) {
			try {
				n = i.readUnsignedByte();
			} catch (Exception x) {
				break;
			}
			if (n == 0) {
				break;
			}
			if (n < 64) {
				name += plainSegment(i, n);
				continue;
			} else if (n > 191) {
				n = ((n & 63) << 8) + i.readUnsignedByte();
				name += compressedSegment(i, n);
				break;
			} else {
				throw new IOException("bad encoding of name");
			}
		}
		return name;
	}

	String plainSegment(PacketInputStream i, int n) throws IOException {
		byte[] b = new byte[n];
		i.readFully(b);
		return new String(b, "ASCII7") + ".";
	}

	String compressedSegment(PacketInputStream i, int n) throws IOException {
		String s = "";
		long where = i.getPosition();
		i.setPosition(n);
		s = decompressRFC1035Name(i);
		i.setPosition(where);
		return s;
	}

	void readQuery(PacketInputStream i) throws IOException, Exception {
		String s = decompressRFC1035Name(i);
		int n;
		n = i.readUnsignedShort();// type
		n = i.readUnsignedShort();// class
	}

	public String showFlags(short flags) {
		StringBuffer b = new StringBuffer();
		qr = (0 == (flags & (short) 0x8000) >> 15);
		opcode = (flags & (short) 0x7800) >> 11;
		aa = (0 != ((flags & (short) 0x0400) >> 10));
		tc = (0 != ((flags & (short) 0x0200) >> 9));
		rd = (0 != ((flags & (short) 0x0100) >> 8));
		ra = (0 != ((flags & (short) 0x0080) >> 7));
		rcode = (flags & (short) 0x000f);
		return b.toString();
	}

	public void populate_flags() { 
		qr = (0 == (flags & (short) 0x8000) >> 15);
		opcode = (flags & (short) 0x7800) >> 11;
		aa = (0 != ((flags & (short) 0x0400) >> 10));
		tc = (0 != ((flags & (short) 0x0200) >> 9));
		rd = (0 != ((flags & (short) 0x0100) >> 8));
		ra = (0 != ((flags & (short) 0x0080) >> 7));
		rcode = (flags & (short) 0x000f); 
	}
	
	
	public String toString() {
		StringBuffer b = new StringBuffer();
		int n;
		b.append("id" + id);
		b.append(": ");
		if (opcode == 0) {
			b.append("QUERY");
		}
		b.append(" " + showFlags(flags));
		b.append(qr ? "Q" : "R");
		b.append(":");
		b.append(aa ? "AA" : "-A");
		b.append(":");
		b.append(tc ? "TC" : "-T");
		b.append(":");
		b.append(rd ? "RD" : "-D");
		b.append(ra ? "RA" : "-A");
		b.append(": ");
		switch (rcode) {
		case (0):
			b.append("OK");
		break;
		case (1):
			b.append("FormatErr");
		break;
		case (2):
			b.append("SrvFail");
		break;
		case (3):
			if (aa) {
				b.append("NxName");
			}
		break;
		case (4):
			b.append("NotImpl");
		break;
		case (5):
			b.append("WillNot");
		break;
		}
		b.append(":");
		b.append(" queries " + queryCount);
		b.append(" ");
		b.append(" ans " + answerCount);
		b.append(" ");
		b.append(" auth " + authorityCount);
		b.append(" ");
		b.append(" addl " + additionalCount);
		b.append("\n");

		n = 0;
		for (int j = 0; j < answerCount; j++) {
			b.append("     Answer " + (++n));
			b.append("\n");
			b.append(ansArray[j].toString());
		}
		b.append("\n");

		n = 0;
		for (int j = 0; j < authorityCount; j++) {
			b.append("     Authority " + (++n));
			b.append("\n");
			b.append(autArray[j].toString());
		}
		b.append("\n");

		n = 0;
		for (int j = 0; j < additionalCount; j++) {
			b.append("     Additional " + (++n));
			b.append("\n");
			b.append(addArray[j].toString());
		}
		b.append("\n");

		return b.toString();
	}
}
