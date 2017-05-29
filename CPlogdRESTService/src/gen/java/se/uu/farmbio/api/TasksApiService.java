package se.uu.farmbio.api;

import org.glassfish.jersey.media.multipart.FormDataContentDisposition;

import se.uu.farmbio.api.*;
import se.uu.farmbio.model.*;

import java.util.List;
import java.io.InputStream;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import javax.validation.constraints.*;
@javax.annotation.Generated(value = "io.swagger.codegen.languages.JavaJerseyServerCodegen", date = "2017-05-29T12:46:15.437Z")
public abstract class TasksApiService {
    public abstract Response tasksGet(SecurityContext securityContext) throws NotFoundException;
    public abstract Response tasksIdGet(String id,SecurityContext securityContext) throws NotFoundException;
}
