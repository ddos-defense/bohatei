package org.sdnhub.flowtags;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PSIPredicates {

    private String IP;// = PSIUtil.STR;
    private int port;
    private String Group;// = PSIUtil.STR;

    public PSIPredicates(String sip){
        IP = sip;
    }

    public PSIPredicates(String sip, int p){
        IP = sip;
        port = p;
    }

    public void setIP(String s){
        IP = s;
    }

    public String getIP(){
        return IP;
    }

}
