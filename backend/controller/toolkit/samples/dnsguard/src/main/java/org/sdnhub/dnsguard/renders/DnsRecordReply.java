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

public class DnsRecordReply {
	
	private String srcIp, dstIp, request;
	//private int srcPt, dstPt;
	
	private int respType;
	private String data;
	
	private String timestamp;

	public DnsRecordReply(String srcIp, String dstIp, String request, int respType, String data, String timestamp){
		
		this.srcIp = srcIp;
		this.dstIp = dstIp;
		
		this.setRequest(request);
		
		this.respType = respType;
		this.data = data;
		
		this.timestamp = timestamp;
	}
	
	public String getData() {
		return data;
	}
	
	public void setData(String data) {
		this.data = data;
	}
	
	public String getTimestamp() {
		return timestamp;
	}
	
	public void setTimestamp(String timestamp) {
		this.timestamp = timestamp;
	}
	
	public String getDstIp() {
		return dstIp;
	}
	
	public void setDstIp(String dstIp) {
		this.dstIp = dstIp;
	}
	
	public String getSrcIp() {
		return srcIp;
	}
	
	public void setSrcIp(String srcIp) {
		this.srcIp = srcIp;
	}

	public int getRespType() {
		return respType;
	}

	public void setRespType(int respType) {
		this.respType = respType;
	}

	public String getRequest() {
		return request;
	}

	public void setRequest(String request) {
		this.request = request;
	}
}
