/*
 * Copyright (c) 2014 SDN Hub.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * author: Luis Chiang
 */


package org.sdnhub.dnsguard.northbound;

import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.core.UriInfo;

import org.codehaus.enunciate.jaxrs.StatusCodes;
import org.codehaus.enunciate.jaxrs.TypeHint;
import org.opendaylight.controller.northbound.commons.RestMessages;
import org.opendaylight.controller.northbound.commons.exception.ServiceUnavailableException;
import org.opendaylight.controller.northbound.commons.exception.UnauthorizedException;
import org.opendaylight.controller.northbound.commons.utils.NorthboundUtils;
import org.opendaylight.controller.sal.authorization.Privilege;
import org.opendaylight.controller.sal.utils.ServiceHelper;
import org.sdnhub.dnsguard.DnsReply;
import org.sdnhub.dnsguard.IDnsGuard;
import org.sdnhub.dnsguard.renders.D3pieData;
import org.sdnhub.dnsguard.renders.DataTableObject;
import org.sdnhub.dnsguard.renders.DnsRecordReply;
import org.sdnhub.dnsguard.renders.DnsUsage;
import org.sdnhub.dnsguard.renders.Violator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * Northbound REST API
 *
 * This entire web class can be accessed via /northbound prefix as specified in
 * web.xml
 *
 * <br>
 * <br>
 * Authentication scheme : <b>HTTP Basic</b><br>
 * Authentication realm : <b>opendaylight</b><br>
 * Transport : <b>HTTP and HTTPS</b><br>
 * <br>
 * HTTPS Authentication is disabled by default.
 */
@Path("/")
public class AppNorthbound {
	
	protected static final Logger log = LoggerFactory.getLogger(AppNorthbound.class);
	
    @Context
    private UriInfo _uriInfo;
    private String username;

    @Context
    public void setSecurityContext(SecurityContext context) {
        if (context != null && context.getUserPrincipal() != null) {
            username = context.getUserPrincipal().getName();
        }
    }

    protected String getUserName() {
        return username;
    }
    
    /**
     * 
     * Sample REST API call
     * 
     * @return A response string
     * 
     *         <pre>
     * Example:
     * 
     * Request URL:
     * http://127.0.0.1:8080/dnsguard/northbound/test
     * 
     * Response body in XML:
     * &lt;?xml version="1.0" encoding="UTF-8" standalone="yes"?&gt;
     * Sample Northbound API
     * 
     * Response body in JSON:
     * Sample Northbound API
     * </pre>
     */
    @Path("/test")
    @GET
    @Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
    @TypeHint(String.class)
    @StatusCodes()
    public String getTest() {
        String result = "<xml><output>Sample Northbound API from module DnsGuard</output></xml>";
        return result;
    }
    
    /**
     * 
     * Sample REST API call
     * 
     * @return A response string
     * 
     *         <pre>
     * Example:
     * 
     * Request URL:
     * http://127.0.0.1:8080/dnsguard/northbound/echo/{echo}
     * 
     * Response body in XML:
     * &lt;?xml version="1.0" encoding="UTF-8" standalone="yes"?&gt;
     * Sample Northbound API
     * 
     * Response body in JSON:
     * Sample Northbound API
     * </pre>
     */
    @Path("/echo/{echo}")
    @GET
    @Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
    @TypeHint(String.class)
    @StatusCodes()
    public String getEcho(@PathParam("echo") String echo) {
    	
    	if (!NorthboundUtils.isAuthorized(getUserName(), "default", Privilege.READ, this)) {
            throw new UnauthorizedException("User is not authorized to perform this operation");
        }
    	
    	IDnsGuard dnshandler = (IDnsGuard) ServiceHelper.getInstance(IDnsGuard.class, "default", this);
    	
        if (dnshandler == null) {
            throw new ServiceUnavailableException("DnsHandler Service " + RestMessages.SERVICEUNAVAILABLE.toString());
        }

        String result = "<xml><output>Query from module DnsGuard " + dnshandler.echo(echo)  + " </output></xml>";
        
        return result;

   }

    /**
     * 
     * Sample REST API call
     * 
     * @return A response string
     * 
     *         <pre>
     * Example:
     * 
     * Request URL:
     * http://127.0.0.1:8080/dnsguard/northbound/resolv
     * 
     * Response body in XML:
     * &lt;?xml version="1.0" encoding="UTF-8" standalone="yes"?&gt;
     * Sample Northbound API
     * 
     * Response body in JSON:
     * Sample Northbound API
     * </pre>
     */
    @Path("/resolv/{IpFrom}/{AppIp}")
    @GET
    @Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_JSON })
    @TypeHint(String.class)
    @StatusCodes()
    public String getQuery(@PathParam("IpFrom") String ipFrom, @PathParam("AppIp") String appIP) {
    	
    	if (!NorthboundUtils.isAuthorized(getUserName(), "default", Privilege.READ, this)) {
            throw new UnauthorizedException("User is not authorized to perform this operation");
        }
    	
    	IDnsGuard dnshandler = (IDnsGuard) ServiceHelper.getInstance(IDnsGuard.class, "default", this);
    	
        if (dnshandler == null) {
            throw new ServiceUnavailableException("DnsHandler Service " + RestMessages.SERVICEUNAVAILABLE.toString());
        }

        String result = "<xml><output>ipFrom: " + dnshandler.echo(ipFrom)  + " appIP: " + dnshandler.echo(appIP)  + " </output></xml>";
        
        return result;
    }
    
    /**
     * 
     * Sample REST API call
     * 
     * @return A response string
     * 
     *         <pre>
     * Example:
     * 
     * Request URL:
     * http://127.0.0.1:8080/dnsguard/northbound/lazyresolv/appIp
     * 
     * Response body in XML:
     * &lt;?xml version="1.0" encoding="UTF-8" standalone="yes"?&gt;
     * Sample Northbound API
     * 
     * Response body in JSON:
     * Sample Northbound API
     * </pre>
     */
    @Path("/lazyresolv/{appIp}")
    @GET
    @Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_JSON })
    @TypeHint(String.class)
    @StatusCodes()
    public String lazyresolv(@PathParam("appIp") String appIp) {
    	
    	if (!NorthboundUtils.isAuthorized(getUserName(), "default", Privilege.READ, this)) {
            throw new UnauthorizedException("User is not authorized to perform this operation");
        }
    	
    	IDnsGuard dnshandler = (IDnsGuard) ServiceHelper.getInstance(IDnsGuard.class, "default", this);
    	
        if (dnshandler == null) {
            throw new ServiceUnavailableException("DnsHandler Service " + RestMessages.SERVICEUNAVAILABLE.toString());
        }


        String json = new Gson().toJson( dnshandler.lazyresolv(appIp) );
        
        return json;
    }
    
    /***
     * 
     * @param http://127.0.0.1:8080/dnsguard/northbound/appsbyip/{sourceIp}
     * @return The domains visited by an internal IP
     */
    @Path("/appsbyip/{sourceIp}")
    @GET
    @Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_JSON })
    @TypeHint(String.class)
    @StatusCodes()
    public String appsbyip(@PathParam("sourceIp") String sourceIp) {
    	
    	if (!NorthboundUtils.isAuthorized(getUserName(), "default", Privilege.READ, this)) {
            throw new UnauthorizedException("User is not authorized to perform this operation");
        }
    	
    	IDnsGuard dnshandler = (IDnsGuard) ServiceHelper.getInstance(IDnsGuard.class, "default", this);
    	
        if (dnshandler == null) {
            throw new ServiceUnavailableException("DnsHandler Service " + RestMessages.SERVICEUNAVAILABLE.toString());
        }

        String json = new Gson().toJson( dnshandler.appsbyip(sourceIp) );
        
        return json;
    }

    /**
     * 
     * Returns the violators of the local dns server
     * 
     * @return A response string
     * 
     *         <pre>
     * Example:
     * 
     * Request URL:
     * http://127.0.0.1:8080/dnsguard/northbound/getviolators
     * 
     * Response body in XML:
     * &lt;?xml version="1.0" encoding="UTF-8" standalone="yes"?&gt;
     * Sample Northbound API
     * 
     * Response body in JSON:
     * Sample Northbound API
     * </pre>
     */
    @Path("/getviolators")
    @GET
    @Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_JSON })
    @TypeHint(String.class)
    @StatusCodes()
    public String getViolators() {
    	
    	if (!NorthboundUtils.isAuthorized(getUserName(), "default", Privilege.READ, this)) {
            throw new UnauthorizedException("User is not authorized to perform this operation");
        }
    	
    	IDnsGuard dnshandler = (IDnsGuard) ServiceHelper.getInstance(IDnsGuard.class, "default", this);
    	
        if (dnshandler == null) {
            throw new ServiceUnavailableException("DnsHandler Service " + RestMessages.SERVICEUNAVAILABLE.toString());
        }
        
        List<Violator> violators = dnshandler.getViolators();
        
        DataTableObject dataTableObject = new DataTableObject();
        dataTableObject.setAaData(violators);
        
        Gson gson = new GsonBuilder().setPrettyPrinting().create(); 
        String json = gson.toJson( dataTableObject );
        
        return json;
    }

    
    /**
     * 
     * Returns the violators of the local dns server
     * 
     * @return A response string
     * 
     *         <pre>
     * Example:
     * 
     * Request URL:
     * http://127.0.0.1:8080/dnsguard/northbound/getrecords
     * 
     * Response body in XML:
     * &lt;?xml version="1.0" encoding="UTF-8" standalone="yes"?&gt;
     * Sample Northbound API
     * 
     * Response body in JSON:
     * Sample Northbound API
     * </pre>
     */
    @Path("/getrecords")
    @GET
    @Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_JSON })
    @TypeHint(String.class)
    @StatusCodes()
    public String getRecords() {
    	
    	if (!NorthboundUtils.isAuthorized(getUserName(), "default", Privilege.READ, this)) {
            throw new UnauthorizedException("User is not authorized to perform this operation");
        }
    	
    	IDnsGuard dnshandler = (IDnsGuard) ServiceHelper.getInstance(IDnsGuard.class, "default", this);
    	
        if (dnshandler == null) {
            throw new ServiceUnavailableException("DnsHandler Service " + RestMessages.SERVICEUNAVAILABLE.toString());
        }
        
        List<DnsRecordReply> records = dnshandler.getDatabaseDnsRecords(100, 0);
        
        DataTableObject dataTableObject = new DataTableObject();
        dataTableObject.setAaData(records);
        
        Gson gson = new GsonBuilder().setPrettyPrinting().create(); 
        String json = gson.toJson( dataTableObject );
        
        return json;
    }

    /**
     * 
     * Returns the
     * 
     * @return A response string
     * 
     *         <pre>
     * Example:
     * 
     * Request URL:
     * http://127.0.0.1:8080/dnsguard/northbound/getrecords
     * 
     * Response body in XML:
     * &lt;?xml version="1.0" encoding="UTF-8" standalone="yes"?&gt;
     * Sample Northbound API
     * 
     * Response body in JSON:
     * Sample Northbound API
     * </pre>
     */
    @Path("/findrecords/")
    @POST
    @Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_JSON })
    @TypeHint(String.class)
    @StatusCodes()
    public String findRecords(@FormParam("draw") String draw, @FormParam("start") String start, @FormParam("length") String length, @FormParam("search[value]") String search) {
    	
    	if (!NorthboundUtils.isAuthorized(getUserName(), "default", Privilege.READ, this)) {
            throw new UnauthorizedException("User is not authorized to perform this operation");
        }
    	
    	IDnsGuard dnshandler = (IDnsGuard) ServiceHelper.getInstance(IDnsGuard.class, "default", this);
    	
        if (dnshandler == null) {
            throw new ServiceUnavailableException("DnsHandler Service " + RestMessages.SERVICEUNAVAILABLE.toString());
        }
        
       log.info("draw " +  draw + " start " + start + " length " + length + " search " + search);
       
       //TODO: LC, change behaivor
       List<DnsRecordReply> records = dnshandler.getDatabaseDnsRecords(100, 0);
       
       DataTableObject dataTableObject = new DataTableObject();
       dataTableObject.setAaData(records); 
       dataTableObject.setiDraw(1);
       
       Gson gson = new GsonBuilder().setPrettyPrinting().create(); 
       String json = gson.toJson( dataTableObject );
       
       return json;
        
    }
    
    /**
     * 
     * Returns the violators of the local dns server
     * 
     * @return A response string
     * 
     *         <pre>
     * Example:
     * 
     * Request URL:
     * http://localhost:8080/dnsguard/northbound/getTopExternalDnsUsage
     * 
     * Response body in XML:
     * &lt;?xml version="1.0" encoding="UTF-8" standalone="yes"?&gt;
     * Sample Northbound API
     * 
     * Response body in JSON:
     * Sample Northbound API
     * </pre>
     */
    @Path("/getTopExternalDnsUsage")
    @GET
    @Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_JSON })
    @TypeHint(String.class)
    @StatusCodes()
    public String getTopExternalDnsUsage() {
    	
    	if (!NorthboundUtils.isAuthorized(getUserName(), "default", Privilege.READ, this)) {
            throw new UnauthorizedException("User is not authorized to perform this operation");
        }
    	
    	IDnsGuard dnshandler = (IDnsGuard) ServiceHelper.getInstance(IDnsGuard.class, "default", this);
    	
        if (dnshandler == null) {
            throw new ServiceUnavailableException("DnsHandler Service " + RestMessages.SERVICEUNAVAILABLE.toString());
        }
        
        List<DnsUsage> topdns = dnshandler.getExternalDnsUsage(5);
         
        D3pieData pieDataObject = new D3pieData();
        pieDataObject.setContent(topdns);
        
        Gson gson = new GsonBuilder().setPrettyPrinting().create(); 
        String json = gson.toJson( pieDataObject );
        
        return json;
    }
    
    /***
     * 
     * @param http://127.0.0.1:8080/dnsguard/northbound/setlocaldns/{localdns}
     * @return The domains visited by an internal IP
     */
    @Path("/setlocaldns/{localdns}")
    @GET
    @Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_JSON })
    @TypeHint(String.class)
    @StatusCodes()
	public String setLocalDnsServer(@PathParam("localdns") String local_dns) {
		
		// TODO Auto-generated method stub
    	if (!NorthboundUtils.isAuthorized(getUserName(), "default", Privilege.READ, this)) {
            throw new UnauthorizedException("User is not authorized to perform this operation");
        }
    	
    	IDnsGuard dnshandler = (IDnsGuard) ServiceHelper.getInstance(IDnsGuard.class, "default", this);
    	
        if (dnshandler == null) {
            throw new ServiceUnavailableException("DnsHandler Service " + RestMessages.SERVICEUNAVAILABLE.toString());
        }


        String json = new Gson().toJson( dnshandler.setLocalDnsServer(local_dns) );
        
        return json;
	}
    
    /***
     * 
     * @param http://127.0.0.1:8080/dnsguard/northbound/getlocaldns
     * @return The domains visited by an internal IP
     */
    @Path("/getlocaldns")
    @GET
    @Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_JSON })
    @TypeHint(String.class)
    @StatusCodes()
	public String getLocalDnsServer() {
		
		// TODO Auto-generated method stub
    	if (!NorthboundUtils.isAuthorized(getUserName(), "default", Privilege.READ, this)) {
            throw new UnauthorizedException("User is not authorized to perform this operation");
        }
    	
    	IDnsGuard dnshandler = (IDnsGuard) ServiceHelper.getInstance(IDnsGuard.class, "default", this);
    	
        if (dnshandler == null) {
            throw new ServiceUnavailableException("DnsHandler Service " + RestMessages.SERVICEUNAVAILABLE.toString());
        }


        String json = new Gson().toJson( dnshandler.getLocalDnsServer() );
        
        return json;
	}
    


	@POST
	@Path("/updateconfig")
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	public String updateconfig(@FormParam("dbserver") String dbserver, @FormParam("dbport") String dbport, @FormParam("dbname") String dbname, @FormParam("dbuser") String dbuser, @FormParam("dbpasswd") String dbpasswd, @FormParam("ib_size") String ib_size, @FormParam("ib_max") String ib_max) throws Exception {
 
        String result = "<xml><output>Query from module DnsGuard  </output></xml>";
        
        return result;
	}
}
