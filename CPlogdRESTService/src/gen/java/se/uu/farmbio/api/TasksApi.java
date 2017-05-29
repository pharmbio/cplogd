package se.uu.farmbio.api;

import io.swagger.annotations.ApiParam;
import io.swagger.jaxrs.*;
import se.uu.farmbio.api.NotFoundException;
import se.uu.farmbio.api.TasksApiService;
import se.uu.farmbio.api.factories.TasksApiServiceFactory;
import se.uu.farmbio.model.*;

import java.util.List;
import java.io.InputStream;

import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataParam;

import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.*;
import javax.validation.constraints.*;

@Path("/tasks")
@Consumes({ "multipart/form-data" })
@Produces({ "application/json" })
@io.swagger.annotations.Api(description = "the tasks API")
@javax.annotation.Generated(value = "io.swagger.codegen.languages.JavaJerseyServerCodegen", date = "2017-05-29T12:46:15.437Z")
public class TasksApi  {
   private final TasksApiService delegate = TasksApiServiceFactory.getTasksApi();

    @GET
    
    @Consumes({ "multipart/form-data" })
    @Produces({ "application/json" })
    @io.swagger.annotations.ApiOperation(value = "Get a list of all available tasks URIs", notes = "Get a list of all available tasks URIs", response = String.class, responseContainer = "List", tags={ "Task", })
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 200, message = "list of running tasks", response = String.class, responseContainer = "List"),
        
        @io.swagger.annotations.ApiResponse(code = 500, message = "Server error", response = String.class, responseContainer = "List") })
    public Response tasksGet(@Context SecurityContext securityContext)
    throws NotFoundException {
        return delegate.tasksGet(securityContext);
    }
    @GET
    @Path("/{id}")
    @Consumes({ "multipart/form-data" })
    @Produces({ "application/json" })
    @io.swagger.annotations.ApiOperation(value = "Get status of a specific task", notes = "Get status of a specific task", response = InlineResponse202.class, tags={ "Task", })
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 202, message = "Task is running", response = InlineResponse202.class),
        
        @io.swagger.annotations.ApiResponse(code = 301, message = "Prediction has successfully finished", response = InlineResponse202.class),
        
        @io.swagger.annotations.ApiResponse(code = 404, message = "Task for id not found", response = InlineResponse202.class),
        
        @io.swagger.annotations.ApiResponse(code = 502, message = "Task has failed", response = InlineResponse202.class) })
    public Response tasksIdGet(@ApiParam(value = "The Task URI to query",required=true) @PathParam("id") String id
,@Context SecurityContext securityContext)
    throws NotFoundException {
        return delegate.tasksIdGet(id,securityContext);
    }
}
