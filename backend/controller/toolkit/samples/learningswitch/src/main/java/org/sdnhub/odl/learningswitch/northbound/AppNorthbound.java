
package org.sdnhub.odl.learningswitch.northbound;

import org.sdnhub.odl.learningswitch.ILearningSwitch;
import org.sdnhub.odl.learningswitch.Table;
import org.sdnhub.odl.learningswitch.MacTable.MacPortTableElem;
import org.sdnhub.odl.learningswitch.internal.LearningSwitch;
import org.codehaus.enunciate.jaxrs.StatusCodes;
import org.codehaus.enunciate.jaxrs.TypeHint;
import org.opendaylight.controller.sal.reader.IReadService;
import org.opendaylight.controller.sal.utils.ServiceHelper;
import org.opendaylight.controller.sal.utils.HexEncode;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.core.UriInfo;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.codehaus.enunciate.jaxrs.ResponseCode;
import org.opendaylight.controller.northbound.commons.RestMessages;
import org.opendaylight.controller.northbound.commons.exception.ServiceUnavailableException;
import org.opendaylight.controller.northbound.commons.exception.UnauthorizedException;
import org.opendaylight.controller.northbound.commons.utils.NorthboundUtils;
import org.opendaylight.controller.sal.authorization.Privilege;
import org.opendaylight.controller.sal.core.NodeConnector;
import org.opendaylight.controller.sal.utils.Status;
import org.opendaylight.controller.switchmanager.ISwitchManager;
import org.opendaylight.controller.forwardingrulesmanager.FlowConfig;
import org.opendaylight.controller.forwardingrulesmanager.IForwardingRulesManager;
import org.opendaylight.controller.switchmanager.ISwitchManager;
import org.opendaylight.controller.switchmanager.Switch;
import org.opendaylight.controller.sal.core.Node;
import org.opendaylight.controller.sal.core.Description;
import org.opendaylight.controller.sal.core.Name;
import org.opendaylight.controller.switchmanager.SwitchConfig;
import org.opendaylight.controller.sal.reader.FlowOnNode;




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



	@XmlRootElement(name="SwitchFunction")
	class SwitchFunction {
		@XmlElement(name="function")
		String function;
		public SwitchFunction(String fn) {
			function = fn;
		}
	}

	/**
	 *
	 * Get current function (hub or switch) - GET REST API call
	 *
	 * @return A response string
	 *
	 * <pre>
	 * Example:
	 *
	 * Request URL:
	 * http://localhost:8080/app/northbound/learningswitch/function/
	 *
	 * Response body in JSON:
	 * { "function" : "hub"}
	 * </pre>
	 */
	@Path("/learningswitch/function/")
	@GET
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	@StatusCodes()   
	public SwitchFunction getFunction() {
		if (!NorthboundUtils.isAuthorized(getUserName(), "default", Privilege.WRITE, this)) {
			throw new UnauthorizedException("User is not authorized to perform this operation");
		}
		//LearningSwitch simple = (LearningSwitch) ServiceHelper.getInstance(LearningSwitch.class, "default", this);
		ILearningSwitch simple = (ILearningSwitch) ServiceHelper.getInstance(ILearningSwitch.class, "default", this);
		if (simple == null) {
			throw new ServiceUnavailableException("LearningSwitch Service " + RestMessages.SERVICEUNAVAILABLE.toString());
		}

		SwitchFunction sf = new SwitchFunction(simple.getFunction());
		return sf;
	}

	/**
	 *
	 * Set current function (hub or switch) - PUT REST API call
	 *
	 * @return Response
	 *
	 * <pre>
	 * Example:
	 *
	 * Request URL:
	 * http://localhost:8080/app/northbound/learningswitch/function
	 *
	 * </pre>
	 */
	@Path("/learningswitch/function/")
	@PUT
	@Consumes({ MediaType.APPLICATION_JSON})
	@StatusCodes()
	public Response setFunction(@TypeHint(SwitchFunction.class) SwitchFunction fn) {
		if (!NorthboundUtils.isAuthorized(getUserName(), "default", Privilege.WRITE, this)) {
			throw new UnauthorizedException("User is not authorized to perform this operation");
		}
		ILearningSwitch simple = (ILearningSwitch) ServiceHelper.getInstance(ILearningSwitch.class, "default", this);
		if (simple == null) {
			throw new ServiceUnavailableException("LearningSwitch Service " + RestMessages.SERVICEUNAVAILABLE.toString());
		}

		if (simple.setFunction(fn.function) == true) {
			return Response.status(Response.Status.OK).build();
		} else {
			return Response.status(Response.Status.NOT_FOUND).build();
		}
	}



	/**
	 *
	 * Sample GET REST API call
	 *
	 * @return A response string
	 *
	 * <pre>
	 * Example:
	 *
	 * Request URL:
	 * http://localhost:8080/app/northbound/learningswitch/mactable
	 *
	 * Response body in XML:
	 * &lt;?xml version="1.0" encoding="UTF-8" standalone="yes"?&gt;
	 * Sample Northbound API
	 *
	 * Response body in JSON:
	 * Sample Northbound API
	 * </pre>
	 */
	@Path("/learningswitch/table")
	@GET
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	@StatusCodes()
	public Table  getTableEntries() {
		if (!NorthboundUtils.isAuthorized(getUserName(), "default", Privilege.WRITE, this)) {
			throw new UnauthorizedException("User is not authorized to perform this operation");
		}
		//LearningSwitch simple = (LearningSwitch) ServiceHelper.getInstance(LearningSwitch.class, "default", this);
		ILearningSwitch simple = (ILearningSwitch) ServiceHelper.getInstance(ILearningSwitch.class, "default", this);
		if (simple == null) {
			throw new ServiceUnavailableException("Simple Service " + RestMessages.SERVICEUNAVAILABLE.toString());
		}

		return simple.getData();
	}


	/**
	 *
	 * Sample Delete REST API call
	 *
	 * @return A response string
	 *
	 *         <pre>
	 * Example:
	 *
	 * Request URL:
	 * http://localhost:8080/app/northbound/learningswitch/{uuid}
	 *
	 * Response body in XML:
	 * &lt;?xml version="1.0" encoding="UTF-8" standalone="yes"?&gt;
	 * Sample Northbound API
	 *
	 * Response body in JSON:
	 * Sample Northbound API
	 * </pre>
	 */
	@Path("/learningswitch/table")
	@DELETE
	@StatusCodes({ @ResponseCode(code = 200, condition = "Data Deleted successfully"),
		@ResponseCode(code = 401, condition = "User not authorized to perform this operation"),
		@ResponseCode(code = 500, condition = "Error deleting data"),
		@ResponseCode(code = 503, condition = "One or more of service is unavailable")})
	@Consumes({ MediaType.APPLICATION_JSON})
	public Response deleteTableEntries() {
		if (!NorthboundUtils.isAuthorized(getUserName(), "default", Privilege.WRITE, this)) {
			throw new UnauthorizedException("User is not authorized to perform this operation");
		}
		ILearningSwitch simple = (ILearningSwitch) ServiceHelper.getGlobalInstance(ILearningSwitch.class, this);
		if (simple == null) {
			throw new ServiceUnavailableException("Simple Service " + RestMessages.SERVICEUNAVAILABLE.toString());
		}

		simple.deleteData();
		return Response.status(Response.Status.OK).build();
	}


	@XmlRootElement(name="NodeToFlowEntries")
	class NodeToFlowEntries {
		@XmlElement(name="node")
		String node;
		@XmlElement(name="flows")
		List <String> flows;
		public NodeToFlowEntries() {
			flows = new ArrayList<String>();
		}
	}

	@Path("/learningswitch/flowtable")
	@GET
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	@StatusCodes()
	public List<NodeToFlowEntries> getNodeFlows() {
		if (!NorthboundUtils.isAuthorized(getUserName(), "default", Privilege.WRITE, this)) {
			throw new UnauthorizedException("User is not authorized to perform this operation");
		}
		ISwitchManager switchManager = (ISwitchManager) ServiceHelper.getInstance(ISwitchManager.class, "default",
				this);
		if (switchManager == null) {
			return null;
		}   
		IForwardingRulesManager frm = (IForwardingRulesManager) ServiceHelper.getInstance(
				IForwardingRulesManager.class, "default", this);
		if (frm == null) {
			return null;
		}   

		IReadService rds = (IReadService) ServiceHelper.getInstance(IReadService.class, "default", this);

		List< NodeToFlowEntries > output = new ArrayList<NodeToFlowEntries>();
		//Map<String, Object> output = new HashMap<String, Object>(2);

		for (Switch sw : switchManager.getNetworkDevices()) {
			Node node = sw.getNode();
			NodeToFlowEntries nfentries = new NodeToFlowEntries();

			nfentries.node = node.toString();

			//List<FlowConfig> staticFlowList = frm.getStaticFlows(node);
			List<FlowOnNode> flowList = rds.readAllFlows(node);
			for (FlowOnNode flow : flowList) {
				nfentries.flows.add(flow.getFlow().toString());
			}   
			output.add(nfentries);
		}
		return output;
	}

	private String getNodeDesc(Node node, ISwitchManager switchManager) {
		Description desc = (Description) switchManager.getNodeProp(node, Description.propertyName);
		String description = (desc == null) ? "" : desc.getValue();
		return (description.isEmpty() || description.equalsIgnoreCase("none")) ? node.toString() : description;
	}



	//    @Path("/learningswitch/{uuid}")
	//    @GET
	//    @Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	//    @TypeHint(MacToPortTable.class)
	//    @StatusCodes()
	//    public MacToPortTable getData(@PathParam("uuid") String uuid) {
	//        if (!NorthboundUtils.isAuthorized(getUserName(), "default", Privilege.WRITE, this)) {
	//            throw new UnauthorizedException("User is not authorized to perform this operation");
	//        }
	//        ILearningSwitch simple = (ILearningSwitch) ServiceHelper.getGlobalInstance(ILearningSwitch.class, this);
	//        if (simple == null) {
	//            throw new ServiceUnavailableException("Simple Service " + RestMessages.SERVICEUNAVAILABLE.toString());
	//        }
	//
	//        return simple.readData(UUID.fromString(uuid));
	//    }

	/**
	 *
	 * Sample POST REST API call
	 *
	 * @return A response string
	 *
	 *         <pre>
	 * Example:
	 *
	 * Request URL:
	 * http://localhost:8080/app/northbound/learningswitch
	 *
	 * Response body in XML:
	 * &lt;?xml version="1.0" encoding="UTF-8" standalone="yes"?&gt;
	 * Sample Northbound API
	 *
	 * Response body in JSON:
	 * Sample Northbound API
	 * </pre>
	 */
	//    @Path("/learningswitch")
	//    @POST
	//    @StatusCodes({ @ResponseCode(code = 201, condition = "Data Inserted successfully"),
	//        @ResponseCode(code = 401, condition = "User not authorized to perform this operation"),
	//        @ResponseCode(code = 500, condition = "Error inserting data"),
	//        @ResponseCode(code = 503, condition = "One or more of service is unavailable")})
	//    @Consumes({ MediaType.APPLICATION_JSON})
	//    public Response createData(@TypeHint(MacToPortTable.class) MacToPortTable data) {
	//        if (!NorthboundUtils.isAuthorized(getUserName(), "default", Privilege.WRITE, this)) {
	//            throw new UnauthorizedException("User is not authorized to perform this operation");
	//        }	
	//        ILearningSwitch simple = (ILearningSwitch) ServiceHelper.getGlobalInstance(ILearningSwitch.class, this);
	//        if (simple == null) {
	//            throw new ServiceUnavailableException("Simple Service " + RestMessages.SERVICEUNAVAILABLE.toString());
	//        }
	//        
	//        UUID uuid = simple.createData(data);
	//        if (uuid == null) {
	//            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
	//        }
	//        return Response.status(Response.Status.CREATED)
	//                .header("Location", String.format("%s/%s", _uriInfo.getAbsolutePath().toString(),
	//                                                            uuid.toString()))
	//                .entity(uuid.toString())
	//                .build();
	//    }

	/**
	 *
	 * Sample PUT REST API call
	 *
	 * @return A response string
	 *
	 *         <pre>
	 * Example:
	 *
	 * Request URL:
	 * http://localhost:8080/app/northbound/learningswitch/{uuid}
	 *
	 * Response body in XML:
	 * &lt;?xml version="1.0" encoding="UTF-8" standalone="yes"?&gt;
	 * Sample Northbound API
	 *
	 * Response body in JSON:
	 * Sample Northbound API
	 * </pre>
	 */
	//   @Path("/learningswitch/{uuid}")
	//   @PUT
	//   @StatusCodes({ @ResponseCode(code = 200, condition = "Data Updated successfully"),
	//       @ResponseCode(code = 401, condition = "User not authorized to perform this operation"),
	//       @ResponseCode(code = 500, condition = "Error updating data"),
	//       @ResponseCode(code = 503, condition = "One or more of service is unavailable")})
	//   @Consumes({ MediaType.APPLICATION_JSON})
	//   public Response updateData(@PathParam("uuid") String uuid, @TypeHint(MacToPortTable.class) MacToPortTable data) {
	//       if (!NorthboundUtils.isAuthorized(getUserName(), "default", Privilege.WRITE, this)) {
	//           throw new UnauthorizedException("User is not authorized to perform this operation");
	//       }
	//       ILearningSwitch simple = (ILearningSwitch) ServiceHelper.getGlobalInstance(ILearningSwitch.class, this);
	//       if (simple == null) {
	//           throw new ServiceUnavailableException("Simple Service " + RestMessages.SERVICEUNAVAILABLE.toString());
	//       }
	//       
	//       Status status = simple.updateData(UUID.fromString(uuid), data);
	//       if (!status.isSuccess()) {
	//           return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
	//       }
	//       return Response.status(Response.Status.OK).build();
	//   }

	/**
	 *
	 * Sample Delete REST API call
	 *
	 * @return A response string
	 *
	 *         <pre>
	 * Example:
	 *
	 * Request URL:
	 * http://localhost:8080/app/northbound/learningswitch/{uuid}
	 *
	 * Response body in XML:
	 * &lt;?xml version="1.0" encoding="UTF-8" standalone="yes"?&gt;
	 * Sample Northbound API
	 *
	 * Response body in JSON:
	 * Sample Northbound API
	 * </pre>
	 */
	//  @Path("/learningswitch/{uuid}")
	//  @DELETE
	//  @StatusCodes({ @ResponseCode(code = 200, condition = "Data Deleted successfully"),
	//                 @ResponseCode(code = 401, condition = "User not authorized to perform this operation"),
	//                 @ResponseCode(code = 500, condition = "Error deleting data"),
	//                 @ResponseCode(code = 503, condition = "One or more of service is unavailable")})
	//  @Consumes({ MediaType.APPLICATION_JSON})
	//  public Response updateData(@PathParam("uuid") String uuid) {
	//      if (!NorthboundUtils.isAuthorized(getUserName(), "default", Privilege.WRITE, this)) {
	//          throw new UnauthorizedException("User is not authorized to perform this operation");
	//      }
	//      ILearningSwitch simple = (ILearningSwitch) ServiceHelper.getGlobalInstance(ILearningSwitch.class, this);
	//      if (simple == null) {
	//          throw new ServiceUnavailableException("Simple Service " + RestMessages.SERVICEUNAVAILABLE.toString());
	//      }
	//      
	//      Status status = simple.deleteData(UUID.fromString(uuid));
	//      if (!status.isSuccess()) {
	//          return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
	//      }
	//      return Response.status(Response.Status.OK).build();
	//  }

}
