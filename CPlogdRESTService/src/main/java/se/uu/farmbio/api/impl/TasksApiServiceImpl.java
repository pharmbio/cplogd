package se.uu.farmbio.api.impl;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;

import se.uu.farmbio.api.NotFoundException;
import se.uu.farmbio.api.TasksApiService;
import se.uu.farmbio.api.responses.ResponseFactory;
@javax.annotation.Generated(value = "io.swagger.codegen.languages.JavaJerseyServerCodegen", date = "2017-05-29T12:46:15.437Z")
public class TasksApiServiceImpl extends TasksApiService {
	
	// TODO - remove or make password-protected for internal usage only?
	@Override
	public Response tasksGet(SecurityContext securityContext) throws NotFoundException {
		// do some magic!
		//		return Response.ok().entity(new ApiResponseMessage(ApiResponseMessage.OK, "magic!")).build();

		// Connect to backend to list all running tasks - TODO
		return ResponseFactory
				.errorResponse(500, "Not implemented yet");
	}
	@Override
	public Response tasksIdGet(String id, SecurityContext securityContext) throws NotFoundException {
		try {

			if (id== null || id.isEmpty()) {
				return ResponseFactory.badRequestResponse(400, "missing or malformatted id parameter", "id");
			}

			// Try to get info from backend for given id TODO


			// If no info - task not found
			return ResponseFactory.errorResponse(404, "Task for id not found");
		} catch (Exception e) {
			return ResponseFactory.errorResponse(500, "Server Error - " + e.getMessage());
		}
	}
}
