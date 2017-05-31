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


		//		// Iterator over molecules
		//		Iterator<IAtomContainer> molsIterator = null;
		//		CDKMutexLock.requireLock();
		//		try{
		//			molsIterator = MolReaderFactory.getIterator(predictURI);
		//			if(!molsIterator.hasNext()){
		//				logger.debug("Could not parse any molecules from URI");
		//				CDKMutexLock.releaseLock();
		//				return ResponseFactory.badRequestResponse(400, "No molecules found in uri", Arrays.asList("uri"));
		//			}
		//		} catch (Exception e) {
		//			logger.debug("Could not parse the URI at all");
		//			CDKMutexLock.releaseLock();
		//			return ResponseFactory.badRequestResponse(400, "URI could not be read", Arrays.asList("uri"));
		//		}
		//
		//		CDKMutexLock.releaseLock();

		//		boolean isSMILESInput = (molsIterator instanceof IteratingSMILESWithPropertiesReader ? true : false	);
		//		boolean isSDFInput = (molsIterator instanceof IteratingSDFReader ? true : false);
		//		boolean isJSONInput = (molsIterator instanceof JSONChemFileReader ? true : false);

		// Setup results output
		//		File tmpResultFile = File.createTempFile("prediction_output", ".tmp");
		//		tmpResultFile.deleteOnExit();

		//		doMultiPredict(molsIterator, outputter, confidence)

		return ResponseFactory.taskAccepted("taskURI - TODO");
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

		// Iterator over molecules
		//		Iterator<IAtomContainer> molsIterator = null;
		//		CDKMutexLock.requireLock();
		//		try{
		//			molsIterator = MolReaderFactory.getIterator(tmpPredictFile.toURI());
		//			if(!molsIterator.hasNext()){
		//				logger.debug("Could not parse any molecules from dataFile");
		//				CDKMutexLock.releaseLock();
		//				return ResponseFactory.badRequestResponse(400, "No molecules found in file", Arrays.asList("dataFile"));
		//			}
		//		} catch (Exception e) {
		//			logger.debug("Could not parse the dataFile at all");
		//			CDKMutexLock.releaseLock();
		//			return ResponseFactory.badRequestResponse(400, "dataFile could not be read", Arrays.asList("dataFile"));
		//		}
		//		CDKMutexLock.releaseLock();


		//		boolean isSMILESInput = (molsIterator instanceof IteratingSMILESWithPropertiesReader ? true : false	);
		//		boolean isSDFInput = (molsIterator instanceof IteratingSDFReader ? true : false);
		//		boolean isJSONInput = (molsIterator instanceof JSONChemFileReader ? true : false);

		//		IAtomContainer molToPredict=null;
		//		try{
		//			molToPredict = CPSignFactory.parseSMILES(smiles);
		//		} catch(IllegalArgumentException e){
		//			logger.debug("Got exception when parsing smiles: " + e.getMessage() + "\nreturning error-msg and stopping");
		//			return ResponseFactory.badRequestResponse(400, "Invalid query SMILES:" + smiles, Arrays.asList("smiles"));
		//		}

		//		try {
		//			ACPRegressionResult res = signaturesACPReg.predict(molToPredict, confidence);
		//			return ResponseFactory.predictResponse(new Prediction(smiles, res.getInterval().getValue0(), res.getInterval().getValue1(), res.getY_hat()));
		//		} catch (IllegalAccessException | CDKException e) {
		//			logger.debug("Failed predicting smiles=" + smiles, e);
		//			return ResponseFactory.errorResponse(500, "Server error predicting");
		//		}

		return ResponseFactory.taskAccepted("Task accepted - TODO");
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
