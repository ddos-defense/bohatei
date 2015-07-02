/*
 * Copyright (c) 2014 SDN Hub.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * author: Luis Chiang
 */

package org.sdnhub.dnsguard.web;

import java.util.Properties;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.codehaus.enunciate.jaxrs.StatusCodes;
import org.codehaus.enunciate.jaxrs.TypeHint;
import org.opendaylight.controller.northbound.commons.RestMessages;
import org.opendaylight.controller.northbound.commons.exception.ServiceUnavailableException;
import org.opendaylight.controller.sal.authorization.UserLevel;
import org.opendaylight.controller.sal.utils.ServiceHelper;
import org.opendaylight.toolkit.web.IDaylightWeb;
import org.sdnhub.dnsguard.IDnsGuard;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * This entire web class can be accessed via /web prefix as specified in web.xml
 */
@Controller
@RequestMapping("/")
public class AppWeb implements IDaylightWeb {
    private static final String WEB_NAME = "DNSGUARD App";
    private static final String WEB_ID = "dnsguard";
    private static final short WEB_ORDER = 2;
    private static final UserLevel AUTH_LEVEL = UserLevel.CONTAINERUSER;

    public AppWeb() {
        ServiceHelper.registerGlobalService(IDaylightWeb.class, this, null);
    }

    @RequestMapping(value = "")
    public String index(Model model, HttpServletRequest request) {
    	
    	IDnsGuard dnshandler = (IDnsGuard) ServiceHelper.getInstance(IDnsGuard.class, "default", this);
    	
        if (dnshandler == null) {
            throw new ServiceUnavailableException("DnsHandler Service " + RestMessages.SERVICEUNAVAILABLE.toString());
        }
        
    	// If there is a problem with the DB configure DB
    	if(dnshandler.isConnected())
    		return "main";
		
    	return "modconfig";
    	 
    }
    
    @RequestMapping(value = "modconfig")
    public String modconfig(Model model, HttpServletRequest request) {
    	
    	return "modconfig";
    	 
    }
    
    @RequestMapping(value = "dnsconfig")
    public String dnsconfig(Model model, HttpServletRequest request) {
    	
    	return "dnsconfig";
    	 
    }
    
    @Override
    public String getWebName() {
        return WEB_NAME;
    }
    
    @Override
    public String getWebId() {
        return WEB_ID;
    }

    @Override
    public short getWebOrder() {
        return WEB_ORDER;
    }

    @Override
    public boolean isAuthorized(UserLevel userLevel) {
        return userLevel.ordinal() <= AUTH_LEVEL.ordinal();
    }

    @RequestMapping(value = "login")
    public String login(final HttpServletRequest request, final HttpServletResponse response) {
        return "forward:" + "/";
    }
     
	@POST
	@RequestMapping(value = "updateconfig")
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	public String updateconfig(@FormParam("dbserver") String dbserver, @FormParam("dbport") String dbport, @FormParam("dbname") String dbname, @FormParam("dbuser") String dbuser, @FormParam("dbpasswd") String dbpasswd, @FormParam("ib_size") String ib_size, @FormParam("ib_max") String ib_max) throws Exception {
		
    	IDnsGuard dnshandler = (IDnsGuard) ServiceHelper.getInstance(IDnsGuard.class, "default", this);
    	
        if (dnshandler == null) {
            throw new ServiceUnavailableException("DnsHandler Service " + RestMessages.SERVICEUNAVAILABLE.toString());
        }
        
        Properties props = new Properties();
         
        props.put("dbserver", dbserver);
        props.put("dbport", dbport);
        
        props.put("dbname", dbname);
        props.put("dbuser", dbuser);
        props.put("dbpasswd", dbpasswd);
        props.put("internal_buffer_init_size", ib_size);
        props.put("internal_buffer_max_size", ib_max);
        
        try{
        	dnshandler.savePropsAndConnect(props);
        }catch(Exception ex){
        	
        }
        
    	// If there is a problem with the DB configure DB
    	if(dnshandler.isConnected())
    		return "main";
		
    	return "modconfig";
	}
	
	@POST
	@RequestMapping(value = "updatedns")
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	public String updatedns(@FormParam("localdns") String localdns) throws Exception {
		
    	IDnsGuard dnshandler = (IDnsGuard) ServiceHelper.getInstance(IDnsGuard.class, "default", this);
    	
        if (dnshandler == null) {
            throw new ServiceUnavailableException("DnsHandler Service " + RestMessages.SERVICEUNAVAILABLE.toString());
        }
        
        // needed more validation
        dnshandler.setLocalDnsServer(localdns);
        
        return "main";
	}
	 
}
