package org.sdnhub.flowtags;

import java.util.HashMap;
import java.util.Map;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.lang.*;
import java.io.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class PSIBenchmark {

    private static volatile PSIBenchmark instance;
    protected static final Logger log = LoggerFactory.getLogger(PSIBenchmark.class);
    private PSIFSMManager fsmManager;

    private static final long MEGABYTE = 1024L * 1024L;
    public static long bytesToMegabytes(long bytes) {
        return bytes / MEGABYTE;
    }

    public PSIBenchmark(){
    }

    public static PSIBenchmark getInstance() {
        if (instance == null) {
            synchronized (PSIBenchmark.class) {
                if (instance == null) {
                    instance = new PSIBenchmark();
                }
            }
        }
        return instance;
    }

    public void PSIMemory(){
        System.out.print("PSIMemory()");
        
        // Get the Java runtime
        Runtime runtime = Runtime.getRuntime();
        // Run the garbage collector
        runtime.gc();
        // Calculate the used memory
        long memory = runtime.totalMemory() - runtime.freeMemory();
        System.out.print("Used memory is bytes: " + memory);
        System.out.print("Used memory is megabytes: "+ bytesToMegabytes(memory)+" ");
    }

}
