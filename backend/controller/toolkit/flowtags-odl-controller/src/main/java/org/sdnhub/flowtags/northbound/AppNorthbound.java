
package org.sdnhub.flowtags.northbound;

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
import org.sdnhub.flowtags.Flowtags;
import org.sdnhub.flowtags.IFlowtags;
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
    	
    	IFlowtags proactive = (IFlowtags) ServiceHelper.getInstance(IFlowtags.class, "default", this);
    	
        if (proactive == null) {
            throw new ServiceUnavailableException("ProactiveHandler Service " + RestMessages.SERVICEUNAVAILABLE.toString());
        }

        String result = "<xml><output>Query from module Proactive " + proactive.echo(echo)  + " </output></xml>";
        
        return result;

   }
    
}
