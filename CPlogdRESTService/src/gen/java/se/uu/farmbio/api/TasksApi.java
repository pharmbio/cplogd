package se.uu.farmbio.api;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;

import io.swagger.annotations.ApiParam;
import se.uu.farmbio.api.factories.TasksApiServiceFactory;
import se.uu.farmbio.models.BadRequestError;
import se.uu.farmbio.models.TaskInfo;

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
	@io.swagger.annotations.ApiOperation(value = "<b>Internal Usage only</b>Get a list of all available tasks URIs", notes = "Get a list of all available tasks URIs", response = String.class, responseContainer = "List", tags={ "Task", })
	@io.swagger.annotations.ApiResponses(value = { 
			@io.swagger.annotations.ApiResponse(code = 200, message = "list of running tasks", response = String.class, responseContainer = "List"),

			@io.swagger.annotations.ApiResponse(code = 500, message = "Server error", response = Error.class) }) //, responseContainer = "List") })
	public Response tasksGet(@Context SecurityContext securityContext)
			throws NotFoundException {
		return delegate.tasksGet(securityContext);
	}
	@GET
	@Path("/{id}")
	@Consumes({ "multipart/form-data" })
	@Produces({ "application/json" })
	@io.swagger.annotations.ApiOperation(value = "Get status of a specific task", notes = "Get status of a specific task", response = void.class, tags={ "Task", })
	@io.swagger.annotations.ApiResponses(value = { 
			@io.swagger.annotations.ApiResponse(code = 202, message = "Task is running", response = TaskInfo.class),

			@io.swagger.annotations.ApiResponse(code = 301, message = "Prediction has successfully finished", response = String.class),
			
			@io.swagger.annotations.ApiResponse(code = 400, message = "Bad request (malformatted id)", response = BadRequestError.class),
			
			@io.swagger.annotations.ApiResponse(code = 404, message = "Task for id not found", response = Error.class),

			@io.swagger.annotations.ApiResponse(code = 502, message = "Task has failed", response = Error.class) })
	public Response tasksIdGet(@ApiParam(value = "The Task URI to query",required=true) @PathParam("id") String id
			,@Context SecurityContext securityContext)
					throws NotFoundException {
		return delegate.tasksIdGet(id,securityContext);
	}
}
