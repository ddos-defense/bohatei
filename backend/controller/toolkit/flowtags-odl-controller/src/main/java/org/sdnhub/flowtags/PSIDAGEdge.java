package org.sdnhub.flowtags;

import java.util.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PSIDAGEdge {

    public int id;
    public PSIDAG sDAG; // from node
    public PSIDAG dDAG; // to node
    public PSITrans trans; // Transition

    public PSIDAGEdge(PSIDAG s, PSIDAG d, PSITrans t){
        sDAG = s;
        dDAG = d;
        trans = t;
    }

}
