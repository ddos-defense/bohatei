package org.sdnhub.flowtags;

import java.util.HashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FTOutputTags {
	
	private HashMap<FTOutputTagKey, FTOutputTagValue> outputTags = null;
	private HashMap<FTOutputTagKey, FTOutputTagValue> outputTagsWithSrcIP = null;
	private FTTags ftTags;
	protected static final Logger logger = LoggerFactory.getLogger(Flowtags.class);
	
	public FTOutputTags(FTTags ftTags){
		this.outputTags = new HashMap<FTOutputTagKey, FTOutputTagValue>();
		this.outputTagsWithSrcIP = new HashMap<FTOutputTagKey, FTOutputTagValue>();
		this.ftTags = ftTags;
	}
	
	public boolean add(Integer mid, Integer state, Integer preTag, Integer newTag, Integer nextHost){
		if( preTag != 0 && ! this.ftTags.contains(preTag)){
			logger.info("preTag:" + preTag + " is not registered at tags");
			System.out.println("preTag:" + preTag + " is not registered at tags");
			return false;
		}
		if( newTag != 0 && ! this.ftTags.contains(newTag)){
			logger.info("newTag:" + newTag + " is not registered at tags");
			System.out.println("newTag:" + newTag + " is not registered at tags");
			return false;
		}
		
		this.outputTags.put(new FTOutputTagKey(mid, state, preTag), new FTOutputTagValue(newTag, nextHost));
		int srcIP = this.ftTags.getFiveTuple(preTag).getNwSrc();
		if(srcIP != 0){
			this.outputTagsWithSrcIP.put(new FTOutputTagKey(mid, state, new Integer(srcIP)), new FTOutputTagValue(newTag, nextHost));
		}
		return true;
	}
	
	public boolean add(int mid, int state, int preTag, int newTag, int nextHost){
		if(preTag != 0 && ! this.ftTags.contains(new Integer(preTag))){
			logger.info("preTag:" + preTag + " is not registered at tags");
			System.out.println("preTag:" + preTag + " is not registered at tags");
			return false;
		}
		if(newTag !=0 && ! this.ftTags.contains(new Integer(newTag))){
			logger.info("newTag:" + newTag + " is not registered at tags");
			System.out.println("newTag:" + newTag + " is not registered at tags");
			return false;
		}
		this.outputTags.put(new FTOutputTagKey(mid, state, preTag), new FTOutputTagValue(newTag, nextHost));
		int srcIP = this.ftTags.getFiveTuple(preTag).getNwSrc();
		if(srcIP != 0){
			this.outputTagsWithSrcIP.put(new FTOutputTagKey(mid, state, srcIP), new FTOutputTagValue(newTag, nextHost));
		}
		return true;
	}
	
	public boolean del(int mid, int state, int preTag){
		FTOutputTagKey key = new FTOutputTagKey(mid, state, preTag);
		if(this.outputTags.containsKey(key)){
			this.outputTags.remove(key);
		}
		int srcIP = this.ftTags.getFiveTuple(preTag).getNwSrc();
		key = new FTOutputTagKey(mid, state, srcIP);
		if(this.outputTagsWithSrcIP.containsKey(key)){
			this.outputTagsWithSrcIP.remove(key);
		}
		return true;
	}
	
	public boolean clear(){
		this.outputTags.clear();
		this.outputTagsWithSrcIP.clear();
		return true;
	}
	
	public int get(int mid, int state, int preTag){
		System.out.println("mid:" + mid + " state:" + state + " preTag:" + preTag);
		FTOutputTagKey key = new FTOutputTagKey(mid, state, preTag);
		if( this.outputTags.containsKey(key)){
			return outputTags.get(key).nextTag.intValue();
		}
		else{
			return -1;
		}
	}
	
	public int getWithSrcIP(int mid, int state, int srcIP){
		System.out.println("mid:" + mid + " state:" + state + " srcIP:" + srcIP);
		FTOutputTagKey key = new FTOutputTagKey(mid, state, srcIP);
		if( this.outputTagsWithSrcIP.containsKey(key)){
			return outputTagsWithSrcIP.get(key).nextTag.intValue();
		}
		else{
			return -1;
		}
	}
	
	@Override
	public String toString(){
		String s="";
		for (FTOutputTagKey key : this.outputTags.keySet()){
			s += "<" + key.toString() + ">, <"+ this.outputTags.get(key) + ">\n";
		}
		return s;
	}

	private class FTOutputTagKey{
		private Integer mid = null;
		private Integer state = null;
		private Integer preTag = null;
		
		public FTOutputTagKey(int mid, int state, int preTag){
			this.mid = new Integer(mid);
			this.state = new Integer(state);
			this.preTag = new Integer(preTag);
		}
		
		public FTOutputTagKey(Integer mid, Integer state, Integer preTag){
			this.mid = mid;
			this.state = state;
			this.preTag = preTag;
		}
		
		@Override
		public boolean equals(Object object) {
			if (this == object) {
				return true;
			}
			if (object == null || object.getClass() != this.getClass()) {
				return false;
			}
			FTOutputTagKey another = (FTOutputTagKey) object;
			return this.toString().equals(another.toString());
		}
		
		@Override
		public int hashCode() {
			int h = 1;
			h = h * 31 + (this.mid == null ? 0 : this.mid.hashCode());
			h = h * 31 + (this.state == null ? 0 : this.state.hashCode());
			h = h * 31 + (this.preTag == null ? 0 : this.preTag.hashCode());
			return h;
		}
		
		@Override
		public String toString(){
			return this.mid.toString() + ":" + this.state.toString() + ":" + this.preTag.toString();
		}
	}
	
	private class FTOutputTagValue{
		private Integer nextTag;
		private Integer nextHost;
		
		private FTOutputTagValue(int nextTag, int nextHost){
			this.nextTag = new Integer(nextTag);
			this.nextHost = new Integer(nextHost);
		}
		
		private FTOutputTagValue(Integer nextTag, Integer nextHost){
			this.nextTag = nextTag;
			this.nextHost = nextHost;
		}
		@Override
		public String toString(){
			return this.nextTag.toString() + ":" + this.nextHost.toString();
		}
		
	}
}
