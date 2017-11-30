package se.uu.farmbio.api.predict;

import java.awt.image.BufferedImage;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.InvalidKeyException;
import java.util.Arrays;

import javax.imageio.ImageIO;
import javax.ws.rs.core.Response;

import org.apache.commons.io.IOUtils;
import org.openscience.cdk.exception.CDKException;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.genettasoft.chem.io.out.MoleculeFigure.GradientFigureBuilder;
import com.genettasoft.chem.io.out.MoleculeGradientDepictor;
import com.genettasoft.chem.io.out.fields.ColorGradientField;
import com.genettasoft.chem.io.out.fields.PredictionIntervalField;
import com.genettasoft.chem.io.out.fields.TitleField;
import com.genettasoft.depict.GradientFactory;
import com.genettasoft.modeling.CPSignFactory;
import com.genettasoft.modeling.cheminf.SignaturesCPRegression;
import com.genettasoft.modeling.cheminf.SignificantSignature;
import com.genettasoft.modeling.io.bndTools.BNDLoader;
import com.genettasoft.modeling.ml.cp.CPRegressionResult;

import se.uu.farmbio.api.responses.ResponseFactory;
import se.uu.farmbio.models.Prediction;

public class Predict {

	private static final Logger logger = LoggerFactory.getLogger(Predict.class);
	private static final String MODEL_SPLIT_1 = "Chembl23_1.cpsign";
	private static final String MODEL_SPLIT_2 = "Chembl23_2.cpsign";

	private static Response serverErrorResponse = null;
	private static SignaturesCPRegression signaturesACPReg = null;

	public static SignaturesCPRegression getModel() {
		return signaturesACPReg;
	}

	static {

		// Get the root logger for cpsign
		Logger cpsingLogger =  org.slf4j.LoggerFactory.getLogger("com.genettasoft.modeling");
		if(cpsingLogger instanceof ch.qos.logback.classic.Logger) {
			ch.qos.logback.classic.Logger cpsignRoot = (ch.qos.logback.classic.Logger) cpsingLogger;
			// Disable all cpsign-output
			cpsignRoot.setLevel(ch.qos.logback.classic.Level.OFF);
		}

		// Enable debug output for this library
		Logger cpLogDLogging = org.slf4j.LoggerFactory.getLogger("se.uu.farmbio");
		if(cpLogDLogging instanceof ch.qos.logback.classic.Logger) {
			ch.qos.logback.classic.Logger cpLogDLogger = (ch.qos.logback.classic.Logger) cpLogDLogging;
			cpLogDLogger.setLevel(ch.qos.logback.classic.Level.DEBUG);
		}

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
				URI modelURI_1 = Predict.class.getClassLoader().getResource(MODEL_SPLIT_1).toURI();
				URI modelURI_2 = Predict.class.getClassLoader().getResource(MODEL_SPLIT_2).toURI();
				if(modelURI_1 == null || modelURI_2 == null)
					throw new IOException("did not locate the model file");
				signaturesACPReg = (SignaturesCPRegression) BNDLoader.loadModel(modelURI_1, null);
				logger.info("Loaded first split of model");
				signaturesACPReg.addModel(modelURI_2, null);
				logger.info("Loaded second split of model");
				logger.info("Finished initializing the server with the loaded model");
			} catch (IllegalAccessException | IOException | URISyntaxException | InvalidKeyException | IllegalArgumentException e) {
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
		logger.debug("got a prediction task, smiles="+smiles + " , conf=" + confidence);

		if(serverErrorResponse != null)
			return serverErrorResponse;

		if(confidence < 0 || confidence > 1){
			logger.debug("invalid argument confidence=" + confidence);
			return ResponseFactory.badRequestResponse(400, "invalid argument", Arrays.asList("confidence"));
		}

		if (smiles==null || smiles.isEmpty()){
			logger.debug("Missing arguments 'smiles'");
			return ResponseFactory.badRequestResponse(400, "missing argument", Arrays.asList("smiles"));
		}

		IAtomContainer molToPredict=null;
		CDKMutexLock.requireLock();
		try{
			// Parse the SMILES
			try{
				molToPredict = CPSignFactory.parseSMILES(smiles);
			} catch(IllegalArgumentException e){
				logger.debug("Got exception when parsing smiles: " + e.getMessage() + "\nreturning error-msg and stopping", e);
				return ResponseFactory.badRequestResponse(400, "Invalid query SMILES '" + smiles + "'", Arrays.asList("smiles"));
			}
			// Do prediction
			try {
				CPRegressionResult res = signaturesACPReg.predict(molToPredict, confidence);
				logger.debug("Successfully finished predicting smiles="+smiles+", interval=" + res.getInterval() + ", conf=" + confidence);
				return ResponseFactory.predictResponse(new Prediction(smiles, res.getInterval().getValue0(), res.getInterval().getValue1(), res.getY_hat(), confidence));
			} catch (IllegalAccessException | CDKException e) {
				logger.debug("Failed predicting smiles=" + smiles, e);
				return ResponseFactory.errorResponse(500, "Server error predicting");
			}
		} finally {
			CDKMutexLock.releaseLock();
		}
	}

	public static Response doImagePredict(String smiles, double conf, int imageWidth, int imageHeight) {
		logger.debug("got a predict-image task, smiles="+smiles +", imageWidth="+imageWidth+", imageHeight="+imageHeight);

		if(serverErrorResponse != null)
			return serverErrorResponse;
		
		if(conf < 0 || conf > 1){
			logger.debug("invalid argument confidence=" + conf);
			return ResponseFactory.badRequestResponse(400, "invalid argument", Arrays.asList("confidence"));
		}

		if (imageWidth > 5000 || imageHeight> 5000)
			return ResponseFactory.badRequestResponse(400, "image height and width can maximum be 5000 pixels", Arrays.asList("imageWidth", "imageHeight"));
		
		// Return empty img if no smiles sent
		if (smiles==null || smiles.isEmpty()){
			// return an empty img
			try{
				BufferedImage image = new BufferedImage(imageWidth, imageHeight, BufferedImage.TYPE_4BYTE_ABGR);
				//			 BufferedImage image = depictor.depict(molToPredict, signSign.getAtomValues())

				ByteArrayOutputStream baos = new ByteArrayOutputStream();
				ImageIO.write(image, "png", baos);
				byte[] imageData = baos.toByteArray();

				return Response.ok( new ByteArrayInputStream(imageData) ).build();
			} catch ( IOException e) {
				logger.debug("Failed returning empty image for empty smiles");
				return ResponseFactory.errorResponse(500, "Server error");
			}
		}


		IAtomContainer molToPredict=null;
		CDKMutexLock.requireLock();
		try {
			// Parse SMILES
			try{
				molToPredict = CPSignFactory.parseSMILES(smiles);
			} catch(IllegalArgumentException e){
				logger.debug("Got exception when parsing smiles: " + e.getMessage() + "\nreturning error-msg and stopping", e);
				return ResponseFactory.badRequestResponse(400, "Invalid query SMILES '" + smiles + "'", "smiles");
			}

			// CALCULATE GRADIENT & GENERATE IMAGE
			SignificantSignature signSign = null;

			try {
				signSign = signaturesACPReg.predictSignificantSignature(molToPredict);
				MoleculeGradientDepictor depictor = new MoleculeGradientDepictor(GradientFactory.getDefaultBloomGradient());
				depictor.setImageHeight(imageHeight);
				depictor.setImageWidth(imageWidth);
				GradientFigureBuilder builder = new GradientFigureBuilder(depictor);
				builder.addFieldOverImg(new TitleField(signaturesACPReg.getModelName()));
				
				CPRegressionResult res = signaturesACPReg.predict(molToPredict, conf);
				builder.addFieldUnderImg(new PredictionIntervalField(res.getInterval(), conf));
				builder.addFieldUnderImg(new ColorGradientField(depictor.getColorGradient()));

				BufferedImage image = builder.build(molToPredict, signSign.getAtomValues()).getImage();
				//			 BufferedImage image = depictor.depict(molToPredict, signSign.getAtomValues())

				ByteArrayOutputStream baos = new ByteArrayOutputStream();
				ImageIO.write(image, "png", baos);
				byte[] imageData = baos.toByteArray();

				return Response.ok( new ByteArrayInputStream(imageData) ).build();
			}
			catch (IllegalAccessException | CDKException | IOException e) {
				logger.debug("Failed predicting smiles=" + smiles, e);
				return ResponseFactory.errorResponse(500, "Server error");
			}
		}
		finally {
			CDKMutexLock.releaseLock();
		}
	}




	/*
	 * =====================================================================================================================
	 * 
	 * 									MULTIPLE PREDICTIONS (CURRENTLY NOT AVAILIABLE)
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
		String taskID = "42"; // TODO
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
		String taskID = "42"; // TODO
		new Thread(new PredictRunnable(tmpPredictFile, confidence, taskID)).start();

		// Send back the URI needed for query the task
		return ResponseFactory.taskAccepted("/tasks/" + taskID);
	}

}
