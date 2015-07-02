package org.sdnhub.flowtags;

import java.util.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// public class PSINF extends FTMiddleBoxes {
public class PSINFEdge {

    public int id;
    public PSINF sNF; // from node
    public PSINF dNF; // to node
    public String tNote; // traffic annotation

    public PSINFEdge(PSINF s, PSINF d, String t){
        sNF = s;
        dNF = d;
        tNote = t;
    }

}
