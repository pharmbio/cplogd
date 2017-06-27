package se.uu.farmbio.api;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;

import io.swagger.jaxrs.config.SwaggerContextService;
import io.swagger.models.Contact;
import io.swagger.models.ExternalDocs;
import io.swagger.models.Info;
import io.swagger.models.Scheme;
import io.swagger.models.Swagger;

public class Bootstrap extends HttpServlet {
	/**
	 * 
	 */
	private static final long serialVersionUID = -439284950848469873L;

	@Override
	public void init(ServletConfig config) throws ServletException {
		Info info = new Info()
				.title("Swagger Server")
				.description("This is a Conformal Prediction Service for predicting Log D values for compounds. The  underlying model has been trained and evaluated on ChEMBL 23 data. Modeling is done using CPSign, product of GenettaSoft AB.")
				.termsOfService("")
				.contact(new Contact()
						.email("info@genettasoft.com"))
				//      .license(new License()
				//        .name("")
				//        .url("http://unlicense.org"))
				;

		Swagger swagger = new Swagger()
	    		.info(info)
	    		.basePath("/v1")
	    		.host("localhost")
	    		.scheme(Scheme.HTTP)
	    		.produces("application/json")
	    		.externalDocs(new ExternalDocs("More information", "http://cpsign-docs.genettasoft.com/"))
	    		;

		new SwaggerContextService().withServletConfig(config).updateSwagger(swagger);
	}
}
