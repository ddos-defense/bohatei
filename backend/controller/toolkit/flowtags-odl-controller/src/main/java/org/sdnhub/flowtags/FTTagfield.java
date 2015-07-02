package org.sdnhub.flowtags;

import java.util.HashMap;
import java.util.Map;

public enum FTTagfield {
	NOTDEFINED(0), TOS(1), VLAN(2);
	 
	private int code;
	 
	private static Map<Integer, FTTagfield> codeToStatusMapping;
	 
	private FTTagfield(int type) {
	   this.code = type;
	}
	 
	public int getInt() {
	   return code;	   
	}
	 
     public static FTTagfield getTagfield(int i) {
        if (codeToStatusMapping == null) {
            initMapping();
        }
        return codeToStatusMapping.get(i);
    }
	 
    private static void initMapping() {
    	
        codeToStatusMapping = new HashMap<Integer, FTTagfield>();
        
        for (FTTagfield field : values()) {
            codeToStatusMapping.put(field.code, field);
        }
    }
    
    @Override
    public String toString() {
    	 return this.name();
    }
}
