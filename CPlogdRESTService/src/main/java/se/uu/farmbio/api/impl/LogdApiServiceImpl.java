package se.uu.farmbio.api.impl;

import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;

import org.glassfish.jersey.media.multipart.FormDataContentDisposition;

import se.uu.farmbio.api.LogdApiService;
import se.uu.farmbio.api.NotFoundException;
import se.uu.farmbio.api.predict.Predict;
import se.uu.farmbio.api.predict.Utils;
import se.uu.farmbio.api.responses.ResponseFactory;
@javax.annotation.Generated(value = "io.swagger.codegen.languages.JavaJerseyServerCodegen", date = "2017-05-29T12:46:15.437Z")
public class LogdApiServiceImpl extends LogdApiService {
	
	@Override
	public Response logdGet( @NotNull String smiles,  @Min(0) @Max(1) Double confidence, SecurityContext securityContext) throws NotFoundException {
		return Predict.doSinglePredict(smiles, confidence);
	}
	
//	@Override
//	public Response logdPostURI( String uri, InputStream dataFileInputStream, FormDataContentDisposition dataFileDetail,  @Min(0) @Max(1) Double confidence, SecurityContext securityContext) throws NotFoundException {
//		System.out.println("Got a POST to /logd ");
//		
//		if(uri!= null && dataFileInputStream != null){
//			return ResponseFactory.badRequestResponse(400, "Cannot send both uri and POST a file", Arrays.asList("uri", "dataFile"));
//		} else if (uri != null) {
//			uri = Utils.cleanURI(uri);
//			URI checkedURI = null;
//			try{
//				checkedURI = new URI(uri);
//			} catch (URISyntaxException e) {
//				return ResponseFactory.badRequestResponse(400, "URI malformatted", "uri");
//			}
//			
//			return Predict.doUriPredict(checkedURI, confidence);
//		} else {
//			return Predict.doFilePredict(dataFileInputStream, confidence);
//		}
//
//	}

//	@Override
	public Response logdPostURI(String uri, Double confidence, SecurityContext securityContext)
			throws NotFoundException {
		uri = Utils.cleanURI(uri);
		URI checkedURI = null;
		try{
			checkedURI = new URI(uri);
		} catch (URISyntaxException e) {
			return ResponseFactory.badRequestResponse(400, "URI malformatted", "uri");
		}
		return Predict.doUriPredict(checkedURI, confidence);
	}

//	@Override
	public Response logdPostFile(InputStream dataFileInputStream, FormDataContentDisposition dataFileDetail,
			Double confidence, SecurityContext securityContext) throws NotFoundException {
		return Predict.doFilePredict(dataFileInputStream, confidence);
	}
}
