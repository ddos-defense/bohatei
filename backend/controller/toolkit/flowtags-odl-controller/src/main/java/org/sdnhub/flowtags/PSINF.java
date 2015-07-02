package org.sdnhub.flowtags;

import java.util.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// public class PSINF extends FTMiddleBoxes {
public class PSINF {
    public static enum PSINF_TYPE {
        PSI_IDS, PSI_IPS
    };

    public String str;
    public String config;
    public int NF_id;
    public List<PSINFEdge> edgelist = new ArrayList<PSINFEdge>();

    public PSINF(String s, String sc){
        str = s;
        config = sc;
    }

    public PSINF(String s, int d){
        config = s;
        NF_id = d;
    }

    public void addEdge(PSINFEdge edge){
        edgelist.add(edge);
    }

    /*
     * tNote: traffic annotation
     * */
    public void addEdge(PSINF from, PSINF to, String tNote)
    {
        edgelist.add(new PSINFEdge(from, to, tNote));
    }
}
