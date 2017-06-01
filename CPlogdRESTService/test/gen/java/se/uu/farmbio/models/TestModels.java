package se.uu.farmbio.models;

import java.util.Arrays;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.junit.Assert;
import org.junit.Test;

public class TestModels {
	
	static boolean PRINT_JSON = false;
	
	@Test
	public void TestBadRequestError() {
		BadRequestError bre = new BadRequestError(500, "Server Error", Arrays.asList("id", "dasa"));
		if(PRINT_JSON)
			System.out.println(bre.toString());
		
		JSONParser parser = new JSONParser();
		try{
			JSONObject parsedJSON = (JSONObject) parser.parse(bre.toString());
			Assert.assertEquals(3, parsedJSON.size());
		} catch(Exception e){
			Assert.fail();
		}
	}
	
	@Test
	public void TestPrediction() {
		Prediction pred = new Prediction("Cccc", 0.0000001, 1241.12414, 5000.00000, 12.124);
		if(PRINT_JSON)
			System.out.println(pred.toString());
		JSONParser parser = new JSONParser();
		try{
			JSONObject parsedJSON = (JSONObject) parser.parse(pred.toString());
			Assert.assertEquals(5, parsedJSON.size());
		} catch(Exception e){
			Assert.fail();
		}
	}
	
	@Test
	public void TestError() {
		Error err = new Error(241, "error this is eh?");
		if(PRINT_JSON)
			System.out.println(err);
		JSONParser parser = new JSONParser();
		try{
			JSONObject parsedJSON = (JSONObject) parser.parse(err.toString());
			Assert.assertEquals(2, parsedJSON.size());
		} catch(Exception e){
			Assert.fail();
		}
	}
	
	@Test
	public void TestTaskInfo() {
		TaskInfo ti = new TaskInfo("/localhost:sdfads", 124.124, 124.0);
		if(PRINT_JSON)
			System.out.println(ti);
		JSONParser parser = new JSONParser();
		try{
			JSONObject parsedJSON = (JSONObject) parser.parse(ti.toString());
			Assert.assertEquals(3, parsedJSON.size());
		} catch(Exception e){
			Assert.fail();
		}
	}

}
