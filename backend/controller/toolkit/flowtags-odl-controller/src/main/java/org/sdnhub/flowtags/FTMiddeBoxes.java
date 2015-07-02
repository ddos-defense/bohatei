package org.sdnhub.flowtags;

import java.net.UnknownHostException;
import java.util.HashMap;

public class FTMiddeBoxes {
	public static enum TYPE {
		FT_NAT, FT_CONSUME, FT_NON_CONSUME, FT_MONITOR, FT_CLENT
	};

	private HashMap<Integer, FTMiddeBoxInfo> mbes = null;
	private HashMap<String, FTMiddeBoxInfo> mbesWithSWID = null;

	public FTMiddeBoxes() {
		this.mbes = new HashMap<Integer, FTMiddeBoxInfo>();
		this.mbesWithSWID = new HashMap<String, FTMiddeBoxInfo>();
	}

	public boolean add(int mbid, int id, int type, int address, int mask) {
		Integer key = new Integer(mbid);
		FTMiddeBoxInfo mbInfo = null;
		switch (type) {
		case 0:
			mbInfo = new FTMiddeBoxInfo(mbid, id, TYPE.FT_NAT, address, mask);
			break;
		case 1:
			mbInfo = new FTMiddeBoxInfo(mbid, id, TYPE.FT_CONSUME, address,
					mask);
			break;
		case 2:
			mbInfo = new FTMiddeBoxInfo(mbid, id, TYPE.FT_NON_CONSUME, address,
					mask);
			break;
		case 3:
			mbInfo = new FTMiddeBoxInfo(mbid, id, TYPE.FT_MONITOR, address,
					mask);
			break;
		default:
			return false;
		}
		this.mbes.put(key, mbInfo);
		this.mbesWithSWID.put(mbInfo.swID, mbInfo);
		return true;
	}

	public boolean del(int mbid) {
		Integer key = new Integer(mbid);
		if (!this.mbes.containsKey(key)) {
			return false;
		}
		this.mbes.remove(key);
		return true;
	}

	public boolean clear() {
		this.mbes.clear();
		return true;
	}

	@Override
	public String toString() {
		String s = "";
		for (Integer mbid : this.mbes.keySet()) {
			s += mbid.toString() + ":" + this.mbes.get(mbid).toString() + "\n";
		}
		return s;
	}

	public FTMiddeBoxes.TYPE getType(int mbid) {
		Integer key = new Integer(mbid);
		if (this.mbes.containsKey(key)) {
			return this.mbes.get(key).type;
		} else {
			return null;
		}
	}

	public FTMiddeBoxes.TYPE getType(String swID) {
		if (this.mbesWithSWID.containsKey(swID)) {
			return this.mbesWithSWID.get(swID).type;
		} else {
			return null;
		}
	}

	public String getSWID(int mbid) {
		Integer key = new Integer(mbid);
		if (this.mbes.containsKey(key)) {
			return this.mbes.get(key).swID;
		} else {
			return null;
		}
	}

	public int getMBID(String swID) {
		if (this.mbesWithSWID.containsKey(swID)) {
			return this.mbesWithSWID.get(swID).mbID;
		} else {
			return -1;
		}
	}

	public int getHostID(int mbid) {
		Integer key = new Integer(mbid);
		if (this.mbes.containsKey(key)) {
			return this.mbes.get(key).hostID;
		}
		return -1;
	}

	private class FTMiddeBoxInfo {
		private FTMiddeBoxes.TYPE type;
		private String swID;
		private String swName;
		private int mbID;
		private int hostID;
		private int address;
		private int mask;

		private FTMiddeBoxInfo(int mbID, int hostID, FTMiddeBoxes.TYPE type,
				int address, int mask) {
			this.type = type;
			this.mbID = mbID;
			this.hostID = hostID;
			this.swID = FTUtil.getSWID(hostID);
			this.swName = FTUtil.getSWName(hostID);
			this.address = address;
			this.mask = mask;
		}

		@Override
		public String toString() {
			try {
				return this.swID + ":" + this.swName + ":"
						+ this.type.toString() + ":"
						+ FTUtil.IPAddressStringFromInt(address) + "/"
						+ FTUtil.IPAddressStringFromInt(mask);
			} catch (UnknownHostException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return null;
		}
	}

}
