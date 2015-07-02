#set( $symbol_pound = '#' )
#set( $symbol_dollar = '$' )
#set( $symbol_escape = '\' )
package ${package};

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.opendaylight.controller.sal.authorization.UserLevel;
import org.opendaylight.controller.sal.core.Edge;
import org.opendaylight.controller.sal.core.Node;
import org.opendaylight.controller.sal.utils.GlobalConstants;
import org.opendaylight.controller.sal.utils.ServiceHelper;
import org.opendaylight.controller.topologymanager.ITopologyManager;
import org.opendaylight.phoenix.web.IDaylightWeb;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping("/")
public class App implements IDaylightWeb {
    private static final String WEB_NAME = "${artifactId}";
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

    @RequestMapping(value = "/visual.json", method = RequestMethod.GET)
    @ResponseBody
    public Map<String, Object> getLinkData(HttpServletRequest request) {
        Map<String, Object> result = new HashMap<String, Object>();

        ITopologyManager topologyManager = (ITopologyManager) ServiceHelper.getInstance(ITopologyManager.class,
                GlobalConstants.DEFAULT.toString(), this);
        if (topologyManager == null) {
            return result;
        }

        Map<Node, Set<Edge>> nodeEdges = topologyManager.getNodeEdges();
        List<Node> globalNodes = new ArrayList<Node>(nodeEdges.keySet());

        result.put("directed", false);
        result.put("multigraph", false);
        result.put("graph", new ArrayList());

        List<Map<String, Object>> nodes = new ArrayList<Map<String, Object>>();
        result.put("nodes", nodes);

        List<Map<String, Integer>> links = new ArrayList<Map<String, Integer>>();
        result.put("links", links);

        for (Node n : globalNodes) {
            Map<String, Object> node = new HashMap<String, Object>();
            node.put("id", n.toString());
            node.put("fixed", true);
            nodes.add(node);

            for (Edge edge : nodeEdges.get(n)) {
                Map<String, Integer> e = new HashMap<String, Integer>();

                e.put("source", globalNodes.indexOf(edge.getHeadNodeConnector().getNode()));
                e.put("target", globalNodes.indexOf(edge.getTailNodeConnector().getNode()));
                links.add(e);
            }
        }

        return result;
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
