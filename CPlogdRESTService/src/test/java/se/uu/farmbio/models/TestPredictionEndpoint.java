package se.uu.farmbio.models;

import javax.ws.rs.core.Response;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.junit.Assert;
import org.junit.Test;

import se.uu.farmbio.api.PredictApi;

public class TestPredictionEndpoint {
	
	@Test
	public void testNoSMILES() throws ParseException{
		Response res = new PredictApi().predictGet(null, 0.08, null);
		Assert.assertEquals(400, res.getStatus());
		assertSMILESisFaulty(res);
	}
	
	public static void assertSMILESisFaulty(Response res) throws ParseException{
		String invalSMILES = res.getEntity().toString();
		JSONParser parser = new JSONParser();
		System.out.println(res.getEntity());
		JSONObject json = (JSONObject) parser.parse(invalSMILES);
		Assert.assertEquals(400l, json.get("code"));
		Assert.assertTrue(((JSONArray)json.get("fields")).contains("smiles"));
	}
	
	@Test
	public void testInvalidSMILES() throws ParseException {
		Response res = new PredictApi().predictGet("ccc", 0.08, null);
		Assert.assertEquals(400, res.getStatus());
		assertSMILESisFaulty(res);
	}
	
	@Test
	public void testConfNumberOutOfRangeNonExisting() throws Exception {
		Response res = new PredictApi().predictGet("CCCC", null, null);
		Assert.assertTrue(res.getStatus() != 200);
		System.out.println("RES no conf: " + res.getEntity());
	}
	
	@Test
	public void testConfNumberOutOfRangeLow() throws Exception {
		Response res = new PredictApi().predictGet("CCCC", -0.001, null);
		System.out.println("RES low conf: " + res.getEntity());
	}
	
	@Test
	public void testConfNumberOutOfRangeHigh() throws Exception {
		Response res = new PredictApi().predictGet("CCCC", 1.01, null);
		System.out.println("RES high conf: " + res.getEntity());
	}
	
	
	@Test
	public void testValidSMILEShouldBeOK() throws Exception {
		Response res = new PredictApi().predictGet("CCCC", 0.08, null);
		Assert.assertEquals(200, res.getStatus());
		System.out.println("Correct prediction: " + res.getEntity());
		
		// it should be parsable with json!
		JSONParser parser = new JSONParser();
		try {
			parser.parse((String) res.getEntity());
		} catch(Exception e) {
			Assert.fail("Prediction was not rendered as correct json: " + res.getEntity());
		}
	}

}
