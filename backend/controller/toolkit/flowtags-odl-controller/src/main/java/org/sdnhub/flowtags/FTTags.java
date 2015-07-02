package org.sdnhub.flowtags;

import java.util.HashMap;

public class FTTags {
	private HashMap<Integer, FTValue> tags = null;
	private HashMap<Integer, Integer> tagsWithSrcIP = null;
	
	public FTTags(){
		this.tags = new HashMap<Integer, FTValue>();
		this.tagsWithSrcIP = new HashMap<Integer, Integer>();
	}
	
	public boolean add(Integer tag, FTFiveTuple fiveTuple, int next){
		this.tags.put(tag, new FTValue(fiveTuple, next));
		Integer key = new Integer(this.getFiveTuple(tag).getNwSrc());
		if(key != 0){
			this.tagsWithSrcIP.put(key,tag);
		}
		return true;
	}
	
	public boolean add(Integer tag, FTFiveTuple fiveTuple){
		this.tags.put(tag, new FTValue(fiveTuple));
		Integer key = new Integer(this.getFiveTuple(tag).getNwSrc());
		if(key !=0){
			this.tagsWithSrcIP.put(key, tag);
		}
		return true;
	}
	
	public boolean del(Integer tag){
		if(! this.tags.containsKey(new Integer(tag))){
			return false;
		} 
		Integer key = new Integer(this.getFiveTuple(tag).getNwSrc());
		if(this.tagsWithSrcIP.containsKey(key)){
			this.tagsWithSrcIP.remove(key);
		}
		this.tags.remove(tag);
		return true;
	}
	
	public void clear(){
		this.tags.clear();
		this.tagsWithSrcIP.clear();
	}
	
	public FTFiveTuple getFiveTuple(int tag){
		Integer key = new Integer(tag);
		if(this.tags.containsKey(key)){
			return this.tags.get(key).fiveTuple;
		}
		else{
			return null;
		}
	}
	
	public int getNext(int tag){
		Integer key = new Integer(tag);
		if(this.tags.containsKey(key)){
			return this.tags.get(key).next.intValue();
		}
		return 0;
	}
	
	public boolean contains(int tag){
		return this.tags.containsKey(new Integer(tag));
	}
	
	@Override
	public String toString(){
		String s = "";
		for( Integer tag: this.tags.keySet()){
			s += tag.toString() + "\t" + "<"+ this.tags.get(tag).fiveTuple.toString()+">, " + this.tags.get(tag).next+ "\n";
		}
		return s;
	}
	
	private class FTValue{
		private FTFiveTuple fiveTuple = null;
		private Integer next = null;
		private Integer defulatNext = new Integer(5);
		private FTValue(FTFiveTuple fiveTuple, int next){
			this.fiveTuple = fiveTuple;
			this.next = new Integer(next);
		}
		private FTValue(FTFiveTuple fiveTuple){
			this.next = this.defulatNext;
		}
		
		
	}
}
