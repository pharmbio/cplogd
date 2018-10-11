package se.uu.farmbio.api.predict;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.security.InvalidKeyException;
import java.util.Arrays;

import javax.imageio.ImageIO;
import javax.ws.rs.core.Response;

import org.javatuples.Pair;
import org.openscience.cdk.geometry.GeometryUtil;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.genettasoft.chem.io.out.MoleculeFigure.GradientFigureBuilder;
import com.genettasoft.chem.io.out.MoleculeGradientDepictor;
import com.genettasoft.chem.io.out.fields.ColorGradientField;
import com.genettasoft.chem.io.out.fields.PredictionIntervalField;
import com.genettasoft.chem.io.out.fields.TitleField;
import com.genettasoft.depict.GradientFactory;
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

	private static final int MIN_IMAGE_SIZE = 50;
	private static final int MAX_IMAGE_SIZE = 5000;
	private static final String URL_ENCODING = "UTF-8";

	private static Response serverErrorResponse = null;
	private static SignaturesCPRegression signaturesACPReg = null;

	public static SignaturesCPRegression getModel() {
		return signaturesACPReg;
	}

	static {
		System.setProperty("awt.useSystemAAFontSettings", "lcd");
		System.setProperty("swing.aatext", "true");
		System.setProperty("sun.java2d.xrender","true");

		// Get the root logger for cpsign
		Logger cpsingLogger =  org.slf4j.LoggerFactory.getLogger("com.genettasoft.modeling");
		if(cpsingLogger instanceof ch.qos.logback.classic.Logger) {
			ch.qos.logback.classic.Logger cpsignRoot = (ch.qos.logback.classic.Logger) cpsingLogger;
			// Disable all cpsign-output
			cpsignRoot.setLevel(ch.qos.logback.classic.Level.WARN);
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

	public static Response doSinglePredict(String molecule, double confidence) {
		logger.info("got a prediction task, conf=" + confidence);

		if (serverErrorResponse != null)
			return serverErrorResponse;

		if (confidence < 0 || confidence > 1){
			logger.warn("invalid argument confidence=" + confidence);
			return ResponseFactory.badRequestResponse(400, "invalid argument", Arrays.asList("confidence"));
		}

		if (molecule==null || molecule.isEmpty()){
			logger.warn("Missing arguments 'molecule'");
			return ResponseFactory.badRequestResponse(400, "missing argument", Arrays.asList("molecule"));
		}

		// Clean the molecule - Text
		try {
			if (molecule != null && !molecule.isEmpty())
				molecule = URLDecoder.decode(molecule, URL_ENCODING);
		} catch (Exception e) {
			return ResponseFactory.badRequestResponse(400, "Could not decode molecule text", Arrays.asList("molecule"));
		}
		if (molecule.split("\n").length > 1)
			logger.info("MDL file:\n"+molecule);

		// try to parse an IAtomContainer - or fail
		Pair<IAtomContainer, Response> molOrFail = ChemUtils.parseMolecule(molecule);
		if (molOrFail.getValue1() != null)
			return molOrFail.getValue1();

		IAtomContainer molToPredict=molOrFail.getValue0();

		String smiles = null;
		try {
			smiles = ChemUtils.getAsSmiles(molToPredict, molecule);
		} catch (Exception e) {
			logger.debug("Failed creating smiles from IAtomContainer",e);
			return ResponseFactory.errorResponse(400, "Could not generate SMILES for molecule");
		}

		CDKMutexLock.requireLock();

		try{
			// Do prediction
			try {
				CPRegressionResult res = signaturesACPReg.predict(molToPredict, confidence);
				logger.info("Successfully finished predicting smiles="+smiles+", interval=" + res.getInterval() + ", conf=" + confidence);
				return ResponseFactory.predictResponse(new Prediction(smiles, res.getInterval().getValue0(), res.getInterval().getValue1(), res.getY_hat(), confidence));
			} catch (Exception | Error e) {
				logger.warn("Failed predicting smiles=" + smiles, e);
				return ResponseFactory.errorResponse(500, "Server error predicting");
			}
		} finally {
			CDKMutexLock.releaseLock();
		}
	}

	public static Response doImagePredict(String molecule, Double conf, int imageWidth, int imageHeight, boolean addTitle) {
		logger.info("got a predict-image task, conf="+conf+", imageWidth="+imageWidth+", imageHeight="+imageHeight);

		if(serverErrorResponse != null)
			return serverErrorResponse;

		if (conf != null && (conf < 0 || conf > 1)){
			logger.warn("invalid argument confidence=" + conf);
			return ResponseFactory.badRequestResponse(400, "invalid argument", Arrays.asList("confidence"));
		}

		if (imageWidth < MIN_IMAGE_SIZE || imageHeight < MIN_IMAGE_SIZE){
			logger.warn("Failing execution due to too small image required");
			return ResponseFactory.badRequestResponse(400, "image height and with must be at least "+MIN_IMAGE_SIZE+" pixels", Arrays.asList("imageWidth", "imageHeight"));
		}

		if (imageWidth > MAX_IMAGE_SIZE || imageHeight> MAX_IMAGE_SIZE){
			logger.warn("Failing execution due to too large image requested");
			return ResponseFactory.badRequestResponse(400, "image height and width can maximum be "+MAX_IMAGE_SIZE+" pixels", Arrays.asList("imageWidth", "imageHeight"));
		}

		// Return empty img if no smiles sent
		if (molecule==null || molecule.isEmpty()){
			// return an empty img
			try{
				BufferedImage image = new BufferedImage(imageWidth, imageHeight, BufferedImage.TYPE_INT_ARGB);
				Graphics2D g2d = image.createGraphics();
				g2d.setColor(Color.WHITE);
				g2d.fillRect(0, 0, image.getWidth(), image.getHeight());
				g2d.dispose();
				ByteArrayOutputStream baos = new ByteArrayOutputStream();
				ImageIO.write(image, "png", baos);
				byte[] imageData = baos.toByteArray();

				return Response.ok( new ByteArrayInputStream(imageData) ).build();
			} catch ( IOException e) {
				logger.info("Failed returning empty image for empty smiles");
				return ResponseFactory.errorResponse(500, "Server error");
			}
		}

		// Clean the molecule - Text
		try {
			if (molecule != null && !molecule.isEmpty())
				molecule = URLDecoder.decode(molecule, URL_ENCODING);
		} catch (Exception e) {
			return ResponseFactory.badRequestResponse(400, "Could not decode molecule text", Arrays.asList("molecule"));
		}
		if (molecule.split("\n").length > 1)
			logger.info("MDL file:\n"+molecule);


		// try to parse an IAtomContainer - or fail
		Pair<IAtomContainer, Response> molOrFail = ChemUtils.parseMolecule(molecule);
		if (molOrFail.getValue1() != null)
			return molOrFail.getValue1();

		IAtomContainer molToPredict=molOrFail.getValue0();

		if (GeometryUtil.has2DCoordinates(molToPredict))
			logger.info("Molecule has 2D coordinates pre-calculated");

		// Get smiles representation of molecule
		String smiles = null;
		try {
			smiles = ChemUtils.getAsSmiles(molToPredict, molecule);
		} catch (Exception e) {
			logger.debug("Failed creating smiles from IAtomContainer",e);
			return ResponseFactory.errorResponse(400, "Could not generate SMILES for molecule");
		}


		CDKMutexLock.requireLock();
		try {

			// CALCULATE GRADIENT & GENERATE IMAGE
			SignificantSignature signSign = null;

			try {
				signSign = signaturesACPReg.predictSignificantSignature(molToPredict);
				MoleculeGradientDepictor depictor = new MoleculeGradientDepictor(GradientFactory.getDefaultBloomGradient());
				depictor.setImageHeight(imageHeight);
				depictor.setImageWidth(imageWidth);
				GradientFigureBuilder builder = new GradientFigureBuilder(depictor);
				if (addTitle)
					builder.addFieldOverImg(new TitleField("Chembl23 cpLogD"));

				// add confidence interval only if given confidence and image size is big enough
				if (conf != null && imageWidth>80){
					CPRegressionResult res = signaturesACPReg.predict(molToPredict, conf);
					builder.addFieldUnderImg(new PredictionIntervalField(res.getInterval(), conf));
				}
				builder.addFieldUnderImg(new ColorGradientField(depictor.getColorGradient()));

				BufferedImage image = builder.build(molToPredict, signSign.getAtomValues()).getImage();
				//			 BufferedImage image = depictor.depict(molToPredict, signSign.getAtomValues())

				ByteArrayOutputStream baos = new ByteArrayOutputStream();
				ImageIO.write(image, "png", baos);
				byte[] imageData = baos.toByteArray();

				return Response.ok( new ByteArrayInputStream(imageData) ).build();
			}
			catch (Exception | Error e) {
				logger.warn("Failed predicting smiles=" + smiles, e);
				return ResponseFactory.errorResponse(500, "Server error");
			}
		}
		finally {
			CDKMutexLock.releaseLock();
		}
	}
}
