package org.opendaylight.phoenix.app.web;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.opendaylight.controller.sal.authorization.UserLevel;
import org.opendaylight.controller.sal.utils.ServiceHelper;
import org.opendaylight.phoenix.web.IDaylightWeb;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * This entire web class can be accessed via /web prefix as specified in web.xml
 */
@Controller
@RequestMapping("/")
public class AppWeb implements IDaylightWeb {
    private static final String WEB_NAME = "Devices App";
    private static final String WEB_ID = "device";
    private static final short WEB_ORDER = 1;
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
