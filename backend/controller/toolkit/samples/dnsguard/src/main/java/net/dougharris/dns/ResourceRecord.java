package net.dougharris.dns;

import net.dougharris.dumppacket.TTL;
import net.dougharris.utility.DumpHex;

public class ResourceRecord {
	private String name;
	private int cls;
	private int type;
	private int ttl;
	private int length;
	private byte[] data;

	private String src_ip, dst_ip;
	private short udp_src, udp_dst;
	
	private final static Class[] typeClasses = new Class[]
	{
		null,
		AResourceRecord.class,
		NSResourceRecord.class,
		null,
		null,
		CNAMEResourceRecord.class,
		SOAResourceRecord.class,
		null,
		null,
		null,
		null,//ResourceRecord.NULLResourceRecord,
		null,//ResourceRecord.WKSResourceRecord,
		PTRResourceRecord.class,
		null, //ResourceRecord.HINFOResourceRecord,
		null,
		MXResourceRecord.class,
		null //ResourceRecord.TXTResourceRecord,
	};

	public int getLength() {
		return length;
	}

	public byte[] getData() {
		return data;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public void setRclass(int cls) {
		this.cls = cls;
	}

	public int getRclass() {
		return cls;
	}

	public void setType(int type) {
		this.type = type;
	}

	public int getType() {
		return type;
	}

	public void setTTL(int ttl) {
		this.ttl = ttl;
	}

	public int getTTL() {
		return ttl;
	}

	public void setLength(int length) {
		this.length = length;
	}

	public void setData(byte[] data) {
		this.data = data;
	}

	private String headerToString() {
		StringBuffer b = new StringBuffer();
		int t = getType();
		b.append("Name:" + getName() + " ");
		b.append("Type:" + RFC1035.getTypeString(t) + " ");
		b.append("Class:" + RFC1035.getRclassString(1) + " ");
		b.append("TTL:" + new TTL(getTTL()) + " ");
		b.append("Length:" + getLength() + "\n ");
		return b.toString();
	}

	/* This is the default one they get */
	/* Not filling the byte[] may cause a problem */
	/* if it does not know the subclass */
	/* I need a polymorphic dataToString! */
	/* I also need a polymorphic readData originally */
	public String dataToString() {
		String result = null;
		switch (getType()) {
		case 1:
			((AResourceRecord) this).dataToString();
			break;
		case 2:
			((NSResourceRecord) this).dataToString();
			break;
		case 5:
			((CNAMEResourceRecord) this).dataToString();
			break;
		case 6:
			((SOAResourceRecord) this).dataToString();
			break;
		case 12:
			((PTRResourceRecord) this).dataToString();
			break;
		case 15:
			((MXResourceRecord) this).dataToString();
			break;
		default:
			result = DumpHex.bytesPrint(getData());
			break;
		}
		return result;
	}

	public String toString() {
		return (headerToString() + dataToString());
	}
}
