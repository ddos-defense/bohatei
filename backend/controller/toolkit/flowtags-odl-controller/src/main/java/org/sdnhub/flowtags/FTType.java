package org.sdnhub.flowtags;

import java.util.HashMap;
import java.util.Map;

public enum FTType {
	 QUERY(0), INTAG(1), OUTTAG(2), CONTROL(3), CONTROL_REPLY(4);
	 
	 private int code;
	 
	 private static Map<Integer, FTType> codeToStatusMapping;
	 
	 private FTType(int type) {
	   this.code = type;
	 }
	 
	 public int getInt() {
	   return code;	   
	 }
	 
	 public static FTType getType(int i) {
        if (codeToStatusMapping == null) {
            initMapping();
        }
        return codeToStatusMapping.get(i);
    }
	 
    private static void initMapping() {
    	
        codeToStatusMapping = new HashMap<Integer, FTType>();
        
        for (FTType type : values()) {
            codeToStatusMapping.put(type.code, type);
        }
    }

    @Override
    public String toString() {
    	 return this.name();
    } 
    
}