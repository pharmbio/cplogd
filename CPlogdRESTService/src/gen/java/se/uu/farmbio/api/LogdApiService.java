package se.uu.farmbio.api;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
@javax.annotation.Generated(value = "io.swagger.codegen.languages.JavaJerseyServerCodegen", date = "2017-05-29T12:46:15.437Z")
public abstract class LogdApiService {
	public abstract Response logdGet( @NotNull String smiles, @Min(0) @Max(1) Double confidence,SecurityContext securityContext) throws NotFoundException;
	public abstract Response logdImageGet( @NotNull String smiles, @Max(5000) int imageWidth, @Max(5000) int imageHeight, SecurityContext securityContext) throws NotFoundException;
//	public abstract Response logdPostURI(String uri, @Min(0) @Max(1) Double confidence,SecurityContext securityContext) throws NotFoundException;
//	public abstract Response logdPostFile(InputStream dataFileInputStream, FormDataContentDisposition dataFileDetail, @Min(0) @Max(1) Double confidence,SecurityContext securityContext) throws NotFoundException;
}
