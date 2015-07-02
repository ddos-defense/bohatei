package org.sdnhub.flowtags;

import java.util.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PSITrafficState {
    private int id;
    private String key;
    private String DAG;

    // modify

    private String curState = "default";
    private Map<String, String> map = new HashMap<String, String>();

    public PSITraffic traffic;
    public String DAGdif;

    public PSITrafficState(){
    }
    
    public PSITrafficState(String s, String d){
        key = s;
        DAG = d;
    }

    public String getKey()
    {
        return key;
    }

    public void setKey(String s){
        key = s;
    }

    public String getDAG()
    {
        return DAG;
    }

    public void setDAG(String s){
        DAG = s;
    }


}
