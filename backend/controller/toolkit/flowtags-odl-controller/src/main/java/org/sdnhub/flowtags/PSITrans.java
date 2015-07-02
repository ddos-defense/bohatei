package org.sdnhub.flowtags;

import java.util.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// public class PSIDAG extends FTMiddleBoxes {
public class PSITrans {

    protected static final Logger log = LoggerFactory.getLogger(PSITrans.class);

    public int id;
    public String str;
    public PSIDAG sDAG; // from node
    public List<PSIState> sState = new ArrayList<PSIState>();
    public PSIDAG dDAG; // to node
    public List<PSIState> dState = new ArrayList<PSIState>();
    public String event; // event expression
    public PSITraffic traffic;
    public String DAGdif;

    private boolean isTrans = false;

    public PSITrans(){
    }

    public PSITrans(boolean b)
    {
        isTrans = b;
    }

    public PSITrans(String e, String dif){
        event = e;
        DAGdif = dif;
        
    }

    public PSITrans( boolean b, PSIDAG sd, PSIDAG dd, String e){
        isTrans = b;
        sDAG = sd;
        dDAG = dd;
        event = e;
    }
    public PSITrans(String s, PSIDAG sd, PSIDAG dd, String e){
        str = s;
        sDAG = sd;
        dDAG = dd;
        event = e;
    }
    
    public PSITrans(String s,List<PSIState> sS, List<PSIState> dS, PSIDAG sd, PSIDAG dd, String e){
        str = s;
        sState = sS;
        dState = dS;
        sDAG = sd;
        dDAG = dd;
        log.info("PSITrans1 sS" + sS.get(0).curState);
        log.info("PSITrans1 sState" + sState.get(0).curState);
        event = e;
    }

    public boolean isTrans(){
        return isTrans;
    }

    public boolean isTransState(List<PSIState> sS, List<PSIState> dS){
        log.info("PAITrans size" + String.valueOf(sS.size()) + String.valueOf(sState.size()));
        if (sS.size() != sState.size() )
        {
            return false;
        }
        for (int i = 0; i<sS.size();i++){
            log.info("PAITrans get " + sState.get(i).curState + sS.get(i).curState);
            
            if (!((sState.get(i).curState).equals((sS.get(i).curState))))
            {
                return false;
            }            
        }
        log.info("PAITrans size" + String.valueOf(dS.size()) + String.valueOf(dState.size()));
        if (dS.size() != dState.size() )
        {
            return false;
        }
        for (int i = 0; i<dS.size();i++){
            log.info("PAITrans get " + dState.get(i).curState + dS.get(i).curState);
            if (!((dState.get(i).curState).equals((dS.get(i).curState))))
            {
                return false;
            }            
        }
        return true;
    }

}
