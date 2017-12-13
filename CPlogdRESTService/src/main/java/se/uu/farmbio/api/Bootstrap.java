package se.uu.farmbio.api;

import java.util.ArrayList;
import java.util.Arrays;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;

import io.swagger.jaxrs.config.SwaggerContextService;
import io.swagger.models.ExternalDocs;
import io.swagger.models.Info;
import io.swagger.models.Scheme;
import io.swagger.models.Swagger;

public class Bootstrap extends HttpServlet {
	/**
	 * 
	 */
	private static final long serialVersionUID = -439284950848469873L;
	private static final String HOST_URL_ENV_VARIABLE = "HOST_URL";
	
	public static String getHostURL(){
		try{
			return System.getenv(HOST_URL_ENV_VARIABLE);
		} catch(Exception e){
			return "localhost";
		}
	}

	@Override
	public void init(ServletConfig config) throws ServletException {
		Info info = new Info()
				.title("cpLogD")
				.description("This is a Conformal Prediction Service for predicting logD values for compounds. The  underlying model has been trained and evaluated on ChEMBL 23 data. Modeling is done using CPSign, product of GenettaSoft AB.")
				.termsOfService("")
				.version("0.0.1")
				;

		Swagger swagger = new Swagger()
	    		.info(info)
	    		.basePath("/v1")
	    		.host(getHostURL())
	    		.produces("application/json")
	    		.externalDocs(new ExternalDocs("Pharmb.io", "https://pharmb.io/"))
	    		;
		swagger.setSchemes(new ArrayList<Scheme>(Arrays.asList(Scheme.HTTPS,Scheme.HTTP)));

		new SwaggerContextService().withServletConfig(config).updateSwagger(swagger);
	}
}
