package se.uu.farmbio.api.predict;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;

import javax.ws.rs.core.Response;

import org.apache.commons.io.IOUtils;
import org.openscience.cdk.exception.CDKException;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.genettasoft.modeling.CPSignFactory;
import com.genettasoft.modeling.acp.ACPRegressionResult;
import com.genettasoft.modeling.cheminf.SignaturesACPRegression;
import com.genettasoft.modeling.io.bndTools.BNDLoader;

import se.uu.farmbio.api.responses.ResponseFactory;
import se.uu.farmbio.models.Prediction;

public class Predict {

	private static final Logger logger = LoggerFactory.getLogger(Predict.class);
	private static final String MODEL = "acp.regression.test.cpsign";

	private static Response serverErrorResponse = null;
	private static SignaturesACPRegression signaturesACPReg = null;

	public static SignaturesACPRegression getModel() {
		return signaturesACPReg;
	}

	static {
		// Get the root logger for cpsign
		ch.qos.logback.classic.Logger cpsignRoot = (ch.qos.logback.classic.Logger) org.slf4j.LoggerFactory.getLogger("com.genettasoft.modeling");
		// Disable all output
		cpsignRoot.setLevel(ch.qos.logback.classic.Level.OFF);
		// Instantiate the factory 
		try{
			Utils.getFactory();
			logger.info("Initiated the CPSignFactory");
		} catch (RuntimeException re){
			logger.error("Got exception when trying to instantiate CPSignFactory: " + re.getMessage());
			serverErrorResponse = ResponseFactory.errorResponse(500, re.getMessage());
		}
		// load the model - only if no error previously encountered
		if (serverErrorResponse == null) {
			try {
				logger.debug("Trying to load in the model");
				URI modelURI = Predict.class.getClassLoader().getResource(MODEL).toURI();
				if(modelURI == null)
					throw new IOException("did not locate the model file");
				signaturesACPReg = (SignaturesACPRegression) BNDLoader.loadModel(modelURI, null);
				logger.info("Finished initializing the server with the loaded model");
			} catch (IllegalAccessException | IOException | URISyntaxException e) {
				logger.error("Could not load the model", e);
				serverErrorResponse = ResponseFactory.errorResponse(500, "Server error - could not load the built model");
			}
		}

	}
	


	/*
	 * =====================================================================================================================
	 * 
	 * 									SINGLE PREDICTION
	 * 
	 * =====================================================================================================================
	 */

	public static Response doSinglePredict(String smiles, double confidence) {
		logger.info("got a prediction task, smiles="+smiles + " , conf=" + confidence);
		
		if(serverErrorResponse != null)
			return serverErrorResponse;
		
		if (smiles==null || smiles.isEmpty()){
			logger.debug("Missing arguments 'smiles'");
			return ResponseFactory.badRequestResponse(400, "missing argument", Arrays.asList("smiles"));
		}

		IAtomContainer molToPredict=null;
		CDKMutexLock.requireLock();
		try{
			molToPredict = CPSignFactory.parseSMILES(smiles);
		} catch(IllegalArgumentException e){
			logger.debug("Got exception when parsing smiles: " + e.getMessage() + "\nreturning error-msg and stopping");
			CDKMutexLock.releaseLock();
			return ResponseFactory.badRequestResponse(400, "Invalid query SMILES \"" + smiles + "\"", Arrays.asList("smiles"));
		}

		try {
			ACPRegressionResult res = signaturesACPReg.predict(molToPredict, confidence);
			CDKMutexLock.releaseLock();
			return ResponseFactory.predictResponse(new Prediction(smiles, res.getInterval().getValue0(), res.getInterval().getValue1(), res.getY_hat(), confidence));
		} catch (IllegalAccessException | CDKException e) {
			logger.debug("Failed predicting smiles=" + smiles, e);
			CDKMutexLock.releaseLock();
			return ResponseFactory.errorResponse(500, "Server error predicting");
		}
	}



	/*
	 * =====================================================================================================================
	 * 
	 * 									MULTIPLE PREDICTIONS
	 * 
	 * =====================================================================================================================
	 */


	/**
	 * For URI
	 * @param predictURI
	 * @param confidence
	 * @return
	 */
	public static Response doUriPredict(URI predictURI, double confidence) {
		logger.info("got a prediction task, uri="+ predictURI + " , conf=" + confidence);
		if (predictURI==null){
			logger.debug("Missing arguments 'predictURI'");
			return ResponseFactory.badRequestResponse(400, "missing argument", Arrays.asList("uri"));
		}
		
		if(serverErrorResponse != null)
			return serverErrorResponse;


		// Spawn a new thread to do the work - send a Task back as 'task accepted'
		// Get the task ID from backend
		int taskID = 42; // TODO
		new Thread(new PredictRunnable(predictURI, confidence, taskID)).start();
		logger.debug("Worker thread spawn, sending Task accepted back to caller");
		
		// Send back the URI needed for query the task
		return ResponseFactory.taskAccepted("/tasks/" + taskID);
	}



	/**
	 * For dataFile
	 * @param fileInputStream
	 * @param confidence
	 * @return
	 */
	public static Response doFilePredict(InputStream fileInputStream, double confidence) {
		logger.info("got a prediction task with posted file" + " , conf=" + confidence);
		if (fileInputStream==null){
			logger.debug("Missing arguments 'fileInputStream'");
			return ResponseFactory.badRequestResponse(400, "missing argument", Arrays.asList("dataFile"));
		}
		
		if(serverErrorResponse != null)
			return serverErrorResponse;

		// We have to copy it to local (for now at least)
		File tmpPredictFile = null;
		try{
			tmpPredictFile = File.createTempFile("molecules.predict", ".tmp");
			tmpPredictFile.deleteOnExit();
			IOUtils.copyLarge(fileInputStream, new BufferedOutputStream(new FileOutputStream(tmpPredictFile)));	
		} catch (Exception e) {
			logger.debug("Could not copy POST'ed file", e);
			return ResponseFactory.badRequestResponse(400, "Failed uploading file", "dataFile");
		}

		// Spawn a new thread to do the work - send a Task back as 'task accepted'
		// Get the task ID from backend
		int taskID = 42; // TODO
		new Thread(new PredictRunnable(tmpPredictFile, confidence, taskID)).start();
		
		// Send back the URI needed for query the task
		return ResponseFactory.taskAccepted("/tasks/" + taskID);
	}

}
