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
import se.uu.farmbio.api.factories.LogdApiServiceFactory;
import se.uu.farmbio.models.BadRequestError;
import se.uu.farmbio.models.Error;
import se.uu.farmbio.models.Prediction;

@Path("/")
@Consumes({ "multipart/form-data" })
@Produces({ "application/json" })
@io.swagger.annotations.Api(description = "the logD API")
@javax.annotation.Generated(value = "io.swagger.codegen.languages.JavaJerseyServerCodegen", date = "2017-05-29T12:46:15.437Z")
public class LogdApi  {
	private final LogdApiService delegate = LogdApiServiceFactory.getLogdApi();

	@Path("/prediction")
	@GET
	@Consumes({ "multipart/form-data" })
	@Produces({ "application/json" })
	@io.swagger.annotations.ApiOperation(value = "Predict a single compound in SMILES format", 
	notes = "Predict the logD value of a compound in SMILES format", 
	response = void.class, 
	tags={ "Predict", })
	@io.swagger.annotations.ApiResponses(value = { 
			@io.swagger.annotations.ApiResponse(code = 200, message = "prediction result", response = Prediction.class),

			@io.swagger.annotations.ApiResponse(code = 400, message = "SMILES not possible to parse", response = BadRequestError.class),

			@io.swagger.annotations.ApiResponse(code = 500, message = "Server error", response = Error.class) })
	public Response logdGet(@ApiParam(value = "Compound structure notation using SMILES notation", required=true) @DefaultValue("c1ccccc1") @QueryParam("smiles") String smiles
			,@ApiParam(value = "The desired confidence of the prediction", defaultValue="0.8") @QueryParam("confidence") Double confidence
			,@Context SecurityContext securityContext)
					throws NotFoundException {
		return delegate.logdGet(smiles,confidence,securityContext);
	}

	@Path("/predictionImage")
	@GET
	@Consumes({ "multipart/form-data" })
	@Produces("image/png")
	@io.swagger.annotations.ApiOperation(value = "Predict the gradient of a single compound in SMILES format", 
	notes = "Predict and depict the gradient of a compound in SMILES format, using the logD predictor",
	response = void.class, 
	tags={ "Predict", })
	@io.swagger.annotations.ApiResponses(value = { 
			@io.swagger.annotations.ApiResponse(code = 200, message = "prediction result", response = void.class),

			@io.swagger.annotations.ApiResponse(code = 400, message = "SMILES not possible to parse", response = BadRequestError.class),

			@io.swagger.annotations.ApiResponse(code = 500, message = "Server error", response = Error.class) })
	public Response logdImgGet(
			@ApiParam(value = "Compound structure notation using SMILES notation", required=true) @DefaultValue("c1ccccc1") @QueryParam("smiles") String smiles,
			@ApiParam(value = "Image width", required=false) @DefaultValue("600") @QueryParam("imageWidth") int imgWidth,
			@ApiParam(value = "Image height (height of molecule part, total image hight will be larger due to added legend)", required=false) @DefaultValue("600") @QueryParam("imageHeight") int imgHeight,
			@Context SecurityContext securityContext)
					throws NotFoundException {
		return delegate.logdImageGet(smiles, imgWidth, imgHeight, securityContext);
	}



	// POST THAT TAKES ONLY A URI

	//	@POST
	//	@Consumes({ "multipart/form-data" })
	//	@Produces({ "application/json" })
	//	@io.swagger.annotations.ApiOperation(value = "Predict the result from a complete file, either in SDF or SMILES (one SMILES per line)", 
	//		notes = "<b>Upload of local datafile</b></br>Predict the result from a complete file, either in SDF or SMILES (one SMILES per line). The file will be predicted and new properties will be added to the properties already present in the file. The result from this endpoint is the URI of a <b>Task</b> that should be queried for when the  prediction has finished.", 
	//		response = void.class, tags={ "Predict", })
	//	@io.swagger.annotations.ApiResponses(value = { 
	//			@io.swagger.annotations.ApiResponse(code = 202, message = "Prediction accepted by server - redirect to prediction task to query", response = void.class),
	//
	//			@io.swagger.annotations.ApiResponse(code = 400, message = "Bad request (missing dataset)", response = BadRequestError.class),
	//
	//			@io.swagger.annotations.ApiResponse(code = 500, message = "Server error", response = Error.class) })
	//	public Response logdPost(@ApiParam(value = "A dataset to be predicted", required=true) @QueryParam("uri") String uri
	//			,@ApiParam(value = "The desired confidence of the prediction", defaultValue="0.8") @DefaultValue("0.8") @QueryParam("confidence") Double confidence
	//			,@Context SecurityContext securityContext)
	//					throws NotFoundException {
	//		return delegate.logdPostURI(uri,confidence,securityContext);
	//	}

	// POST THAT REQUIRES UPLOADING A FILE

	//	@POST
	//	@Path("/upload")
	//	@Consumes({ "multipart/form-data" })
	//	@Produces({ "application/json" })
	//	@io.swagger.annotations.ApiOperation(value = "Predict the result from a complete file, either in SDF or SMILES (one SMILES per line)", 
	//		notes = "<b>publically accessible URI</b></br>Predict the result from a complete file, either in SDF or SMILES (one SMILES per line). The file will be predicted and new properties will be added to the properties already present in the file. The result from this endpoint is the URI of a <b>Task</b> that should be queried for when the  prediction has finished.", 
	//		response = void.class, tags={ "Predict", })
	//	@io.swagger.annotations.ApiResponses(value = { 
	//			@io.swagger.annotations.ApiResponse(code = 202, message = "Prediction accepted by server - redirect to prediction task to query", response = void.class),
	//
	//			@io.swagger.annotations.ApiResponse(code = 400, message = "Bad request (missing dataset)", response = BadRequestError.class),
	//
	//			@io.swagger.annotations.ApiResponse(code = 500, message = "Server error", response = Error.class) })
	//	public Response logdPostUpload(
	//			@FormDataParam("dataFile") InputStream dataFileInputStream,
	//			@FormDataParam("dataFile") FormDataContentDisposition dataFileDetail
	//			,@ApiParam(value = "The desired confidence of the prediction", defaultValue="0.8") @DefaultValue("0.8") @QueryParam("confidence") Double confidence
	//			,@Context SecurityContext securityContext)
	//					throws NotFoundException {
	//		return delegate.logdPostFile(dataFileInputStream, dataFileDetail,confidence,securityContext);
	//	}
}


//@POST
//@Consumes({ "multipart/form-data" })
//@Produces({ "application/json" })
//@io.swagger.annotations.ApiOperation(value = "Predict the result from a complete file, either in SDF or SMILES (one SMILES per line)", notes = "Predict the result from a complete file, either in SDF or SMILES (one SMILES per line). <b>Either</b> upload the datafile <b>or</b> send a URI where the dataset can be read from (must be a publically accessible URI). The file will be predicted and new properties will be added to the properties already present in the file. The result from this endpoint is the URI of a <b>Task</b> that should be queried for when the  prediction has finished.", response = void.class, tags={ "Predict", })
//@io.swagger.annotations.ApiResponses(value = { 
//		@io.swagger.annotations.ApiResponse(code = 202, message = "Prediction accepted by server - redirect to prediction task to query", response = void.class),
//
//		@io.swagger.annotations.ApiResponse(code = 400, message = "Bad request (missing dataset)", response = BadRequestError.class),
//
//		@io.swagger.annotations.ApiResponse(code = 500, message = "Server error", response = Error.class) })
//public Response logdPost(@ApiParam(value = "A dataset to be predicted", required=false) @QueryParam("uri") String uri
//		,
//		@FormDataParam("dataFile") InputStream dataFileInputStream,
//		@FormDataParam("dataFile") FormDataContentDisposition dataFileDetail
//		,@ApiParam(value = "The desired confidence of the prediction", defaultValue="0.8") @DefaultValue("0.8") @QueryParam("confidence") Double confidence
//		,@Context SecurityContext securityContext)
//				throws NotFoundException {
//	return delegate.logdPost(uri,dataFileInputStream, dataFileDetail,confidence,securityContext);
//}
