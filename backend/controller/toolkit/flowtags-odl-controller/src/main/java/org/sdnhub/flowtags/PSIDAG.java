package org.sdnhub.flowtags;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Stack;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PSIDAG {

    protected static final Logger log = LoggerFactory.getLogger(PSIPolicyManager.class);

    /**
     *      * The implementation here is basically an adjacency list, but instead
     *           * of an array of lists, a Map is used to map each vertex to its list of 
     *                * adjacent vertices.
     *                     */   
    private Map<PSINF, List<PSINF>> neighbors = new HashMap<PSINF,List<PSINF>>();
    private List<PSINF> nodelist = new ArrayList<PSINF>();
    private Map<String,PSINF> nodemap = new HashMap<String,PSINF>();
    private List<PSIDAGEdge> edgelist = new ArrayList<PSIDAGEdge>();

    private String key;

    public PSIDAG(){
    }

    public PSIDAG(String s){
        log.info("PSIDAG construction");
        key = s;
        log.info("PSIDAG "+key);
    }

    public List<PSIDAGEdge> getDAGEdge(){
        return edgelist;
    }

    /*
     * return node by key
     * */
    public PSINF getNF(String s){
        return nodemap.get(s);
    }

    public void setKey(String s){
        key = s;
    }

    public String getKey(){
        return key;
    }

    public void addEdge(PSIDAG from, PSIDAG to, PSITrans trans)
    {
        edgelist.add(new PSIDAGEdge(from, to, trans));
    }

    /**
     *      * String representation of graph.
     *           */
    public String toString () {
        StringBuffer s = new StringBuffer();
        for (PSINF v: neighbors.keySet()) s.append("\n    " + v + " -> " + neighbors.get(v));
        return s.toString();                
    }

    /**
     *      * Add a vertex to the graph.  Nothing happens if vertex is already in graph.
     *           */
    public void add (PSINF vertex) {
        if (neighbors.containsKey(vertex)) return;
        neighbors.put(vertex, new ArrayList<PSINF>());
        // nodelist.add(vertex);
        nodemap.put(vertex.str, vertex);
    }

    /**
     *      * True iff graph contains vertex.
     *           */
    public boolean contains (PSINF vertex) {
        return neighbors.containsKey(vertex);
    }

    /**
     *      * Add an edge to the graph; if either vertex does not exist, it's added.
     *           * This implementation allows the creation of multi-edges and self-loops.
     *                */
    public void add (PSINF from, PSINF to, String aNote) {
        log.info("this");
        this.add(from); this.add(to);
        log.info("neighbor");
        neighbors.get(from).add(to);
        log.info("from");
        from.addEdge(from, to, aNote);
        log.info("from");
    }

    /**
     *      * Remove an edge from the graph.  Nothing happens if no such edge.
     *           * @throws IllegalArgumentException if either vertex doesn't exist.
     *                */
    public void remove (PSINF from, PSINF to) {
        if (!(this.contains(from) && this.contains(to)))
            throw new IllegalArgumentException("Nonexistent vertex");
        neighbors.get(from).remove(to);
        // TODO add remove for from 
    }

}
