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
		String infoText = "The model predicts Log D based on a support vector machine trained on data from ChEMBL version 23 comprising approximately 1.6 million compounds. "+ 
				"The confidence interval is calculated for the confidence specified as argument to the endpoints using the conformal prediction approach. "+
				"For a graphical prediction UI, please go to https://cplogd.service.pharmb.io/draw/. " +
				"For citing this service and for more information:\n" + 
				"\n" + 
				"**A confidence predictor for logD using conformal regression and a support-vector machine**\n" + 
				"Maris Lapins, Staffan Arvidsson, Samuel Lampa, Arvid Berg, Wesley Schaal, Jonathan Alvarsson and Ola Spjuth Journal of Cheminformatics 10.1 (2018): 17. \n" + 
				"https://link.springer.com/article/10.1186/s13321-018-0271-1";
		Info info = new Info()
				.title("cpLogD")
				.description(infoText)
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
