package se.uu.farmbio.api.predict;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.Iterator;

import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.io.iterator.IteratingSDFReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.genettasoft.modeling.acp.ACPRegressionResult;
import com.genettasoft.modeling.app.PredictionResultOutputter;
import com.genettasoft.modeling.cheminf.SignaturesACPRegression;
import com.genettasoft.modeling.io.chemreader.IteratingSMILESWithPropertiesReader;
import com.genettasoft.modeling.io.chemreader.JSONChemFileReader;
import com.genettasoft.modeling.io.chemreader.MolReaderFactory;

public class PredictRunnable implements Runnable {

	private static SignaturesACPRegression model;
	private static final Logger logger = LoggerFactory.getLogger(PredictRunnable.class);
	
	private URI remoteURISource;
	private File uploadedFileSource;
	private boolean predictFromURI;
	private double confidence;
	private int TASK_ID;
	private File resultsFile;
	
	static {
		model = Predict.getModel();
	}


	public PredictRunnable(URI remoteURI, double confidence, int taskID) {
		this.remoteURISource = remoteURI;
		this.confidence = confidence;
		this.TASK_ID = taskID;
		this.predictFromURI = true;
	}

	public PredictRunnable(File uploadedFile, double confidence, int taskID) {
		this.uploadedFileSource = uploadedFile;
		this.confidence = confidence;
		this.TASK_ID = taskID;
		this.predictFromURI = false;
	}


	public void run() {

		// create a tmp file for storing the output - should be transfered to backend once done
		try {
			resultsFile = File.createTempFile("prediction.output." + TASK_ID, ".tmp");
		} catch (IOException e1) {
			logger.debug("Could not create a temporary file for prediction output");
			exitWithFailure("Server Error - Could not create a temporary file for prediction output");
		}
		resultsFile.deleteOnExit();


		CDKMutexLock.requireLock();

		// create an iterator
		Iterator<IAtomContainer> mols = null;
		try {
			mols = MolReaderFactory.getIterator((predictFromURI? remoteURISource : uploadedFileSource.toURI()));
			if(! mols.hasNext())
				exitWithFailure("No molecules successfully parsed from given input");
		} catch (IOException e) {
			if(predictFromURI) {
				logger.debug("Could not parse the given URI: " + remoteURISource,e);
				exitWithFailure("Could not read from the given URI: " + remoteURISource);
			} else {
				logger.debug("Could not parse the given dataFile",e);
				exitWithFailure("Could not read from the uploaded file");
			}
		}

		// create the output
		PredictionResultOutputter outputter = null;
		try {
			if (mols instanceof IteratingSMILESWithPropertiesReader) {
				outputter = PredictionResultOutputter.getSMILESOutputter(resultsFile, false, ((IteratingSMILESWithPropertiesReader) mols).getColumnHeaders(), null);
			} else if (mols instanceof IteratingSDFReader){
				outputter = PredictionResultOutputter.getSDFOutputter(resultsFile, false, null);
			} else if (mols instanceof JSONChemFileReader){
				outputter = PredictionResultOutputter.getJSONOutputter(resultsFile, false, null);
			}
		} catch(IOException e) {
			logger.debug("Could not initiate the PredictionResultOutputter", e);
			exitWithFailure("Server Error - could not initiate the result file outputter");
		}


		IAtomContainer mol;
		int numSuccess=0, numFailed=0;

		while (mols.hasNext()){
			mol = mols.next();

			try{
				ACPRegressionResult res = model.predict(mol, confidence);
				mol.setProperty("lower", res.getInterval().getValue(0));
				mol.setProperty("higher", res.getInterval().getValue(1));
				mol.setProperty("predictionMidpoint", res.getY_hat());
				outputter.writeMol(mol, null, null, null);
				numSuccess++;
			} catch (Exception e) {
				numFailed++;
			}

		}
		if (numSuccess == 0) {
			if (numFailed >0)
				exitWithFailure("Could not successfully predict any compounds, failed with " + numFailed + " compounds");
			else 
				exitWithFailure("Could not successfully predict any compounds");
		}
		
		// Release the lock
		CDKMutexLock.releaseLock();
		
		// Successfully end - update backend with data and then remove the temporary file TODO
		
		
	}
	
	/**
	 * Make sure to explicitly delete any results to release memory and release
	 * the CDK-lock for serialization
	 */
	private void exitWithFailure(String errorMsg){
		if(resultsFile!=null)
			resultsFile.delete();
		CDKMutexLock.releaseLock();
		// Update backend with error-message TODO
	}

}
