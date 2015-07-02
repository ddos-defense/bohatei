/*
 * Copyright (c) 2014 SDN Hub.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * author: Luis Chiang
 */

package org.sdnhub.dnsguard.renders;

public class Violator {
	
	private String ip;
	private String timestamp; 
	
	public Violator(String ip, String timestamp){
		this.ip = ip;
		this.timestamp = timestamp;
	}
	
	public String getIp() {
		return ip;
	}
	public void setIp(String ip) {
		this.ip = ip;
	}
	public String getTimestamp() {
		return timestamp;
	}
	public void setTimestamp(String timestamp) {
		this.timestamp = timestamp;
	}

}
