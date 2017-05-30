package se.uu.farmbio.api.predict;

import com.genettasoft.modeling.CPSignFactory;

public class CPSignUtils {
	
	public static CPSignFactory getFactory() throws IllegalArgumentException {
		try{
			CPSignFactory factory = new CPSignFactory(CPSignUtils.class.getClassLoader().getResourceAsStream("cpsign0.5-standard.license"));
			return factory;
		} catch (IllegalArgumentException e) {
			throw new IllegalArgumentException("The cpsign-license is not valid, contact info@genettasoft.com to get a new Docker container with a new license");
		} catch (Exception e) {
			throw new RuntimeException("Could not instantiate CPSign");
		}
	}

}
