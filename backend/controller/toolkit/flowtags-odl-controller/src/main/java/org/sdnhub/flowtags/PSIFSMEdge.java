package org.sdnhub.flowtags;

import java.util.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// public class PSIDAG extends FTMiddleBoxes {
public class PSIFSMEdge {

    protected static final Logger log = LoggerFactory.getLogger(PSIFSMEdge.class);

    private int id;
    private String key;
    private String sState;
    private String dState;
    private String event;

    public PSIFSMEdge(){
    }

    public PSIFSMEdge( String sS, String dS, String e){
        sState = sS;
        dState = dS;
        event = e;
    }
    public String getEvent(){
        log.info("getEvent()"+ sState + dState + event);
        return event;
    }
    public void setEvent(String s){
        event = s;
    }
    public String getSState(){
        return sState;
    }
    public void setSState(String s){
        sState = s;
    }
    public String getDState(){
        return dState;
    }
    public void setDState(String s){
        dState = s;
    }
    
}
