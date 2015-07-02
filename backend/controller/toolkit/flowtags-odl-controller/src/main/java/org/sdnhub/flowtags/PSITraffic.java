package org.sdnhub.flowtags;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PSITraffic {
    private String key;
    private PSIPredicates src;
    private PSIPredicates dst;

    public PSITraffic(PSIPredicates s, PSIPredicates d){
        src = s;
        dst = d;
    }
    public PSITraffic(String k, PSIPredicates s, PSIPredicates d){
        key = k;
        src = s;
        dst = d;
    }
}
