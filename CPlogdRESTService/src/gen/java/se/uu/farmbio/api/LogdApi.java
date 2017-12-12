package se.uu.farmbio.api;

import javax.ws.rs.Consumes;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;

import io.swagger.annotations.ApiParam;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponses;
import io.swagger.annotations.ApiResponse;
import javax.annotation.Generated;
import se.uu.farmbio.api.factories.LogdApiServiceFactory;
import se.uu.farmbio.models.BadRequestError;
import se.uu.farmbio.models.Error;
import se.uu.farmbio.models.Prediction;

@Path("/")
@Consumes({ "multipart/form-data" })
@Produces({ "application/json" })
@Api()
@Generated(value = "io.swagger.codegen.languages.JavaJerseyServerCodegen", date = "2017-05-29T12:46:15.437Z")
public class LogdApi  {
	private final LogdApiService delegate = LogdApiServiceFactory.getLogdApi();

	@Path("/prediction")
	@GET
	@Consumes({ "multipart/form-data" })
	@Produces({ "application/json" })
	@ApiOperation(value = "Predict a single compound in SMILES format", 
	notes = "Predict the logD value of a compound in SMILES format", 
	response = void.class, 
	tags={ "Predict", })
	@ApiResponses(value = { 
			@ApiResponse(code = 200, message = "prediction result", response = Prediction.class),

			@ApiResponse(code = 400, message = "SMILES not possible to parse", response = BadRequestError.class),

			@ApiResponse(code = 500, message = "Server error", response = Error.class) })
	public Response logdGet(
			@ApiParam(value = "Compound structure notation using SMILES notation", required=true) @DefaultValue("c1ccccc1") @QueryParam("smiles") String smiles,
			@ApiParam(value = "The desired confidence of the prediction", defaultValue="0.8", allowableValues="range(0,1)") @QueryParam("confidence") Double confidence,
			@Context SecurityContext securityContext)
					throws NotFoundException {
		return delegate.logdGet(smiles,confidence,securityContext);
	}

	@Path("/predictionImage")
	@GET
	@Consumes({ "multipart/form-data" })
	@Produces({"image/png", "application/json"})
	@ApiOperation(value = "Predict the gradient of a single compound in SMILES format", 
	notes = "Predict and depict the gradient of a compound in SMILES format, using the logD predictor",
	response = void.class, 
	tags={ "Predict", })
	@ApiResponses(value = { 
			@ApiResponse(code = 200, message = "prediction result", response = void.class),

			@ApiResponse(code = 400, message = "SMILES not possible to parse", response = BadRequestError.class),

			@ApiResponse(code = 500, message = "Server error", response = Error.class) })
	public Response logdImgGet(
			@ApiParam(value = "Compound structure notation using SMILES notation", defaultValue="c1ccccc1") 
			@QueryParam("smiles") String smiles,
			@ApiParam(value = "The desired confidence of the prediction, allowed values=(0, 1)", 
				required=false, allowableValues="range[0,1]") 
			@QueryParam("confidence") Double confidence,
			@ApiParam(value = "Image width (min 50 pixels, max 5000 pixels)", 
				required=false, allowableValues="range[50,5000]") 
			@DefaultValue("600") 
			@QueryParam("imageWidth") int imgWidth,
			@ApiParam(value = "Image height (min 50 pixels, max 5000 pixels)", 
				required=false, allowableValues="range[50,5000]") 
			@DefaultValue("600") 
			@QueryParam("imageHeight") int imgHeight,
			@ApiParam(value = "Add title to figure")
			@DefaultValue("false")
			@QueryParam("addTitle") boolean addTitle,
			@Context SecurityContext securityContext)
					throws NotFoundException {
		return delegate.logdImageGet(smiles, confidence, imgWidth, imgHeight, addTitle, securityContext);
	}

}