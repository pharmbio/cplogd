package se.uu.farmbio.api.predict;

import com.genettasoft.modeling.CPSignFactory;

public class Utils {
	
	public static CPSignFactory getFactory() throws IllegalArgumentException {
		try{
			System.out.println(Utils.class.getClassLoader().getResource("*.license"));
			
			CPSignFactory factory = new CPSignFactory(Utils.class.getClassLoader().getResourceAsStream("cpsign0.5-standard.license"));
			return factory;
		} catch (IllegalArgumentException e) {
			throw new IllegalArgumentException("The cpsign-license is not valid, contact info@genettasoft.com to get a new Docker container with a new license");
		} catch (Exception e) {
			throw new RuntimeException("Could not instantiate CPSign: " + e.getMessage());
		}
	}
	
	public static double roundTo3digits(double val){
		return Math.round(val*1000.0)/1000.0;
	}

	public static String cleanURI(String scrambledURI){
		String changeColons = scrambledURI.replaceAll("%3A", ":");
		String changeDashes = changeColons.replaceAll("%2F", "/");
		return changeDashes;
	}
}
