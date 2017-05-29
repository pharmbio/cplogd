package se.uu.farmbio.api.impl;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;

import se.uu.farmbio.api.ApiResponseMessage;
import se.uu.farmbio.api.NotFoundException;
import se.uu.farmbio.api.TasksApiService;
@javax.annotation.Generated(value = "io.swagger.codegen.languages.JavaJerseyServerCodegen", date = "2017-05-29T12:46:15.437Z")
public class TasksApiServiceImpl extends TasksApiService {
	@Override
	public Response tasksGet(SecurityContext securityContext) throws NotFoundException {
		// do some magic!
		return Response.ok().entity(new ApiResponseMessage(ApiResponseMessage.OK, "magic!")).build();
	}
	@Override
	public Response tasksIdGet(String id, SecurityContext securityContext) throws NotFoundException {
		// do some magic!
		return Response.ok().entity(new ApiResponseMessage(ApiResponseMessage.OK, "magic!")).build();
	}
}
