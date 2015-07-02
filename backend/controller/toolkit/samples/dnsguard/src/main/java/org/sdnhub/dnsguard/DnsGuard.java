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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.StringWriter;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import net.dougharris.dns.RFC1035;

import org.opendaylight.controller.hosttracker.IfHostListener;
import org.opendaylight.controller.hosttracker.hostAware.HostNodeConnector;
import org.opendaylight.controller.sal.action.Action;
import org.opendaylight.controller.sal.action.Controller;
import org.opendaylight.controller.sal.core.Node;
import org.opendaylight.controller.sal.core.NodeConnector;
import org.opendaylight.controller.sal.core.Property;
import org.opendaylight.controller.sal.core.UpdateType;
import org.opendaylight.controller.sal.flowprogrammer.Flow;
import org.opendaylight.controller.sal.flowprogrammer.IFlowProgrammerService;
import org.opendaylight.controller.sal.inventory.IListenInventoryUpdates;
import org.opendaylight.controller.sal.match.Match;
import org.opendaylight.controller.sal.match.MatchField;
import org.opendaylight.controller.sal.match.MatchType;
import org.opendaylight.controller.sal.packet.Ethernet;
import org.opendaylight.controller.sal.packet.IDataPacketService;
import org.opendaylight.controller.sal.packet.IListenDataPacket;
import org.opendaylight.controller.sal.packet.IPv4;
import org.opendaylight.controller.sal.packet.Packet;
import org.opendaylight.controller.sal.packet.PacketResult;
import org.opendaylight.controller.sal.packet.RawPacket;
import org.opendaylight.controller.sal.packet.UDP;
import org.opendaylight.controller.sal.utils.EtherTypes;
import org.opendaylight.controller.sal.utils.NetUtils;
import org.opendaylight.controller.sal.utils.ServiceHelper;
import org.opendaylight.controller.sal.utils.Status;
import org.opendaylight.controller.switchmanager.ISwitchManager;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.sdnhub.dnsguard.renders.DnsRecordReply;
import org.sdnhub.dnsguard.renders.DnsUsage;
import org.sdnhub.dnsguard.renders.Violator;
//import org.sdnhub.learningswitch.ILearningSwitch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.hsqldb.Server;

public class DnsGuard implements IListenDataPacket, IListenInventoryUpdates,
		IfHostListener, IDnsGuard {
	
	private Server hsqlServer;
	private DnsGuardPersistence database;
	
	protected static final Logger log = LoggerFactory.getLogger(DnsGuard.class);
	private String dbconfig_filename = "database.config";
	
	private IDataPacketService dataPacketService = null;

	void setDataPacketService(IDataPacketService s) {
		this.dataPacketService = s;
	}

	void unsetDataPacketService(IDataPacketService s) {
		if (this.dataPacketService == s) {
			this.dataPacketService = null;
		}
	}

	private ISwitchManager swManager = null;

	void setSwitchManager(ISwitchManager s) {
		log.info("set ISwitchManager");
		this.swManager = s;
	}

	void unsetSwitchManager(ISwitchManager s) {
		if (this.swManager == s) {
			this.swManager = null;
		}
	}

	private IFlowProgrammerService programmer = null;

	public void setFlowProgrammerService(IFlowProgrammerService s) {
		this.programmer = s;
	}

	public void unsetFlowProgrammerService(IFlowProgrammerService s) {
		if (this.programmer == s) {
			this.programmer = null;
		}
	}

	@Override
	public PacketResult receiveDataPacket(RawPacket inPkt) {

		// log.info("packet arrived");

		Packet etherpack = this.dataPacketService.decodeDataPacket(inPkt);

		if (etherpack instanceof Ethernet) {

			Packet ippack = etherpack.getPayload();

			if (ippack instanceof IPv4) {

				Packet udppackt = ippack.getPayload();

				if (udppackt instanceof UDP) {

					// & 0xFFFF
					short sport = ((UDP) udppackt).getSourcePort();
					short dport = ((UDP) udppackt).getDestinationPort();

					if (sport == 53 || dport == 53) {

						int sip = ((IPv4) ippack).getSourceAddress();
						int dip = ((IPv4) ippack).getDestinationAddress();

						byte[] dnspacket = ((UDP) udppackt).getRawPayload();

						if (dnspacket != null) {

							RFC1035 dns_msg = new RFC1035(dnspacket);

							try {
								dns_msg.parse();

								if (dns_msg.qr == false) {

									log.info(
											"DNS REPLY len: {}  from: {} to: {} dport: {}",
											dnspacket.length, NetUtils
													.getInetAddress(sip)
													.toString(), NetUtils
													.getInetAddress(dip)
													.toString(), dport);

									DnsReply reply = new DnsReply(dns_msg);

									if (dns_msg.getCountAnswer() > 0) {
										reply.setPacketHeaders(NetUtils
												.getInetAddress(sip).toString()
												.replace("/", ""), NetUtils
												.getInetAddress(dip).toString()
												.replace("/", ""), sport, dport);
										database.save(reply);
									}
								}

							} catch (Exception ex) {

								log.error(ex.getLocalizedMessage());
							}
						}
					}
				}
			}
		}

		return PacketResult.KEEP_PROCESSING;
	}

	void init() {

//		ILearningSwitch simple = (ILearningSwitch) ServiceHelper.getInstance(
//				ILearningSwitch.class, "default", this);
//		if (simple == null) {
//			log.error("Not able to find the learningswitch");
//		} else {
//			log.info("learningswitch found, loaded before dnsguard OK");
//		}

		// read info from config file
		
	}

	void start(){
		
//		   hsqlServer = new Server();
//
//         // HSQLDB prints out a lot of informations when
//         // starting and closing, which we don't need now.
//         // Normally you should point the setLogWriter
//         // to some Writer object that could store the logs.
//         hsqlServer.setLogWriter(null);
//         hsqlServer.setSilent(true);
//
//         // The actual database will be named 'xdb' and its
//         // settings and data will be stored in files
//         // testdb.properties and testdb.script
//         hsqlServer.setDatabaseName(0, "dnsspy");
//         hsqlServer.setDatabasePath(0, "file:dbfile");
//
//         // Start the database!
//         hsqlServer.start();
         
		 connectToDb();
	}
	
	void stop(){
		if (hsqlServer != null) {
            hsqlServer.stop();
        }
	}
	
	private String getDatabaseSql(){
		
		BundleContext bundleContext = FrameworkUtil.getBundle( this.getClass()).getBundleContext();

		URL urlfile = bundleContext.getBundle().getResource("/database.sql"); 
		
		InputStream input;
		
		//TODO: LC FIX
		String sqlCreation = "";
		
		try{
			
			input = urlfile.openConnection().getInputStream();
			
			BufferedReader br = new BufferedReader(new InputStreamReader(input));
			String line;
			
			while ((line = br.readLine()) != null) {
				sqlCreation = sqlCreation + line;
			}
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return sqlCreation;
		
	}
	private void connectToDb(){
 	  
		String sqlCreation = getDatabaseSql(); 
		Properties prop = getStartupConfig(dbconfig_filename);
		
		// connect to db
		database = new DnsGuardPersistence(prop.getProperty("dbserver"),
				Integer.valueOf(prop.getProperty("dbport")),
				prop.getProperty("dbname"), prop.getProperty("dbuser"),
				prop.getProperty("dbpasswd"), Integer.valueOf(prop
						.getProperty("internal_buffer_init_size")),
				Integer.valueOf(prop.getProperty("internal_buffer_max_size")), sqlCreation);
		 
		
		boolean dbconx = database.Connect();

		log.info("Database connected: {} ", dbconx);
	}

	/*
	 * This method read from a file shipped with the bundle... 
	 * This method is deprecated, and still in code to have a reference how to read resources
	 */
//	public Properties getStartupConfig(String filename) {
//
//		// http://www.mkyong.com/java/java-properties-file-examples/
//
//		Properties prop = new Properties();
//		InputStream input = null;
//
//		try {
//
//			BundleContext bundleContext = FrameworkUtil.getBundle(
//					this.getClass()).getBundleContext();
//			 
//			URL urlfile = bundleContext.getBundle().getResource(
//					"/database.config");
//
//			// input = new FileInputStream(
//			// bundleContext.getBundle().getResource("database.config").to );
//
//			input = urlfile.openConnection().getInputStream();
//
//			// load a properties file
//			prop.load(input);
//
//			input.close();
//			 
//			Enumeration<?> e = prop.propertyNames();
//			while (e.hasMoreElements()) {
//				String key = (String) e.nextElement();
//				String value = prop.getProperty(key);
//				log.info(key + ": " + value);
//			}
//			
//				//LC: This section is for using a file in the local bundle storage (seems only available at running time)
//				File foutput = bundleContext.getDataFile("dns-guard-save.txt");
//				OutputStream  fostream = new FileOutputStream(foutput);
//				
//				Properties newprop = new Properties();
//				newprop.put("newval", "luis");
//				
//				newprop.store(fostream, "");
//				
//				fostream.close();
//			
//			return prop;
//
//		} catch (IOException ex) {
//			ex.printStackTrace();
//		} finally {
//			if (input != null) {
//				try {
//					input.close();
//				} catch (IOException e) {
//					e.printStackTrace();
//				}
//			}
//		}
//
//		return null;
//
//	}

	public Properties getStartupConfig(String filename) {

		// http://www.mkyong.com/java/java-properties-file-examples/

		Properties prop = new Properties();
		InputStream input = null;

		try {

			BundleContext bundleContext = FrameworkUtil.getBundle(
					this.getClass()).getBundleContext();
			 
			File configfile = bundleContext.getDataFile( dbconfig_filename );
			 
			input = new FileInputStream(configfile);

			// load a properties file
			prop.load(input);

			input.close();
			
			Enumeration<?> e = prop.propertyNames();
			while (e.hasMoreElements()) {
				String key = (String) e.nextElement();
				String value = prop.getProperty(key);
				log.info(key + ": " + value);
			}
			
			return prop;

		} catch (IOException ex) {
			ex.printStackTrace();
		} finally {
			if (input != null) {
				try {
					input.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

		return prop;

	}
	
	@Override
	public String echo(String in) {
		return in + " from class";
	}

	@Override
	public List<String> lazyresolv(String appIp) {

		// select distinct request FROM dnsspy.bulkreply where data =
		// '61.211.161.1'
		if (database.Connect()) {
			return database.lazyresolv(appIp);
		}

		return new ArrayList<String>();
	}

	@Override
	public List<String> appsbyip(String sourceIp) {

		// select distinct request FROM dnsspy.bulkreply where data =
		// '61.211.161.1'
		if (database.Connect()) {
			return database.appsbyip(sourceIp);
		}

		return new ArrayList<String>();
	}

	@Override
	public void updateNode(Node node, UpdateType type, Set<Property> props) {
		// log.info("updateNode, NodeType {} Update {} Prop {}",
		// node.getID().toString(), type.toString(), props.toString());

		if (type == UpdateType.ADDED) {

			for (Iterator<Property> it = props.iterator(); it.hasNext();) {

				Property pval = it.next();

				// log.info("updateNode {} - {}", pval.getName().toString(),
				// pval.getStringValue());

				if (pval.getName() == "macAddress") {

					log.info("sw is up");

					// forward dns traffic to controller: TP_DST, (short)53
					Match match = new Match();
					match.setField(new MatchField(MatchType.DL_TYPE,
							EtherTypes.IPv4.shortValue()));
					match.setField(new MatchField(MatchType.NW_PROTO, (byte) 17));
					match.setField(new MatchField(MatchType.TP_DST, (short) 53));

					List<Action> actions = new ArrayList<Action>();
					actions.add(new Controller());

					Flow flow = new Flow(match, actions);
					flow.setIdleTimeout((short) 0);
					flow.setHardTimeout((short) 0);
					flow.setPriority((short) 32769);

					// Modify the flow on the network node
					Status status = programmer.addFlow(node, flow);

					if (!status.isSuccess()) {
						log.warn(
								"SDN Plugin failed to program the flow: {}. The failure is: {}",
								flow, status.getDescription());
					}

					// forward dns traffic to controller: TP_SRC, (short)53
					// match = new Match();
					// match.setField( new MatchField(MatchType.DL_TYPE,
					// EtherTypes.IPv4.shortValue()));
					// match.setField( new MatchField(MatchType.NW_PROTO,
					// (byte)17) );
					// match.setField( new MatchField(MatchType.TP_SRC,
					// (short)53) );
					//
					// actions = new ArrayList<Action>();
					// actions.add(new Controller());
					//
					// flow = new Flow(match, actions);
					// flow.setIdleTimeout((short) 0);
					// flow.setHardTimeout((short) 0);
					// flow.setPriority( (short) 32769);
					//
					// // Modify the flow on the network node
					// status = programmer.addFlow(node, flow);
					//
					// if (!status.isSuccess()) {
					// log.warn("SDN Plugin failed to program the flow: {}. The failure is: {}",
					// flow, status.getDescription());
					// }

					// if(this.swManager != null){
					// Set<NodeConnector> nconx =
					// this.swManager.getNodeConnectors(node);
					// log.info("got nconx");
					// }
				}

			}

		}

	}

	@Override
	public void updateNodeConnector(NodeConnector nodeConnector,
			UpdateType type, Set<Property> props) {

	}

	@Override
	public void hostListener(HostNodeConnector host) {
		log.info("new host {}", host.getNetworkAddressAsString());
	}

	@Override
	public List<Violator> getViolators() {

		// select distinct request FROM dnsspy.bulkreply where data =
		// '61.211.161.1'
		if (database.Connect()) {
			return database.getViolators();
		}

		return new ArrayList<>();
	}

	@Override
	public String setLocalDnsServer(String local_dns) {

		// select distinct request FROM dnsspy.bulkreply where data =
		// '61.211.161.1'
		if (database.Connect()) {
			return database.setLocalDnsServer(local_dns);
		}

		return new String("Error updating data");
	}

	@Override
	public String getLocalDnsServer() {

		// select distinct request FROM dnsspy.bulkreply where data =
		// '61.211.161.1'
		if (database.Connect()) {
			return database.getLocalDnsServer();
		}

		return new String("Error getting data");
	}

	@Override
	public List<DnsUsage> getExternalDnsUsage(int top) {
		// select distinct request FROM dnsspy.bulkreply where data =
		// '61.211.161.1'
		if (database.Connect()) {

			return database.getExternalDnsUsage(top);
		}

		return new ArrayList<>();
	}

	@Override
	public List<DnsRecordReply>getDatabaseDnsRecords(int limit, int offset) {
		// TODO Auto-generated method stub
		if (database.Connect()) {

			return database.getDatabaseDnsRecords(limit, offset);
		}

		return new ArrayList<>();
	}
	
	@Override
	public Boolean isConnected(){
		
		if(database != null){
			return database.isConnected();
		}
		
		return false;
	}
	
	@Override
	public void savePropsAndConnect(Properties props){
		
		BundleContext bundleContext = FrameworkUtil.getBundle(
				this.getClass()).getBundleContext();
		
		File foutput = bundleContext.getDataFile( dbconfig_filename );
		OutputStream fostream;
		
		try {
			fostream = new FileOutputStream(foutput);
			
			 
	 		props.store(fostream, "");
	 		fostream.close();
	 		
		} catch ( IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
 
		connectToDb();
		
	}
	
}
