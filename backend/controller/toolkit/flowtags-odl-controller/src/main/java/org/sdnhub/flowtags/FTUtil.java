package org.sdnhub.flowtags;

import java.math.BigInteger;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class FTUtil {
	public static enum RouterType {
		HostAgent, Core, VirtualCore
	};

	public FTUtil() {
	}

	// These depend on Topology
	public static String getSWID(int id) {
		if (id < 0x100) {
			return "00:00:00:00:00:00:00:" + FTUtil.intToHex(id);
		} else if (id < 0x10000) {
			return "00:00:00:00:00:00:" + FTUtil.intToHex((id >> 8)) + ":"
					+ FTUtil.intToHex(id & 0xff);
		} else if (id < 0x1000000) {
			return "00:00:00:00:00:" + FTUtil.intToHex((id >> 16)) + ":"
					+ FTUtil.intToHex((id & 0xff00) >> 8) + ":"
					+ FTUtil.intToHex(id & 0xff);
		}
		return "";
	}
	
	public static String getSWID(long id) {
		if (id < 0x100) {
			return "00:00:00:00:00:00:00:" + FTUtil.intToHex(id);
		} else if (id < 0x10000) {
			return "00:00:00:00:00:00:" + FTUtil.intToHex((id >> 8)) + ":"
					+ FTUtil.intToHex(id & 0xff);
		} else if (id < 0x1000000) {
			return "00:00:00:00:00:" + FTUtil.intToHex((id >> 16)) + ":"
					+ FTUtil.intToHex((id & 0xff00) >> 8) + ":"
					+ FTUtil.intToHex(id & 0xff);
		} else if (id < 0x100000000L ){
			return "00:00:00:00:" + FTUtil.intToHex((id >> 24)) + ":"
					+FTUtil.intToHex((id &0xff0000) >> 16) + ":"
					+ FTUtil.intToHex((id & 0xff00) >> 8) + ":"
					+ FTUtil.intToHex(id & 0xff);
		} else if ( id < 0x10000000000L) {
			return "00:00:00:" + FTUtil.intToHex((id >>32)) + ":" +
					FTUtil.intToHex((id & 0xff000000l)>> 24) + ":"
					+ FTUtil.intToHex((id & 0xff0000)>> 16) + ":"
					+ FTUtil.intToHex((id & 0xff00) >> 8) + ":"
					+ FTUtil.intToHex(id & 0xff);
		} else if ( id < 0x1000000000000L) {
			return "00:00:" + FTUtil.intToHex((id>>40)) + ":" +
					FTUtil.intToHex((id & 0xff00000000L)>>32) + ":" +
					FTUtil.intToHex((id & 0xff000000L)>>24) + ":" +
					FTUtil.intToHex((id & 0xff0000)>> 16) + ":"
					+ FTUtil.intToHex((id & 0xff00) >> 8) + ":"
					+ FTUtil.intToHex(id & 0xff);
		}
		return "";
	}

	private static String intToHex(int id) {
		if (id < 0x10) {
			return "0" + Integer.toHexString(id);
		} else {
			return Integer.toHexString(id);
		}
	}
	
	private static String intToHex(long _id) {
		int id = (int)_id;
		if (id < 0x10) {
			return "0" + Integer.toHexString(id);
		} else {
			return Integer.toHexString(id);
		}
	}
	

	public static String getSWName(int id) {
		return "s" + id;
	}

	public static FTUtil.RouterType getRouterType(String nodeID) {
		String[] ids = nodeID.split(":");
		if( ids[0].equals("00") && ids[1].equals("00") && ids[2].equals("00") && ids[3].equals("00") && 
				ids[4].equals("00") && ids[5].equals("00")){
			if(ids[6].equals("00")){
				return RouterType.HostAgent;
			} else {
				return RouterType.VirtualCore;
			}
		}else{
			return RouterType.Core;
		}
	}

	public static String IPAddressStringFromInt(int address)
			throws UnknownHostException {
		if (address == 0) {
			return "0";
		}
		ByteBuffer bb = ByteBuffer.allocate(4);
		bb.order(ByteOrder.BIG_ENDIAN);
		bb.clear();
		String ipString = InetAddress.getByAddress(bb.putInt(address).array())
				.toString().substring(1);
		return ipString;
	}

	public static String IPAddressStringFromInt(Integer address)
			throws UnknownHostException {
		if (address.intValue() == 0) {
			return "0";
		}
		ByteBuffer bb = ByteBuffer.allocate(4);
		bb.order(ByteOrder.BIG_ENDIAN);
		bb.clear();
		String ipString = InetAddress
				.getByAddress(bb.putInt(address.intValue()).array()).toString()
				.substring(1);
		return ipString;
	}

	public static InetAddress InetAddressFromInt(int address)
			throws UnknownHostException {
		ByteBuffer buffer = ByteBuffer.allocate(32);
		buffer.putInt(address);
		buffer.position(0);
		byte[] bytes = new byte[4];
		buffer.get(bytes);
		return InetAddress.getByAddress(bytes);
	}
	
	public static byte[] MacAddressBytesFromString(String macAddress){
		String[] macAddressParts = macAddress.split(":");
		byte[] macAddressBytes = new byte[6];
		for (int i = 0; i < 6; i++) {
			Integer hex = Integer.parseInt(macAddressParts[i], 16);
			macAddressBytes[i] = hex.byteValue();
		}
		return macAddressBytes;
	}

}
