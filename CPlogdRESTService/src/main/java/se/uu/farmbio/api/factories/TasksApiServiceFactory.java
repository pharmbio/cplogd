package se.uu.farmbio.api.factories;

import se.uu.farmbio.api.TasksApiService;
import se.uu.farmbio.api.impl.TasksApiServiceImpl;

@javax.annotation.Generated(value = "io.swagger.codegen.languages.JavaJerseyServerCodegen", date = "2017-05-29T12:46:15.437Z")
public class TasksApiServiceFactory {
    private final static TasksApiService service = new TasksApiServiceImpl();

    public static TasksApiService getTasksApi() {
        return service;
    }
}
