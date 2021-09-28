package se.uu.farmbio.api;

import javax.ws.rs.ApplicationPath;

import org.glassfish.jersey.CommonProperties;
import org.glassfish.jersey.jackson.JacksonFeature;
import org.glassfish.jersey.server.ResourceConfig;

import io.swagger.v3.jaxrs2.integration.resources.OpenApiResource;

@ApplicationPath("/v1")
public class RESTApplication extends ResourceConfig {
	
	public RESTApplication() {
		property(CommonProperties.METAINF_SERVICES_LOOKUP_DISABLE, true);
		register(PredictApi.class);
		register(OpenApiResource.class);
		register(JacksonFeature.class); // Jackson-serialization
	}
	
}
