/*
 * Copyright (c) 2014 SDN Hub.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * author: Luis Chiang
 */

package org.sdnhub.dnsguard;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import net.dougharris.dns.RFC1035;
import net.dougharris.dns.ResourceRecord;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DnsReply {
	protected static final Logger log = LoggerFactory.getLogger(DnsReply.class);

	private RFC1035 dns_reply;

	private String src_ip, dst_ip;
	private int udp_src, udp_dst;

	public DnsReply(RFC1035 rfc1035) {
		dns_reply = rfc1035; 
	}

	public void setPacketHeaders(String src_ip, String dst_ip, short udp_src,
			short udp_dst) {

		this.setSrc_ip(src_ip);
		this.setDst_ip(dst_ip);

		this.setUdp_dst(udp_dst);
		this.setUdp_src(udp_src);

		if (udp_dst < 0) {
			this.setUdp_dst(udp_dst & 0xFFFF);
		}

		if (udp_src < 0) {
			this.setUdp_src(udp_src & 0xFFFF);
		}
	}

	public ResourceRecord[] getAnswers() {
		return this.dns_reply.ansArray;
	}

	public List<ResourceRecord> getListAnswers() {

		List<ResourceRecord> tmp = (List<ResourceRecord>) Arrays
				.asList(this.dns_reply.ansArray);

		return tmp;
	}

	public ResourceRecord[] getAuthoritative() {
		return this.dns_reply.autArray;
	}

	public List<ResourceRecord> getListAuthoritative() {
		return (List<ResourceRecord>) Arrays.asList(this.dns_reply.autArray);
	}

	public ResourceRecord[] getAdditionals() {
		return this.dns_reply.addArray;
	}

	public List<ResourceRecord> getListAdditionals() {
		return (List<ResourceRecord>) Arrays.asList(this.dns_reply.addArray);
	}

	public List<ResourceRecord> getAllAnswers() {

		List<ResourceRecord> allanswers = new ArrayList<ResourceRecord>();

		allanswers.addAll(getListAnswers());
		allanswers.addAll(getListAdditionals());
		allanswers.addAll(getListAuthoritative());

		return allanswers;
	}

	public String getSrc_ip() {
		return src_ip;
	}

	public void setSrc_ip(String src_ip) {
		this.src_ip = src_ip;
	}

	public String getDst_ip() {
		return dst_ip;
	}

	public void setDst_ip(String dst_ip) {
		this.dst_ip = dst_ip;
	}

	public int getUdp_dst() {
		return udp_dst;
	}

	public void setUdp_dst(int i) {
		this.udp_dst = i;
	}

	public int getUdp_src() {
		return udp_src;
	}

	public void setUdp_src(int udp_src) {
		this.udp_src = udp_src;
	}
}
