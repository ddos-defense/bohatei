package org.sdnhub.flowtags;

import java.util.HashMap;
import java.util.Map;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/*
 * FSM for each Traffic
 * */
public class PSIFSM {
    protected static final Logger log = LoggerFactory.getLogger(PSIFSM.class);
    private String key;
    /* only one set of traffic, ref the global set*/
    private PSITraffic traffic;
    /* current Traffic State */
    private String curState;
    /* string - vertex map */
    private Map<String, PSITrafficState> statemap = new HashMap<String, PSITrafficState>();
    /* vertex - edge map */
    private Map<String, List<PSIFSMEdge>> edgemap = new HashMap<String, List<PSIFSMEdge>>();
    /* dagmap */
    private Map<String, PSIDAG> dagmap = new HashMap<String, PSIDAG>();
    /* 
     * vertex - DAG map
     * every state correspond to one DAG 
     * */ 
    private Map<String, String> v_d_map = new HashMap<String, String>();

    public PSIFSM(){
    }

    public PSITrans handleEvent(PSIEvent event){
        log.info("handleEvent(PSIEvent event)");
        /*
         * search all the edges of current state
         * */
        for(PSIFSMEdge edge : edgemap.get(curState)){
            log.info("for(PSIFSMEdge edge "+event.getKey()+" "+edge.getEvent());
            if ((event.getKey()).equals(edge.getEvent()))
            {
                log.info("if ((event.getKey()).equals(edge.getEvent()))");
                String sStateKey = (statemap.get(edge.getSState())).getKey();
                String dStateKey = (statemap.get(edge.getDState())).getKey();

                PSIDAG sDAG = dagmap.get(v_d_map.get(sStateKey));
                PSIDAG dDAG = dagmap.get(v_d_map.get(dStateKey));
                if (!(sStateKey.equals(dStateKey)))
                {
                    log.info("if (!(sStateKey.equals(dStateKey)))");
                    // Jump to next state
                    curState = dStateKey;
                    return new PSITrans(true, sDAG, dDAG, edge.getEvent());
                }
                else{
                    return new PSITrans(false, sDAG, dDAG, edge.getEvent());
                }
            }
        }
        return new PSITrans(false);
    }

    public PSIDAG getDAG(String s){
        return dagmap.get(s);
    }

    public void setKey(String s){
        key = s;
    }

    public String getKey(){
        return key;
    }

    public void setTraffic(PSITraffic t){
        traffic = t;
    }

    public PSITraffic getTraffic(){
        return traffic;
    }


    public void setCurState(String s){
        curState = s;
    }

    /**
     *      *      * Add a vertex to the graph.  Nothing happens if vertex is already in graph.
     *          */
    public boolean addState (PSITrafficState state) {
        if (statemap.containsKey(state.getKey())) return false;
        statemap.put(state.getKey(), state);
        edgemap.put(state.getKey(), new ArrayList<PSIFSMEdge>());
        return true;
    }

    /**
     *      *      * True iff graph contains vertex.
     *           *           */
    public boolean contains (PSITrafficState state) {
        return statemap.containsKey(state.getKey());
    }

    public void addEdge (String key, PSIFSMEdge edge){
       (edgemap.get(key)).add(edge);
    }

    public boolean addDAG (PSIDAG dag) {
        if (dagmap.containsKey(dag.getKey())) return false;
        dagmap.put(dag.getKey(), dag);
        return true;
    }



}
