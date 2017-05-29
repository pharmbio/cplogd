package se.uu.farmbio.api;

import io.swagger.jaxrs.config.SwaggerContextService;
import io.swagger.models.*;

import io.swagger.models.auth.*;

import javax.servlet.http.HttpServlet;
import javax.servlet.ServletContext;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;

public class Bootstrap extends HttpServlet {
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

		ServletContext context = config.getServletContext();
		Swagger swagger = new Swagger().info(info);

		new SwaggerContextService().withServletConfig(config).updateSwagger(swagger);
	}
}
