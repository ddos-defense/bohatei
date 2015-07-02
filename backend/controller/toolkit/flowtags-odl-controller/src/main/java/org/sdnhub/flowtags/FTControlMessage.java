package org.sdnhub.flowtags;

import java.util.ArrayList;

public class FTControlMessage {
	private int controlType;
	private ArrayList<Integer> args;
	private String reply = "";
	
	public static final int TAG_ADD = 1;
	public static final int TAG_DEL = 2;
	public static final int TAG_DUMP = 3;
	public static final int TAG_CLEAR = 4;
	public static final int OUTTAG_ADD = 5;
	public static final int OUTTAG_DEL = 6;
	public static final int OUTTAG_DUMP = 7;
	public static final int OUTTAG_CLEAR = 8;
	public static final int MB_ADD = 9;
	public static final int MB_DEL = 10;
	public static final int MB_DUMP = 11;
	public static final int MB_CLEAR = 12;
	public static final int FW_ADD = 13;
	public static final int FW_DEL = 14;
	public static final int FW_DUMP = 15;
	public static final int FW_CLEAR = 16;
	public static final int TEST_ADD = 17;
	public static final int TEST_DEL = 18;
	public static final int TAG_GENERATE = 19;
	public static final int TAG_GENERATE_DEL = 20;
	public static final int TAG_GENERATE_CLEAR = 21;
	
	
	
	public FTControlMessage(int controlType, ArrayList<Integer> args){
		this.controlType = controlType;
		this.args = args;
		System.out.println("ARGS:"+args.size());
		System.out.println(args.toString());
		switch(this.controlType){
		case FTControlMessage.TAG_ADD:
			if(this.args.size() < 6){
				throw new IllegalArgumentException("TAG ADD requires 6 arguments, <tagID, srcIP, dstIP, srcPort, dstPort, Proto>");
			}
			break;
		case FTControlMessage.TAG_DEL:
			if(this.args.size() != 1){
				throw new IllegalArgumentException("TAG DEL requires 1 arguments, <tagID>");
			}
			break;
		case FTControlMessage.TAG_DUMP:
			if(this.args.size() != 0){
				throw new IllegalArgumentException("TAG DUDMP requires 0 arguments");
			}
			break;
		case FTControlMessage.TAG_CLEAR:
			if(this.args.size() != 0){
				throw new IllegalArgumentException("TAG CLEAR requires 0 arguments");
			}
			break;
		case FTControlMessage.OUTTAG_ADD:
			if(this.args.size() != 5){
				throw new IllegalArgumentException("OUTTAG ADD requires 4 arguments, <mid, state, preTag, newTag, nextHost>");
			}
			break;
		case FTControlMessage.OUTTAG_DEL:
			if(this.args.size() != 3){
				throw new IllegalArgumentException("OUTTAG DEL requires 3 arguments, <mid, state, preTag>");
			}
			break;
		case FTControlMessage.OUTTAG_DUMP:
			if(this.args.size() != 0){
				throw new IllegalArgumentException("OUTTAG DUMP requires 0 arguments");
			}
			break;
		case FTControlMessage.OUTTAG_CLEAR:
			if(this.args.size() != 0){
				throw new IllegalArgumentException("OUTTAG CLEAR requires 0 arguments");
			}
			break;
		case FTControlMessage.MB_ADD:
			if(this.args.size() != 5){
				throw new IllegalArgumentException("MB ADD requires 3 arguments <mid, swid, type, address, mask>");
			}
			break;
		case FTControlMessage.MB_DEL:
			if(this.args.size() != 1){
				throw new IllegalArgumentException("MB ADD requires 1 arguments <mid>");
			}
			break;
		case FTControlMessage.MB_DUMP:
			if(this.args.size() != 0){
				throw new IllegalArgumentException("MB DUMP requires 0");
			}
			break;
		case FTControlMessage.MB_CLEAR:
			if(this.args.size() != 0){
				throw new IllegalArgumentException("MB CLEAR requires 0 arguments");
			}
			break;
		case FTControlMessage.FW_ADD:
			if(this.args.size() != 4 ){
				throw new IllegalArgumentException("FW ADD requires 3 arguments <swID 1st Byte, swID 2nd Byte, mid, connectorID>");
			}
			break;
		case FTControlMessage.FW_DEL:
			if(this.args.size() != 3 ){
				throw new IllegalArgumentException("FW DEL requires 2 arguments <swID, mid>");
			}
			break;
		case FTControlMessage.FW_DUMP:
			if(this.args.size() != 0 ){
				throw new IllegalArgumentException("FW DUMP requires 0 arguments");
			}
			break;
		case FTControlMessage.FW_CLEAR:
			if(this.args.size() != 0 ){
				throw new IllegalArgumentException("FW CLEAR requires 0 arguments");
			}
			break;
		}
	}
	
	public FTControlMessage(String replyMessage){
		this.reply = replyMessage;
	}
	
	public int getControlType(){
		return this.controlType;
	}
	
	public String getReply(){
		return this.reply;
	}
	
	public ArrayList<Integer> getArgs(){
		return this.args;
	}
	
	@Override
	public String toString(){
		return "CType:" + this.controlType + " " + this.args.toString() + " " + this.reply;
	}
	
}
