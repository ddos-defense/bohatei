package org.sdnhub.flowtags;

import java.util.HashMap;
import java.util.Map;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class PSIPolicyManager {
    
    private static volatile PSIPolicyManager instance;
    protected static final Logger log = LoggerFactory.getLogger(PSIPolicyManager.class);
    private PSIFSMManager fsmManager;

    public PSIPolicyManager(){
        fsmManager = new PSIFSMManager(); 
    }

    public static PSIPolicyManager getInstance() {
        if (instance == null) {
            synchronized (PSIPolicyManager.class) {
                if (instance == null) {
                    instance = new PSIPolicyManager();
                }
            }
        }
        return instance;
    }


    public void handleEvent(PSIEvent s){
        // handle transition
        log.info("PSIPolicyManager handleEvent");
        fsmManager.handleEvent(s);
    }

}
