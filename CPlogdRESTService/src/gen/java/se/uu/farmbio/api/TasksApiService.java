package se.uu.farmbio.api;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
@javax.annotation.Generated(value = "io.swagger.codegen.languages.JavaJerseyServerCodegen", date = "2017-05-29T12:46:15.437Z")
public abstract class TasksApiService {
	public abstract Response tasksGet(SecurityContext securityContext) throws NotFoundException;
	public abstract Response tasksIdGet(String id,SecurityContext securityContext) throws NotFoundException;
}
