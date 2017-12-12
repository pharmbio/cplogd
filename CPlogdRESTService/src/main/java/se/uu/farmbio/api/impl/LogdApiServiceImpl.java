package se.uu.farmbio.api.impl;

import javax.validation.constraints.NotNull;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;

import se.uu.farmbio.api.LogdApiService;
import se.uu.farmbio.api.NotFoundException;
import se.uu.farmbio.api.predict.Predict;
import se.uu.farmbio.api.responses.ResponseFactory;
@javax.annotation.Generated(value = "io.swagger.codegen.languages.JavaJerseyServerCodegen", date = "2017-05-29T12:46:15.437Z")
public class LogdApiServiceImpl extends LogdApiService {
	
	@Override
	public Response logdGet( 
			@NotNull String smiles,  
			Double confidence, 
			SecurityContext securityContext) throws NotFoundException {
		if(smiles==null || smiles.isEmpty())
			return ResponseFactory.badRequestResponse(400, "missing argument", "smiles");
		if(confidence == null)
			return ResponseFactory.badRequestResponse(400, "missing argument", "confidence");
		return Predict.doSinglePredict(smiles, confidence);
	}
	
	@Override
	public Response logdImageGet(String smiles, Double confidence, int imgWidth, int imgHeight, boolean addTitle, SecurityContext securityContext) throws NotFoundException {
		return Predict.doImagePredict(smiles, confidence, imgWidth, imgHeight, addTitle);
	}
}
