#set( $symbol_pound = '#' )

package ${package}.northbound;

import ${package}.ISimple;
import ${package}.SimpleData;
import org.codehaus.enunciate.jaxrs.StatusCodes;
import org.codehaus.enunciate.jaxrs.TypeHint;
import org.opendaylight.controller.sal.utils.ServiceHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
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

import org.codehaus.enunciate.jaxrs.ResponseCode;
import org.opendaylight.controller.northbound.commons.RestMessages;
import org.opendaylight.controller.northbound.commons.exception.ServiceUnavailableException;
import org.opendaylight.controller.northbound.commons.exception.UnauthorizedException;
import org.opendaylight.controller.northbound.commons.utils.NorthboundUtils;
import org.opendaylight.controller.sal.authorization.Privilege;
import org.opendaylight.controller.sal.utils.Status;

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
     * http://localhost:8080/app/northbound/${REST-Resource-Name}
     *
     * Response body in XML:
     * &lt;?xml version="1.0" encoding="UTF-8" standalone="yes"?&gt;
     * Sample Northbound API
     *
     * Response body in JSON:
     * Sample Northbound API
     * </pre>
     */
    @Path("/${REST-Resource-Name}")
    @GET
    @Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
    @StatusCodes()
    public List<SimpleData> getData() {
        if (!NorthboundUtils.isAuthorized(getUserName(), "default", Privilege.WRITE, this)) {
            throw new UnauthorizedException("User is not authorized to perform this operation");
        }
        ISimple simple = (ISimple) ServiceHelper.getGlobalInstance(ISimple.class, this);
        if (simple == null) {
            throw new ServiceUnavailableException("Simple Service " + RestMessages.SERVICEUNAVAILABLE.toString());
        }

        Map<UUID, SimpleData> sDataMap = simple.readData();
        if (sDataMap != null) {
            return new ArrayList<SimpleData>(sDataMap.values());
        }
        return new ArrayList<SimpleData>();
    }

    @Path("/${REST-Resource-Name}/{uuid}")
    @GET
    @Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
    @TypeHint(SimpleData.class)
    @StatusCodes()
    public SimpleData getData(@PathParam("uuid") String uuid) {
        if (!NorthboundUtils.isAuthorized(getUserName(), "default", Privilege.WRITE, this)) {
            throw new UnauthorizedException("User is not authorized to perform this operation");
        }
        ISimple simple = (ISimple) ServiceHelper.getGlobalInstance(ISimple.class, this);
        if (simple == null) {
            throw new ServiceUnavailableException("Simple Service " + RestMessages.SERVICEUNAVAILABLE.toString());
        }

        return simple.readData(UUID.fromString(uuid));
    }

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
     * http://localhost:8080/app/northbound/${REST-Resource-Name}
     *
     * Response body in XML:
     * &lt;?xml version="1.0" encoding="UTF-8" standalone="yes"?&gt;
     * Sample Northbound API
     *
     * Response body in JSON:
     * Sample Northbound API
     * </pre>
     */
    @Path("/${REST-Resource-Name}")
    @POST
    @StatusCodes({ @ResponseCode(code = 201, condition = "Data Inserted successfully"),
        @ResponseCode(code = 401, condition = "User not authorized to perform this operation"),
        @ResponseCode(code = 500, condition = "Error inserting data"),
        @ResponseCode(code = 503, condition = "One or more of service is unavailable")})
    @Consumes({ MediaType.APPLICATION_JSON})
    public Response createData(@TypeHint(SimpleData.class) SimpleData data) {
        if (!NorthboundUtils.isAuthorized(getUserName(), "default", Privilege.WRITE, this)) {
            throw new UnauthorizedException("User is not authorized to perform this operation");
        }
        ISimple simple = (ISimple) ServiceHelper.getGlobalInstance(ISimple.class, this);
        if (simple == null) {
            throw new ServiceUnavailableException("Simple Service " + RestMessages.SERVICEUNAVAILABLE.toString());
        }
        
        UUID uuid = simple.createData(data);
        if (uuid == null) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
        return Response.status(Response.Status.CREATED)
                .header("Location", String.format("%s/%s", _uriInfo.getAbsolutePath().toString(),
                                                            uuid.toString()))
                .entity(uuid.toString())
                .build();
    }

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
    * http://localhost:8080/app/northbound/${REST-Resource-Name}/{uuid}
    *
    * Response body in XML:
    * &lt;?xml version="1.0" encoding="UTF-8" standalone="yes"?&gt;
    * Sample Northbound API
    *
    * Response body in JSON:
    * Sample Northbound API
    * </pre>
    */
   @Path("/${REST-Resource-Name}/{uuid}")
   @PUT
   @StatusCodes({ @ResponseCode(code = 200, condition = "Data Updated successfully"),
       @ResponseCode(code = 401, condition = "User not authorized to perform this operation"),
       @ResponseCode(code = 500, condition = "Error updating data"),
       @ResponseCode(code = 503, condition = "One or more of service is unavailable")})
   @Consumes({ MediaType.APPLICATION_JSON})
   public Response updateData(@PathParam("uuid") String uuid, @TypeHint(SimpleData.class) SimpleData data) {
       if (!NorthboundUtils.isAuthorized(getUserName(), "default", Privilege.WRITE, this)) {
           throw new UnauthorizedException("User is not authorized to perform this operation");
       }
       ISimple simple = (ISimple) ServiceHelper.getGlobalInstance(ISimple.class, this);
       if (simple == null) {
           throw new ServiceUnavailableException("Simple Service " + RestMessages.SERVICEUNAVAILABLE.toString());
       }
       
       Status status = simple.updateData(UUID.fromString(uuid), data);
       if (!status.isSuccess()) {
           return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
       }
       return Response.status(Response.Status.OK).build();
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
   * http://localhost:8080/app/northbound/${REST-Resource-Name}/{uuid}
   *
   * Response body in XML:
   * &lt;?xml version="1.0" encoding="UTF-8" standalone="yes"?&gt;
   * Sample Northbound API
   *
   * Response body in JSON:
   * Sample Northbound API
   * </pre>
   */
  @Path("/${REST-Resource-Name}/{uuid}")
  @DELETE
  @StatusCodes({ @ResponseCode(code = 200, condition = "Data Deleted successfully"),
                 @ResponseCode(code = 401, condition = "User not authorized to perform this operation"),
                 @ResponseCode(code = 500, condition = "Error deleting data"),
                 @ResponseCode(code = 503, condition = "One or more of service is unavailable")})
  @Consumes({ MediaType.APPLICATION_JSON})
  public Response updateData(@PathParam("uuid") String uuid) {
      if (!NorthboundUtils.isAuthorized(getUserName(), "default", Privilege.WRITE, this)) {
          throw new UnauthorizedException("User is not authorized to perform this operation");
      }
      ISimple simple = (ISimple) ServiceHelper.getGlobalInstance(ISimple.class, this);
      if (simple == null) {
          throw new ServiceUnavailableException("Simple Service " + RestMessages.SERVICEUNAVAILABLE.toString());
      }
      
      Status status = simple.deleteData(UUID.fromString(uuid));
      if (!status.isSuccess()) {
          return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
      }
      return Response.status(Response.Status.OK).build();
  }

}
