package org.sdnhub.flowtags;

import java.util.HashMap;
import java.util.Map;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import java.lang.*;
import java.io.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PSIFSMManager {

    protected static final Logger log = LoggerFactory.getLogger(PSIFSMManager.class); 
    static StringBuilder sb = new StringBuilder();

    /* traffic map */
    private Map<String, PSITraffic> trafficmap = new HashMap<String, PSITraffic>();
    /* event map */
    private Map<String, PSIEvent> eventmap = new HashMap<String, PSIEvent>();
    /* fsm map */
    private Map<String, PSIFSM> fsmmap = new HashMap<String, PSIFSM>();
    /* 
     * event - fsm map 
     * an event can relate to multiple traffic
     * */
    private Map<String, List<String>> e_f_map = new HashMap<String, List<String>>();

    /*
     * traffic fsm map
     * */
    private Map<String, String> e_t_map = new HashMap<String, String>();

    public String policyfilepath = "/testbed/opendaylight/toolkit/psi-odl-controller/psi_res/policy";

    public PSIFSMManager()
    {
        
        // System.out.print("PSIFSMManager()");
        for (int i=1;i<10;i++)
        {
            String s = "/users/Tianlong/policydir/policy"+i;
            // String s = policyfilepath+i;
            readPolicy(s);
        }
        
        //System.out.print("PSIBenchmark");
        PSIBenchmark.getInstance().PSIMemory();        
        // System.out.print("PSIBenchmark");
        //readPolicy();
    } 

    public void handleEvent(PSIEvent event){
        log.info("handleEvent(PSIEvent event)"+event.getKey()+" "+e_f_map.get(event.getKey()).get(0));
        /*
         * map event to fsm
         * */
        for (String fsmkey : e_f_map.get(event.getKey())){
            PSIFSM fsm = fsmmap.get(fsmkey);
            PSITrans trans = fsm.handleEvent(event);
            if (trans.isTrans()){
                log.info("if (trans.isTrans){");
                PSIDAGModifier.getInstance().handleTransition(trans);
            }
        }
        
    }


    private void readPolicy(String policyfile){
        PSIFSM fsm = new PSIFSM();
        List<String> e_f_List = new ArrayList<String>();
        BufferedReader br = null;
        try
        {
            String sCurrentLine;
            FileReader freader = new FileReader(policyfile);
            br = new BufferedReader(freader);
            // br = new BufferedReader(new FileReader(policyfile));

            /*
             * parse symbol
             * : + =
             *
             * */
            while ((sCurrentLine = br.readLine()) != null)
            {
                // log.info("PolicySet CurrentLine="+sCurrentLine);
                String[] splited = sCurrentLine.split("\\s+");
                // log.info("PolicySet Token="+splited[0]);

                if (splited[0].equals("FSM"))
                {
                    String fsmkey = splited[1];
                    String eventkey = splited[2];
                    String curstate = splited[3];
                    fsm.setKey(fsmkey);
                    fsmmap.put(fsmkey, fsm);
                    eventmap.put(eventkey, new PSIEvent(eventkey));
                    e_f_List.add(fsmkey);
                    e_f_map.put(eventkey, e_f_List);
                    fsm.setCurState(curstate);
                }
                else if (splited[0].equals("Traffic"))
                {
                    String traffickey = splited[1];
                    
                    // Traffic
                    PSIPredicates sPred = new PSIPredicates(splited[2], Integer.parseInt(splited[3]));
                    PSIPredicates dPred = new PSIPredicates(splited[4], Integer.parseInt(splited[5]));
                    PSITraffic traffic = new PSITraffic(traffickey, sPred, dPred);
                    fsm.setTraffic(traffic);
                    trafficmap.put(traffickey, traffic);
                }
                else if (splited[0].equals("DAG"))
                {
                    //log.info("enter DAG");
                    PSIDAG DAGtmp = new PSIDAG(splited[1]); 
                    //log.info("enter DAG"+splited[1]);
                    //log.info("DAGtmp"+DAGtmp.getKey());
                    String[] NFList = (splited[2].split("\\:")[1]).split("\\+");
                     
                    //log.info("NFList"+DAGtmp.getKey());
                    for (int i=0;i<NFList.length;i++){
                        //log.info("for NFList"+DAGtmp.getKey());
                        String[] NFpara = NFList[i].split("\\="); 
                        //log.info("for NFList tmp"+NFpara[0]+NFpara[1]);
                        PSINF NFtmp = new PSINF(NFpara[0], NFpara[1]);
                        //log.info("DAGtmp"+DAGtmp.getKey());
                        DAGtmp.add(NFtmp);
                    }
                    //log.info("NFEdgeList NFList"+DAGtmp.getKey());
                    String[] NFEdgeList = (splited[3].split("\\:")[1]).split("\\+");
                    //log.info("NFEdgeList NFList"+DAGtmp.getKey());
                    for (int i=0;i<NFEdgeList.length;i++){
                        //log.info("for NFEdgeList NFList size "+NFEdgeList.length);
                        String[] para = NFEdgeList[i].split("\\=");
                        //log.info("for NFEdgeList NFList size "+para.length);
                        //log.info("for NFList tmp"+para[0]+para[1]+para[2]);
                        PSINF sNFtmp = DAGtmp.getNF(para[0]);
                        //log.info("for NFList sNFtmp "+sNFtmp.str);
                        PSINF dNFtmp = DAGtmp.getNF(para[1]);
                        //log.info("for NFList dNFtmp "+dNFtmp.str);
                        DAGtmp.add(sNFtmp, dNFtmp, para[2]);
                        //log.info("for DAGtmp");
                    }
                    //log.info("DAGtmp"+DAGtmp.getKey());
                    fsm.addDAG(DAGtmp);
                }
                else if (splited[0].equals("PSITrafficState")) 
                {
                    fsm.addState(new PSITrafficState(splited[1].split("\\:")[0], splited[1].split("\\:")[1]));
                    fsm.addState(new PSITrafficState(splited[2].split("\\:")[0], splited[2].split("\\:")[1]));
                }
                else if (splited[0].equals("PSIFSMEdge")) 
                {

                    String[] state = (splited[1].split("\\:")[1]).split("\\=");
                    //log.info("sState " +state[0]);
                    //log.info("dState " +state[1]);

                    PSIFSMEdge fsmedge = new PSIFSMEdge(state[0], state[1], splited[2].split("\\:")[1]); 
                    fsm.addEdge(state[0],fsmedge);
                }
                else{
                }

            }
            freader.close();
        } catch (IOException e)
        {
            e.printStackTrace();
        }
        
        fsmmap.put(fsm.getKey(), fsm);

        /* Test for Input*/
        // log.info("DAG1 IDS str"+policy.getDAG("DAG1").getNF("IDS").str);
        /*
        log.info("DAG1 IDS str"+policy.getDAG("DAG1").getNF("IDS").str);
        log.info("DAG1 IDS conf"+policy.getDAG("DAG1").getNF("IDS").config);
        log.info("DAG1 IDS conf"+policy.getDAG("DAG1").getNF("IDS").edgelist.get(0).tNote);
        log.info("DAG1 IDS conf"+policy.getDAG("DAG1").getNF("IDS").edgelist.get(1).tNote);
        log.info("DAG1 IPS str"+policy.getDAG("DAG1").getNF("IPS").str);
        log.info("DAG1 IPS conf"+policy.getDAG("DAG1").getNF("IPS").config);
        log.info("DAG2 IDS str"+policy.getDAG("DAG2").getNF("IDS").str);
        log.info("DAG2 IDS conf"+policy.getDAG("DAG2").getNF("IDS").config);
        log.info("DAG2 IPS str"+policy.getDAG("DAG2").getNF("IPS").str);
        log.info("DAG2 IPS conf"+policy.getDAG("DAG2").getNF("IPS").config);
        */
    }

    private static void helper(String cur)
    {
        String[] splited = cur.split("\\s+");
        sb.append(cur +"\n"); 
    }

}
