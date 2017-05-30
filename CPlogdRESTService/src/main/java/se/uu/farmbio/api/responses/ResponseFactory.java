package se.uu.farmbio.api.responses;

import java.util.Arrays;
import java.util.List;

import javax.ws.rs.core.Response;

import se.uu.farmbio.models.BadRequestError;
import se.uu.farmbio.models.Error;
import se.uu.farmbio.models.Prediction;

public class ResponseFactory {

	public static Response notImplementedYet(){
		return Response.status(501)
				.entity( 
						new Error(501,"Service not implemented yet").toString()
						).build();
	}
	
	public static Response errorResponse(int status, String msg){
		return Response.status(status)
				.entity( 
						new Error(status,msg).toString()
						).build();
	}
	
	public static Response badRequestResponse(int status, String message, List<String> fields){
		return Response.status(status)
				.entity(
						new BadRequestError(status, message, fields).toString()
						).build();
	}
	
	public static Response badRequestResponse(int status, String message, String field){
		return Response.status(status)
				.entity(
						new BadRequestError(status, message, Arrays.asList(field)).toString()
						).build();
	}
	
	
	public static Response predictResponse(Prediction prediction){
		return Response.status(200)
				.entity( 
						prediction.toString()
						).build();
	}
	
	public static Response taskAccepted(String taskURI){
		return Response.status(202)
				.entity(taskURI).build();
	}

}
