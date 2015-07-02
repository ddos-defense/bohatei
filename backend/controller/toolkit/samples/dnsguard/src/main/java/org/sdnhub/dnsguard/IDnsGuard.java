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

import java.util.List;
import java.util.Properties;

import org.sdnhub.dnsguard.renders.DnsRecordReply;
import org.sdnhub.dnsguard.renders.DnsUsage;
import org.sdnhub.dnsguard.renders.Violator;

public interface IDnsGuard {

	public String echo (String in);
	
	public List<String> lazyresolv (String appIp);
	
	public List<String> appsbyip (String sourceIp);
	
	public List<Violator>getViolators();
	
	public List<DnsUsage>getExternalDnsUsage(int top);
	
	public List<DnsRecordReply>getDatabaseDnsRecords(int limit, int offset);
	
	public String setLocalDnsServer (String local_dns);
	
	public String getLocalDnsServer ();
	
	//db related
	public Boolean isConnected();
	public void savePropsAndConnect(Properties props);
	
}
