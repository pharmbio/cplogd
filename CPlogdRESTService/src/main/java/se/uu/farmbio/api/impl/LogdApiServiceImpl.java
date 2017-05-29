package se.uu.farmbio.api.impl;

import java.io.InputStream;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;

import org.glassfish.jersey.media.multipart.FormDataContentDisposition;

import se.uu.farmbio.api.ApiResponseMessage;
import se.uu.farmbio.api.LogdApiService;
import se.uu.farmbio.api.NotFoundException;
@javax.annotation.Generated(value = "io.swagger.codegen.languages.JavaJerseyServerCodegen", date = "2017-05-29T12:46:15.437Z")
public class LogdApiServiceImpl extends LogdApiService {
	@Override
	public Response logdGet( @NotNull String smiles,  @Min(0) @Max(1) Double confidence, SecurityContext securityContext) throws NotFoundException {
		// do some magic!
		return Response.ok().entity(new ApiResponseMessage(ApiResponseMessage.OK, "magic!")).build();
	}
	@Override
	public Response logdPost( String uri, InputStream dataFileInputStream, FormDataContentDisposition dataFileDetail,  @Min(0) @Max(1) Double confidence, SecurityContext securityContext) throws NotFoundException {
		// do some magic!
		return Response.ok().entity(new ApiResponseMessage(ApiResponseMessage.OK, "magic!")).build();
	}
}
