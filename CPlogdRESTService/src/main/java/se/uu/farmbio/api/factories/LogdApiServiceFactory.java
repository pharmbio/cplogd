package se.uu.farmbio.api.factories;

import se.uu.farmbio.api.LogdApiService;
import se.uu.farmbio.api.impl.LogdApiServiceImpl;

@javax.annotation.Generated(value = "io.swagger.codegen.languages.JavaJerseyServerCodegen", date = "2017-05-29T12:46:15.437Z")
public class LogdApiServiceFactory {
    private final static LogdApiService service = new LogdApiServiceImpl();

    public static LogdApiService getLogdApi() {
        return service;
    }
}
