package se.uu.farmbio.api;

import io.swagger.annotations.ApiParam;
import io.swagger.jaxrs.*;

import java.io.File;

import se.uu.farmbio.api.LogdApiService;
import se.uu.farmbio.api.NotFoundException;
import se.uu.farmbio.api.factories.LogdApiServiceFactory;
import se.uu.farmbio.model.*;

import java.util.List;
import java.io.InputStream;

import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataParam;

import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.*;
import javax.validation.constraints.*;

@Path("/logd")
@Consumes({ "multipart/form-data" })
@Produces({ "application/json" })
@io.swagger.annotations.Api(description = "the logd API")
@javax.annotation.Generated(value = "io.swagger.codegen.languages.JavaJerseyServerCodegen", date = "2017-05-29T12:46:15.437Z")
public class LogdApi  {
   private final LogdApiService delegate = LogdApiServiceFactory.getLogdApi();

    @GET
    
    @Consumes({ "multipart/form-data" })
    @Produces({ "application/json" })
    @io.swagger.annotations.ApiOperation(value = "Predict a single compound in SMILES format", notes = "", response = InlineResponse200.class, tags={ "Predict", })
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 200, message = "prediction result", response = InlineResponse200.class),
        
        @io.swagger.annotations.ApiResponse(code = 400, message = "SMILES not possible to parse", response = InlineResponse200.class),
        
        @io.swagger.annotations.ApiResponse(code = 500, message = "Server error", response = InlineResponse200.class) })
    public Response logdGet(@ApiParam(value = "Compound structure notation using SMILES notation",required=true) @QueryParam("smiles") String smiles
,@ApiParam(value = "The desired confidence of the prediction", defaultValue="0.8") @DefaultValue("0.8") @QueryParam("confidence") Double confidence
,@Context SecurityContext securityContext)
    throws NotFoundException {
        return delegate.logdGet(smiles,confidence,securityContext);
    }
    @POST
    
    @Consumes({ "multipart/form-data" })
    @Produces({ "application/json" })
    @io.swagger.annotations.ApiOperation(value = "Predict the result from a complete file, either in SDF or SMILES (one SMILES per line)", notes = "Predict the result from a complete file, either in SDF or SMILES (one SMILES per line). <b>Either</b> upload the datafile <b>or</b> send a URI where the dataset can be read from (must be a publically accessible URI). The file will be predicted and new properties will be added to the properties already present in the file. The result from this endpoint is the URI of a <b>Task</b> that should be queried for when the  prediction has finished.", response = void.class, tags={ "Predict", })
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 302, message = "Prediction accepted by server - redirect to prediction task to query", response = void.class),
        
        @io.swagger.annotations.ApiResponse(code = 400, message = "Bad request (missing dataset)", response = void.class),
        
        @io.swagger.annotations.ApiResponse(code = 500, message = "Server error", response = void.class) })
    public Response logdPost(@ApiParam(value = "A dataset to be predicted") @QueryParam("uri") String uri
,
            @FormDataParam("dataFile") InputStream dataFileInputStream,
            @FormDataParam("dataFile") FormDataContentDisposition dataFileDetail
,@ApiParam(value = "The desired confidence of the prediction", defaultValue="0.8") @DefaultValue("0.8") @QueryParam("confidence") Double confidence
,@Context SecurityContext securityContext)
    throws NotFoundException {
        return delegate.logdPost(uri,dataFileInputStream, dataFileDetail,confidence,securityContext);
    }
}
