#set( $symbol_pound = '#' )
#set( $symbol_dollar = '$' )
#set( $symbol_escape = '\' )
package ${package};

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.opendaylight.controller.sal.authorization.UserLevel;
import org.opendaylight.controller.sal.utils.ServiceHelper;
import ${groupId}.web.IDaylightWeb;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/")
public class App implements IDaylightWeb {
    private static final String WEB_NAME = "App";
    private static final String WEB_ID = "${artifactId}";
    private static final short WEB_ORDER = 1;
    private static final UserLevel AUTH_LEVEL = UserLevel.CONTAINERUSER;

    public App() {
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
