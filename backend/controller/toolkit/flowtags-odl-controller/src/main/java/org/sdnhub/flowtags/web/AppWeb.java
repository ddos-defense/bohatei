package org.sdnhub.flowtags.web;

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
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * This entire web class can be accessed via /web prefix as specified in web.xml
 */
@Controller
@RequestMapping("/")
public class AppWeb implements IDaylightWeb {
    private static final String WEB_NAME = "Proactive App";
    private static final String WEB_ID = "proactive";
    private static final short WEB_ORDER = 2;
    private static final UserLevel AUTH_LEVEL = UserLevel.CONTAINERUSER;

    public AppWeb() {
        ServiceHelper.registerGlobalService(IDaylightWeb.class, this, null);
    }

    @RequestMapping(value = "")
    public String index(Model model, HttpServletRequest request) {
		return "main";
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
     
}
