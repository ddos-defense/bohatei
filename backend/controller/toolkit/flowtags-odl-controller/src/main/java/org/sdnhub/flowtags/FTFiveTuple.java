package org.sdnhub.flowtags;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class FTFiveTuple {
	private Integer nwSrc = null;
	private Integer nwDst = null;
	private Integer tpSrcPort = null;
	private Integer tpDstPort = null;
	private Integer nwProto = null;

	public FTFiveTuple(int nwSrc, int nwDst, int tpSrcPort, int tpDstPort,
			int nwProto) {
		this.nwSrc = new Integer(nwSrc);
		this.nwDst = new Integer(nwDst);
		this.tpSrcPort = new Integer(tpSrcPort);
		this.tpDstPort = new Integer(tpDstPort);
		this.nwProto = new Integer(nwProto);
	}

	public FTFiveTuple(Integer nwSrc, Integer nwDst, Integer tpSrcPort,
			Integer tpDstPort, Integer nwProto) {
		this.nwSrc = nwSrc;
		this.nwDst = nwDst;
		this.tpSrcPort = tpSrcPort;
		this.tpDstPort = tpDstPort;
		this.nwProto = nwProto;
	}
	
	public int getNwSrc(){
		return this.nwSrc.intValue();
	}
	
	public int getNwDst(){
		return this.nwDst.intValue();
	}
	
	public int getTpSrcPort(){
		return this.tpSrcPort.intValue();
	}
	
	public int getTpDstPort(){
		return this.tpDstPort.intValue();
	}
	
	public int getNwProto(){
		return this.nwProto.intValue();
	}
	
	public FTFiveTuple getNextTuple(FTFiveTuple tagTuple){
		int nextSrc;
		int nextDst;
		int nextTpSrcPort;
		int nextTpDstPort;
		int nextNwProto;
		nextSrc = tagTuple.getNwSrc() == 0 ? this.getNwSrc(): tagTuple.getNwSrc();
		nextDst = tagTuple.getNwDst() == 0 ? this.getNwDst(): tagTuple.getNwDst();
		nextTpSrcPort = tagTuple.getTpSrcPort() == 0 ? this.getTpSrcPort(): tagTuple.getTpSrcPort();
		nextTpDstPort = tagTuple.getTpDstPort() == 0 ? this.getTpDstPort(): tagTuple.getTpDstPort();
		nextNwProto = tagTuple.getNwProto() == 0 ? this.getNwProto(): tagTuple.getNwProto();
		return new FTFiveTuple(nextSrc, nextDst, nextTpSrcPort, nextTpDstPort, nextNwProto);
	}

	@Override
	public String toString() {
//		ByteBuffer bb = ByteBuffer.allocate(4);
//		bb.order(ByteOrder.BIG_ENDIAN);
		String nwSrcString = "";
		String nwDstString = "";
		try {
//			if (this.nwSrc != 0) {
				//bb.clear();
				//nwSrcString = InetAddress.getByAddress(
				//		bb.putInt(this.nwSrc).array()).toString().substring(1);
				nwSrcString = FTUtil.IPAddressStringFromInt(this.nwSrc);
//			} 
//			if (this.nwDst != 0) {
//				bb.clear();
//				nwDstString = InetAddress.getByAddress(
//						bb.putInt(this.nwDst).array()).toString().substring(1);
				nwDstString = FTUtil.IPAddressStringFromInt(this.nwDst);
//			}
//			else{
//				nwDstString = this.nwDst.toString();
//			}
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return nwSrcString + " : " + nwDstString + " : "
				+ this.tpSrcPort.toString() + " : " + this.tpDstPort.toString()
				+ " : " + this.nwProto.toString();
	}
}
