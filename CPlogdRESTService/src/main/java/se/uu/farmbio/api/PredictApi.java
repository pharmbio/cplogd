package se.uu.farmbio.api;

import javax.ws.rs.Consumes;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;

import org.slf4j.Logger;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import se.uu.farmbio.api.model.BadRequestError;
import se.uu.farmbio.api.model.ErrorResponse;
import se.uu.farmbio.api.model.Prediction;
import se.uu.farmbio.api.model.ServiceRunning;
import se.uu.farmbio.api.predict.Predict;

@Path("")
public class PredictApi  {

	private static final Logger logger = org.slf4j.LoggerFactory.getLogger(PredictApi.class);
	private static final String PREDICT_TAG = "Predict";
	private static final String INFO_TAG = "Server info";
	private static final String PNG_MEDIA_TYPE = "image/png";

	@Path("/health")
	@GET
	@Produces( MediaType.APPLICATION_JSON )
	@Operation(
			summary="Get the status of the prediction service",
			tags = { INFO_TAG },
			responses = {
					@ApiResponse(responseCode="200", description="Service is running",
							content = @Content(schema = @Schema(implementation = ServiceRunning.class))),
					@ApiResponse(responseCode="503", description="Service down", 
					content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
			})
	public Response health() {
		try {
			return Predict.checkHealth();
		} catch (Exception e) {
			return convertToErrorResponse(e);
		}
	}

	@Path("/predict")
	@GET
	@Consumes( MediaType.APPLICATION_FORM_URLENCODED )
	@Produces( MediaType.APPLICATION_JSON )
	@Operation(
			summary = "Make a prediction on a given molecule", 
			tags = {PREDICT_TAG},
			description = "Predict a given molecule in SMILES or MDL v2000/v3000 format. In case a MDL is sent, it must be properly URL-encoded in UTF-8. You can use for instance https://www.urlencoder.org/ to encode your file.", 
			responses = { 
					@ApiResponse(responseCode = "200", description = "OK", content = @Content(
							schema = @Schema(implementation = Prediction.class))),

					@ApiResponse(responseCode = "400", description = "Bad Request",content = @Content(
							schema = @Schema(implementation = BadRequestError.class))),

					@ApiResponse(responseCode = "500", description = "Prediction error", content = @Content(
							schema = @Schema(implementation = ErrorResponse.class))),

					@ApiResponse(responseCode = "503", description = "Service not available", content = @Content(
							schema = @Schema(implementation = ErrorResponse.class)))
			}
			)
	public Response predictGet(

			@Parameter(description = "Compound structure notation using SMILES or MDL format", 
				required=true, 
				example="CCCCC=O")
			@QueryParam("molecule") 
			String molecule,

			@Parameter(description = "The desired confidence of the prediction", required=false, example="0.8", 
				schema = @Schema(maximum="1.0", minimum="0.0"))
			@QueryParam("confidence") 
			Double confidence,
			
			@Context SecurityContext securityContext) {
		try {
			return Predict.doSinglePredict(molecule, confidence);
		} catch (Exception e) {
			return convertToErrorResponse(e);
		}
	}

	@Path("/predict")
	@POST
	@Consumes( MediaType.TEXT_PLAIN )
	@Produces( MediaType.APPLICATION_JSON )
	@Operation(
			summary = "Make a prediction on a given molecule", 
			tags = {PREDICT_TAG},
			description = "Predict a given molecule in SMILES or MDL v2000/v3000 format", 
			responses = { 
					@ApiResponse(responseCode = "200", description = "OK", content = @Content(
							schema = @Schema(implementation = Prediction.class))),

					@ApiResponse(responseCode = "400", description = "Bad Request",content = @Content(
							schema = @Schema(implementation = BadRequestError.class))),

					@ApiResponse(responseCode = "500", description = "Prediction error", content = @Content(
							schema = @Schema(implementation = ErrorResponse.class))),

					@ApiResponse(responseCode = "503", description = "Service not available", content = @Content(
							schema = @Schema(implementation = ErrorResponse.class)))
			}
			)
	public Response predictPost(

			@RequestBody(description="Molecule in SMILES or MDL format",
			content = @Content(examples=@ExampleObject(value="CCCCC=O"), mediaType = MediaType.TEXT_PLAIN))
			String molecule,

			@Parameter(description = "The desired confidence of the prediction", required=false, example="0.8", 
			schema = @Schema(maximum="1.0", minimum="0.0"))
			@QueryParam("confidence") 
			Double confidence,

			@Context SecurityContext securityContext) {
		try {
			return Predict.doSinglePredict(molecule, confidence);
		} catch (Exception e) {
			return convertToErrorResponse(e);
		}
	}


	@Path("/predictionImage")
	@GET
	@Consumes( MediaType.APPLICATION_FORM_URLENCODED )
	@Produces({ PNG_MEDIA_TYPE, MediaType.APPLICATION_JSON }) 
	@Operation(
			summary = "Make a prediction image for the given molecule",
			tags = { PREDICT_TAG },
			description = "Predict a given molecule to get a prediction image, accepts SMILES or MDL v2000/v3000 format. In case a MDL is sent, it must be properly URL-encoded in UTF-8. You can use for instance https://www.urlencoder.org/ to encode your file.",
			responses = { 
					@ApiResponse(responseCode = "200", description = "OK", 
							content=@Content(mediaType = PNG_MEDIA_TYPE)),

					@ApiResponse(responseCode = "400", description = "Bad Request", content = @Content(
							schema = @Schema(implementation = BadRequestError.class), mediaType = MediaType.APPLICATION_JSON)),

					@ApiResponse(responseCode = "500", description = "Prediction error", content = @Content(
							schema = @Schema(implementation = ErrorResponse.class), mediaType = MediaType.APPLICATION_JSON)),

					@ApiResponse(responseCode = "503", description = "Service not available", content = @Content(
							schema = @Schema(implementation = ErrorResponse.class), mediaType = MediaType.APPLICATION_JSON)) 
			}
			)
	public Response predictImageGet( 

			@Parameter(description = "Compound structure notation using SMILES or MDL format", 
			required=false, 
			example="CCCCC=O")
			@QueryParam("molecule") 
			String molecule,

			@Parameter(description = "The desired confidence of the prediction", required=false, example="0.8", 
			schema = @Schema(maximum="1.0", minimum="0.0"))
			@QueryParam("confidence") 
			Double confidence,

			@Parameter(description = "Image width in pixels",
			schema = @Schema(maximum=""+Predict.MAX_IMAGE_SIZE, minimum=""+Predict.MIN_IMAGE_SIZE, defaultValue=""+Predict.DEFAULT_IMAGE_WH))
			@DefaultValue(""+Predict.DEFAULT_IMAGE_WH) 
			@QueryParam("imageWidth") 
			int imageWidth,

			@Parameter(description = "Image height in pixels",
			schema = @Schema(maximum=""+Predict.MAX_IMAGE_SIZE, minimum=""+Predict.MIN_IMAGE_SIZE, defaultValue=""+Predict.DEFAULT_IMAGE_WH)) 
			@DefaultValue(""+Predict.DEFAULT_IMAGE_WH) 
			@QueryParam("imageHeight") 
			int imageHeight,

			@Parameter(description = "Add title to the image (the model name)")
			@DefaultValue("false") 
			@QueryParam("addTitle") 
			boolean addTitle,
			
			@Context SecurityContext securityContext ) {
		logger.debug("Initial image-size at API-level: imageHeight="+imageHeight+", imageWidth="+imageWidth);
		try {
			return Predict.doImagePredict(molecule, confidence, imageWidth, imageHeight, addTitle);
		} catch (Exception e) {
			return convertToErrorResponse(e);
		}
	}

	@Path("/predictionImage")
	@POST
	@Consumes( MediaType.TEXT_PLAIN )
	@Produces({ PNG_MEDIA_TYPE, MediaType.APPLICATION_JSON }) 
	@Operation(
			summary = "Make a prediction image for the given molecule",
			tags = { PREDICT_TAG },
			description = "Predict a given molecule to get a prediction image, accepts SMILES or MDL v2000/v3000 format.",
			responses = { 
					@ApiResponse(responseCode = "200", description = "OK", 
							content=@Content(mediaType = PNG_MEDIA_TYPE)),

					@ApiResponse(responseCode = "400", description = "Bad Request", content = @Content(
							schema = @Schema(implementation = BadRequestError.class), mediaType = MediaType.APPLICATION_JSON)),

					@ApiResponse(responseCode = "500", description = "Prediction error", content = @Content(
							schema = @Schema(implementation = ErrorResponse.class), mediaType = MediaType.APPLICATION_JSON)),

					@ApiResponse(responseCode = "503", description = "Service not available", content = @Content(
							schema = @Schema(implementation = ErrorResponse.class), mediaType = MediaType.APPLICATION_JSON)) 
			}
			)
	public Response predictImagePost( 

			@RequestBody(description="Molecule to predict in SMILES or MDL format",
			content = @Content(examples=@ExampleObject(value="CCCCC=O"), mediaType = MediaType.TEXT_PLAIN))
			String molecule,

			@Parameter(description = "The desired confidence of the prediction", required=false, example="0.8", 
				schema = @Schema(maximum="1.0", minimum="0.0"))
			@QueryParam("confidence") 
			Double confidence,

			@Parameter(description = "Image width in pixels",
				schema = @Schema(maximum=""+Predict.MAX_IMAGE_SIZE, minimum=""+Predict.MIN_IMAGE_SIZE, defaultValue=""+Predict.DEFAULT_IMAGE_WH))
			@DefaultValue(""+Predict.DEFAULT_IMAGE_WH) 
			@QueryParam("imageWidth") 
			int imageWidth,

			@Parameter(description = "Image height in pixels",
			schema = @Schema(maximum=""+Predict.MAX_IMAGE_SIZE, minimum=""+Predict.MIN_IMAGE_SIZE, defaultValue=""+Predict.MAX_IMAGE_SIZE)) 
			@DefaultValue(""+Predict.DEFAULT_IMAGE_WH) 
			@QueryParam("imageHeight") 
			int imageHeight,

			@Parameter(description = "Add title to the image (the model name)")
			@DefaultValue("false") 
			@QueryParam("addTitle") 
			boolean addTitle,

			@Context SecurityContext securityContext ) {
		logger.debug("Initial image-size at API-level: imageHeight="+imageHeight+", imageWidth="+imageWidth);
		try {
			return Predict.doImagePredict(molecule, confidence, imageWidth, imageHeight, addTitle); 
		} catch (Exception e) {
			return convertToErrorResponse(e);
		}
	}

	private Response convertToErrorResponse(Exception e) {
		int code = javax.ws.rs.core.Response.Status.INTERNAL_SERVER_ERROR.getStatusCode();
		return Response.status(code).entity(new se.uu.farmbio.api.model.ErrorResponse(code, "Service failure: " + e.getMessage() + ", please contact the service provider if the error was not due to user-error")).build();
	}

}
