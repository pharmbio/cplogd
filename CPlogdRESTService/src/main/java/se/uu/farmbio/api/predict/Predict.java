package se.uu.farmbio.api.predict;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.Map;

import javax.ws.rs.core.Response;

import org.javatuples.Pair;
import org.openscience.cdk.exception.CDKException;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.genettasoft.modeling.CPSignFactory;
import com.genettasoft.modeling.ISaveableModel;
import com.genettasoft.modeling.acp.ACPRegressionResult;
import com.genettasoft.modeling.cheminf.SignaturesACPClassification;
import com.genettasoft.modeling.cheminf.SignaturesACPRegression;
import com.genettasoft.modeling.cheminf.SignaturesTCPClassification;
import com.genettasoft.modeling.io.bndTools.BNDLoader;

import se.uu.farmbio.api.StringUtil;
import se.uu.farmbio.models.Prediction;
import se.uu.farmbio.api.responses.ResponseFactory;

public class Predict {

	private static final Logger logger = LoggerFactory.getLogger(Predict.class);
	private static final String MODEL = "acp.regression.test.cpsign";

	private static Response serverErrorResponse = null;
	private static SignaturesACPRegression signaturesACPReg = null;

	static {
		// Get the root logger for cpsign
		ch.qos.logback.classic.Logger cpsignRoot = (ch.qos.logback.classic.Logger) org.slf4j.LoggerFactory.getLogger("com.genettasoft.modeling");
		// Disable all output
		cpsignRoot.setLevel(ch.qos.logback.classic.Level.OFF);
		// Instantiate the factory 
		try{
			CPSignUtils.getFactory();
			logger.debug("Initiated the CPSignFactory");
		} catch (RuntimeException re){
			logger.debug("Got exception when trying to instantiate CPSignFactory: " + re.getMessage());
			serverErrorResponse = ResponseFactory.errorResponse(500, re.getMessage());

		}
		// load the model
		if(serverErrorResponse != null) {
			try {
				logger.debug("Trying to load in the model");
				signaturesACPReg = (SignaturesACPRegression) BNDLoader.loadModel(Predict.class.getResource(MODEL).toURI(), null);
				logger.debug("Finished initializing the server with the loaded model");
			} catch (IllegalAccessException | IOException | URISyntaxException e) {
				logger.debug("Could not load the model", e);
				serverErrorResponse = ResponseFactory.errorResponse(500, "Server error - could not load the built model");
			}
		}

	}


	public static synchronized Response doSinglePredict(String smiles, double confidence) {
		logger.debug("got a prediction task, smiles="+smiles + " , conf=" + confidence);
		if (smiles==null || smiles.isEmpty()){
			logger.debug("Missing arguments 'smiles'");
			return ResponseFactory.badRequestResponse(400, "missing argument", Arrays.asList("smiles"));
		}

		IAtomContainer molToPredict=null;
		try{
			molToPredict = CPSignFactory.parseSMILES(smiles);
		} catch(IllegalArgumentException e){
			logger.debug("Got exception when parsing smiles: " + e.getMessage() + "\nreturning error-msg and stopping");
			return ResponseFactory.badRequestResponse(400, "Invalid query SMILES:" + smiles, Arrays.asList("smiles"));
		}
		
		try {
			ACPRegressionResult res = signaturesACPReg.predict(molToPredict, confidence);
			return ResponseFactory.predictResponse(new Prediction(smiles, res.getInterval().getValue0(), res.getInterval().getValue1(), res.getY_hat()));
		} catch (IllegalAccessException | CDKException e) {
			logger.debug("Failed predicting smiles=" + smiles, e);
			return ResponseFactory.errorResponse(500, "Server error predicting");
		}
	}
	
	public static synchronized Response doFilePredict(String smiles, double confidence) {
		logger.debug("got a prediction task, smiles="+smiles + " , conf=" + confidence);
		if (smiles==null || smiles.isEmpty()){
			logger.debug("Missing arguments 'smiles'");
			return ResponseFactory.badRequestResponse(400, "missing argument", Arrays.asList("smiles"));
		}

		IAtomContainer molToPredict=null;
		try{
			molToPredict = CPSignFactory.parseSMILES(smiles);
		} catch(IllegalArgumentException e){
			logger.debug("Got exception when parsing smiles: " + e.getMessage() + "\nreturning error-msg and stopping");
			return ResponseFactory.badRequestResponse(400, "Invalid query SMILES:" + smiles, Arrays.asList("smiles"));
		}
		
		try {
			ACPRegressionResult res = signaturesACPReg.predict(molToPredict, confidence);
			return ResponseFactory.predictResponse(new Prediction(smiles, res.getInterval().getValue0(), res.getInterval().getValue1(), res.getY_hat()));
		} catch (IllegalAccessException | CDKException e) {
			logger.debug("Failed predicting smiles=" + smiles, e);
			return ResponseFactory.errorResponse(500, "Server error predicting");
		}
	}



/*
	public static Response doPredict(String id, String datasetUri, String smiles, double confidence) {
		logger.debug("got a prediction task, id=" + id + ", smiles="+smiles);
		try{

			if ((smiles==null || smiles.isEmpty()) && (id == null || id.isEmpty())){
				logger.debug("Missing arguments 'smiles' and 'id'");
				return ResponseFactory.badRequestResponse(400, "missing arguments", Arrays.asList("id", "smiles"));
			}

			// Fail fast in case SMILES is missing or missing trainingDataUri
			if (smiles == null || smiles.isEmpty()) {
				logger.debug("Missing argument 'smiles'");
				return ResponseFactory.badRequestResponse(400, "missing argument", Arrays.asList("smiles"));
			}
			// just ignore datasetUri for now.. (we would have to store the results somewhere etc..

			// Fail fast, check the SMILES is valid


			// Load the model
			if (id == null || id.isEmpty()){
				logger.debug("Missing argument 'id'");
				return ResponseFactory.badRequestResponse(400, "missing argument", Arrays.asList("id"));
			}

			// Init the CPSign jar with a valid license 
			try{
				CPSignUtils.getFactory();
			} catch (RuntimeException re){
				logger.debug("Got exception when trying to instantiate CPSignFactory: " + re.getMessage());
				return ResponseFactory.errorResponse(500, "Could not locate license");
			}

			ISaveableModel model = null;
			try{
				if (id.equals("1")){
					model = BNDLoader.loadModel(Predict.class.getClassLoader().getResource("acp.regression.cpsign").toURI(), null);
				} else if (id.equals("2")){
					model = BNDLoader.loadModel(Predict.class.getClassLoader().getResource("acp.classification.cpsign").toURI(), null);
				} else if (id.equals("3")){
					model = BNDLoader.loadModel(Predict.class.getClassLoader().getResource("tcp.classification.cpsign").toURI(), null);
				} else {
					URI modelURI = null;
					try{
						modelURI = new URI(id);
						modelURI.toURL(); // make sure it's a functional URI
					} catch(Exception e){modelURI=null;}
					if(modelURI == null){
						// try to clean the URI
						try{
							modelURI = new URI(StringUtil.cleanURI(id));
							modelURI.toURL();
						} catch(Exception e){ modelURI=null;}
					}
					if(modelURI != null)
						model = BNDLoader.loadModel(modelURI, null);
					else
						return ResponseFactory.badRequestResponse(404, "model URI could not be parsed",  Arrays.asList("id"));

				}

			} catch(Exception e){
				logger.debug("Could not load the model", e);
				return ResponseFactory.badRequestResponse(404, "model for specified id not found", Arrays.asList("id"));
			}


			// Do the prediction, depending on the model
			if (model instanceof SignaturesTCPClassification){
				return ResponseFactory.predictResponse(doPredict((SignaturesTCPClassification)model, smiles, molToPredict, confidence));
			} else if (model instanceof SignaturesACPRegression){
				return ResponseFactory.predictResponse(doPredict((SignaturesACPRegression)model, smiles, molToPredict, confidence));
			} else if (model instanceof SignaturesACPClassification){
				return ResponseFactory.predictResponse(doPredict((SignaturesACPClassification)model, smiles, molToPredict, confidence));
			} else {
				return ResponseFactory.badRequestResponse(400, "Model type not supported for prediction at this moment", Arrays.asList("uri"));
			}

		} catch(Exception e){
			logger.debug("",e);
			return ResponseFactory.errorResponse(500, e.getMessage());
		}
	}

	private static Prediction doPredict(SignaturesACPRegression acpReg, String smiles, IAtomContainer mol, double conf) throws Exception {
		ACPRegressionResult res = acpReg.predict(mol, conf);
		Pair<Double, Double> interval = res.getInterval();
		return new Prediction(smiles, interval);
	}

	private static Prediction doPredict(SignaturesACPClassification acpClass, String smiles, IAtomContainer mol, double conf) throws Exception {
		Map<String,Double> res = acpClass.predictMondrianTwoClasses(mol);

		return new Prediction(smiles, res);
	}

	private static Prediction doPredict(SignaturesTCPClassification tcpClass, String smiles, IAtomContainer mol, double conf) throws Exception {
		Map<String, Double> res = tcpClass.predictMondrianTwoClasses(mol);

		return new Prediction(smiles, res);
	}
*/

}
