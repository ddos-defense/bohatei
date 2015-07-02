package org.sdnhub.flowtags;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BasicFactory {
	
	protected static final Logger log = LoggerFactory.getLogger(BasicFactory.class);
	
	public List<FTMessage> parseMessages(ByteBuffer inBuffer) {
		
		log.info(inBuffer.toString());
		List<FTMessage> output = new ArrayList<FTMessage>();
		
		// Temporal hack, must fix flowtags api to use htonl
		//inBuffer.order(ByteOrder.LITTLE_ENDIAN);
		
		if(inBuffer.limit() >= FTMessage.REQUIRED_SIZE ){
			
			FTMessage tmp = new FTMessage();
			
			tmp.readFrom(inBuffer);
			 
			output.add(tmp);
		}else{
			log.error("Incoming packet small than minimun size");	
		}
		
		// TODO Auto-generated method stub
		return output;
		
	}

	public List<PSIMessage> parsePSIMessages(ByteBuffer inBuffer) {
		
		log.info(inBuffer.toString());
		List<PSIMessage> output = new ArrayList<PSIMessage>();
		
		// Temporal hack, must fix flowtags api to use htonl
		//inBuffer.order(ByteOrder.LITTLE_ENDIAN);
		
		// if(inBuffer.limit() >= FTMessage.REQUIRED_SIZE ){
		if(inBuffer.limit() >= 3 ){
            log.info("Basic Factory: PSI Message");			
			PSIMessage tmp = new PSIMessage();
			
			tmp.readFrom(inBuffer);
			 
			output.add(tmp);
		}else{
			log.error("Incoming packet small than minimun size");	
		}
		
		// TODO Auto-generated method stub
		return output;
    }	
	 
}
