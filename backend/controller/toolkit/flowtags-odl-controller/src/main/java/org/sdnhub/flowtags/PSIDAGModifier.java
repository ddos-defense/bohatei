package org.sdnhub.flowtags;

import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.lang.*;
import java.util.*;
import java.io.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class PSIDAGModifier {

    private static volatile PSIDAGModifier instance;
    protected static final Logger log = LoggerFactory.getLogger(PSIDAGModifier.class);

    private SocketChannel socket;
    /* Control Middleboxes and Switches */
    private FTTags ftTags = null;
    private FTOutputTags ftOutTags = null;
    private FTMiddeBoxes mbes = null;
    private CoreRouter coreRouter = null;
    private HashMap<String, EdgeResponder> edgeSW = null;
    private CoreRouters coreRouters = null;

    public PSIDAGModifier(){
        // init
    }

    public static PSIDAGModifier getInstance() {
        if (instance == null) {
            synchronized (PSIDAGModifier.class) {
                if (instance == null) {
                    instance = new PSIDAGModifier();
                }
            }
        }
        return instance;
    }

    public void setCoreRouters(CoreRouters routers){
        log.info("setSwitch");
        coreRouters = routers;
        //String rid = (coreRouters.getRouter("00:00:00:00:00:01:00:01")).toString();
        //log.info("routers: "+rid);
    }

    public void setSocket(SocketChannel s){
        socket = s;
    }

    public void handleTransition(PSITrans trans){
        String dagdif = trans.DAGdif;
        log.info("PSIDAGModifier handleTransition PSITrans" + dagdif);

        this.swUpdate(dagdif);

        // String s = "flowtag_control out add -mbId 1 -state 0 -preTag 1 -newTag 4 -next 5";
        // switchMod();
        // this.switchMod("00:00:00:00:00:01:00:01", "10.2.0.1", "1", "00:00:00:00:00:02","4", 10);
        /*
        CoreRouter router = this.coreRouters.getRouter("00:00:00:00:00:01:00:01");
        router.addFlow("10.2.0.1", "255.255.255.0", "1","00:00:00:00:00:04","2",(short) 13);
        */
    }

    public boolean swUpdate(String dagdif){
        this.switchMod("00:00:00:00:00:01:00:01", "10.2.0.1", "1", "00:00:00:00:00:02","3", 11);
        this.switchMod("00:00:00:00:00:01:00:01", "10.2.0.1", "3", "00:00:00:00:00:02","2", 11);
        return true;
    }

    // bootStrap 
    public boolean swBootStrap(String nodeId){
        // ingress
        if (nodeId.equals("00:00:00:00:00:01:00:01"))
        {
            this.switchMod("00:00:00:00:00:01:00:01", "10.2.0.1", "1", "00:00:00:00:00:02","4", 10);
            this.switchMod("00:00:00:00:00:01:00:01", "10.2.0.1", "4", "00:00:00:00:00:02","2", 10);
            //this.switchMod("00:00:00:00:00:01:00:01", "10.3.0.1", "2", "00:00:00:00:00:03","3", 10);
            //this.switchMod("00:00:00:00:00:01:00:01", "10.2.0.1", "3", "00:00:00:00:00:02","2", 10);
        }
        
        /*
        this.switchMod("00:00:00:00:00:01:00:01", "10.2.0.1", "1", "00:00:00:00:00:02","3", 10);
        // egress
        this.switchMod("00:00:00:00:00:02:00:02", "10.2.0.1", "3", "00:00:00:00:00:02","1", 10);
        // DAG
        this.switchMod("00:00:00:00:00:03:00:03", "10.2.0.1", "2", "00:00:00:00:00:02","1", 10);
        this.switchMod("00:00:00:00:00:03:00:03", "10.2.0.1", "1", "00:00:00:00:00:02","3", 10);
        */
        return true;
    }

    /*
    public boolean switchMod(){
        // TODO which sw to modify
        CoreRouter router = this.coreRouters.getRouter("00:00:00:00:00:01:00:01");
        router.addFlow("10.3.0.1", "255.255.255.0", "1","00:00:00:00:00:04","4",(short) 11);
        
        return router.addFlow("10.3.0.1", "255.255.255.0", "4","00:00:00:00:00:04","3",(short) 12);
    }
    */

    public boolean switchMod(String swID, String dstIP, String inport, String dstMac, String outport, int prior ){
        CoreRouter router = this.coreRouters.getRouter(swID);
        return router.addFlow(dstIP, "255.255.255.0", inport, dstMac, outport, (short) prior);
        
        // TODO which sw to modify
        /*
        CoreRouter router = this.coreRouters.getRouter("00:00:00:00:00:01:00:01");
        router.addFlow("10.3.0.1", "255.255.255.0", "1","00:00:00:00:00:04","4",(short) 11);
        
        return router.addFlow("10.3.0.1", "255.255.255.0", "4","00:00:00:00:00:04","3",(short) 12);
        */
    }

    public long trans_num = 0;
    public long s_time = 0;
    public long c_time = 0;
    public void measureThroughput(){
        /*
         * Measurement
         * */
        // throughtput
        trans_num += 1;
        if (trans_num == 1) s_time = System.currentTimeMillis();
        c_time = System.currentTimeMillis();
        if (trans_num > 1)
        {
            // log.info("Logger name = " + log.getName());
            if ((trans_num%50000)==1)
            {
                //log.info("Trans rate ="+(((float)trans_num)*1000/(c_time-s_time)));
                System.out.print("Trans rate ="+(((float)trans_num)*1000/(c_time-s_time)));
                //System.out.print("\n");
            }
        }
    }

    public void cBench(){
        /*
           try {
           Thread.sleep(29000);
           } catch (InterruptedException ie) {
        //Handle exception
           }
           */

        try{
            String s = "psipongmsg";
            ByteBuffer msgbuffer = ByteBuffer.allocate(48);
            msgbuffer.clear(); 
            msgbuffer.put(s.getBytes());
            msgbuffer.flip();
            while (msgbuffer.hasRemaining())
            {
                socket.write(msgbuffer);
            }
        }
        catch (IOException e)
        {
            log.info("IO Error");
        }
    }

    public void execCMD(String s)
    {
        Process p;
        try {
            p = Runtime.getRuntime().exec(s);
            BufferedReader br = new BufferedReader(
                    new InputStreamReader(p.getInputStream()));
            while ((s = br.readLine()) != null)
                log.info("line:" + s);
            p.waitFor();
            log.info("exit:" + p.exitValue());
            p.destroy();
        } catch (Exception e) {}
    }

    /**/
    public void handleTransition(){
        // handle transition
        log.info("PSIDAGModifier handleTransition");
        // single line cmd
        String s;
        Process p;
        try {
            p = Runtime.getRuntime().exec("rsync root@192.168.123.4:/etc/snort/rules/psi_test.rules /users/Tianlong/psi_repo/psi_test.rules");
            BufferedReader br = new BufferedReader(
                    new InputStreamReader(p.getInputStream()));
            while ((s = br.readLine()) != null)
                log.info("line:" + s);
            p.waitFor();
            log.info("exit:" + p.exitValue());
            p.destroy();
        } catch (Exception e) {}
    }
}
