package org.sdnhub.odl.learningswitch;

public class LearningSwitchOptions {

	public static final int NUM_OPTIONS = 7;
	
	public static final Long SRC_MAC = 1L;
	public static final Long DST_MAC = 2L;
	public static final Long SRC_IPv4 = 4L;
	public static final Long DST_IPv4 = 8L;
	public static final Long IPv4_PROT = 16L;
	public static final Long SRC_PORT = 32L;
	public static final Long DST_PORT = 64L;
	

	
	public Long options;
	
	public LearningSwitchOptions() {
		options = 0L;
	}
}
