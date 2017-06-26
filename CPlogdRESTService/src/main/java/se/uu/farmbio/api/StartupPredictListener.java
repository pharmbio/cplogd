package se.uu.farmbio.api;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import se.uu.farmbio.api.predict.Predict;

public class StartupPredictListener implements ServletContextListener {

	public void contextInitialized(ServletContextEvent event) {
		// causes the static initialization 
		new Predict();
	}

	public void contextDestroyed(ServletContextEvent event) {
		// Do stuff during webapp's shutdown.
	}

}

