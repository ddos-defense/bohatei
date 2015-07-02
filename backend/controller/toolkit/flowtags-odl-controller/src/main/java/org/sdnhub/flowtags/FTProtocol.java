package org.sdnhub.flowtags;

import java.util.HashMap;
import java.util.Map;

public enum FTProtocol {
	UDP((byte)0x11), TCP((byte)0x06);
	
	private byte code;
	 
	private static Map<Byte, FTProtocol> codeToStatusMapping;
	 
	 private FTProtocol(byte proto) {
	   this.code = proto;
	 }
	 
	 public byte getByte() {
	   return code;	   
	 }
	 
	 public static FTProtocol getType(byte i) {
       if (codeToStatusMapping == null) {
           initMapping();
       }
       return codeToStatusMapping.get( new Byte(i)  );
   }
	 
   private static void initMapping() {
   	
       codeToStatusMapping = new HashMap<Byte, FTProtocol>();
       
       for (FTProtocol type : values()) {
           codeToStatusMapping.put(type.code, type);
       }
   }

   @Override
   public String toString() {
   	 return this.name();
   }
   
}
