package org.sdnhub.flowtags;

import java.util.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PSIEvent {

    protected static final Logger log = LoggerFactory.getLogger(PSIEvent.class);

    private int id;
    private String key = PSIUtil.STR;
    private PSITraffic traffic;
    private String msg = PSIUtil.STR;

    public PSIEvent(String s){
        key = s;
    }

    public PSIEvent(String k, String srcIP, String dstIP, String m){
        key = k;
        traffic = new PSITraffic(new PSIPredicates(srcIP), new PSIPredicates(dstIP));
        msg = m;
    }

    public void setKey(String s){
        key = s;
    }

    public String getKey(){
        return key;
    }
}
