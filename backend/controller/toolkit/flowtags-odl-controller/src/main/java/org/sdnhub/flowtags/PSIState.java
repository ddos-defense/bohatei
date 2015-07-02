package org.sdnhub.flowtags;

import java.util.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// public class PSIDAG extends FTMiddleBoxes {
public class PSIState {

    public int id;
    public String str;
    public String curState = "default";
    public Map<String, String> map = new HashMap<String, String>();

    public PSITraffic traffic;
    public String DAGdif;

    public PSIState(){
        map.put("infected", "infected");
    }

    public PSIState(String s){
        curState = s;
    }

    public void handleEvent(String e){
       if (map.containsKey(e))
       {
           curState = map.get(e);
       } 
    }
}
