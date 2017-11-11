package se.uu.farmbio.api.predict;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.Iterator;

import org.openscience.cdk.interfaces.IAtomContainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.genettasoft.modeling.cheminf.SignaturesCPRegression;
import com.genettasoft.modeling.io.chemreader.MolReaderFactory;

public class PredictRunnable implements Runnable {

	private static SignaturesCPRegression model;
	private static final Logger logger = LoggerFactory.getLogger(PredictRunnable.class);

	private URI remoteURISource;
	private File uploadedFileSource;
	private boolean predictFromURI;
	private double confidence;
	private String TASK_ID;
	private File resultsFile;

	static {
		model = Predict.getModel();
	}


	public PredictRunnable(URI remoteURI, double confidence, String taskID) {
		this.remoteURISource = remoteURI;
		this.confidence = confidence;
		this.TASK_ID = taskID;
		this.predictFromURI = true;
	}

	public PredictRunnable(File uploadedFile, double confidence, String taskID) {
		this.uploadedFileSource = uploadedFile;
		this.confidence = confidence;
		this.TASK_ID = taskID;
		this.predictFromURI = false;
	}


	public void run() {

		logger.info("Thread started file/uri prediction");

		try{
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
				if(predictFromURI) 
					exitWithFailure("Could not read from the given URI: " + remoteURISource);
				else 
					exitWithFailure("Could not read from the uploaded file");

			}

			// create the output
//			PredictionResultOutputter outputter = null;
//			try {
//				if (mols instanceof IteratingSMILESWithPropertiesReader) {
//					outputter = PredictionResultOutputter.getSMILESOutputter(resultsFile, false, ((IteratingSMILESWithPropertiesReader) mols).getColumnHeaders(), null);
//				} else if (mols instanceof IteratingSDFReader){
//					outputter = PredictionResultOutputter.getSDFOutputter(resultsFile, false, null);
//				} else if (mols instanceof JSONChemFileReader){
//					outputter = PredictionResultOutputter.getJSONOutputter(resultsFile, false, null);
//				}
//			} catch(IOException e) {
//				logger.debug("Could not initiate the PredictionResultOutputter", e);
//				exitWithFailure("Server Error - could not initiate the result file outputter");
//			}


			IAtomContainer mol;
			int numSuccess=0, numFailed=0;
			
//			TODO
//			while (mols.hasNext()){
//				mol = mols.next();
//
//				try{
//					CPRegressionResult res = model.predict(mol, confidence);
//					mol.setProperty("lower", res.getInterval().getValue(0));
//					mol.setProperty("higher", res.getInterval().getValue(1));
//					mol.setProperty("predictionMidpoint", res.getY_hat());
//					mol.setProperty("confidence", confidence);
//					outputter.writeMol(mol, null, null, null);
//					numSuccess++;
//				} catch (Exception e) {
//					numFailed++;
//				}
//
//			}
			if (numSuccess == 0) {
				if (numFailed >0)
					exitWithFailure("Could not successfully predict any compounds, failed with " + numFailed + " compounds");
				else 
					exitWithFailure("Could not successfully predict any compounds");
			}

			// Release the lock
			CDKMutexLock.releaseLock();

			// Successfully end - update backend with data and then remove the temporary file TODO
			logger.debug("Thread for task '" + TASK_ID + "' successfully finished prediction of file/uri with " + numSuccess + " molecules");

		} catch (ExitThreadExecution e) {
			// Backend have been updated, time for thread to die
		} catch (Exception e) {
			try {
				exitWithFailure(e.getMessage());
			} catch (ExitThreadExecution e2) {
				// Backend have been updated, time for thread to die
			}
		}

	}

	/**
	 * Make sure to explicitly delete any results to release memory and release
	 * the CDK-lock for serialization
	 */
	private void exitWithFailure(String errorMsg) throws ExitThreadExecution {
		logger.info("Thread called exitWithFailure with msg: " + errorMsg);
		if(resultsFile!=null)
			resultsFile.delete();
		CDKMutexLock.releaseLock();
		// Update backend with error-message TODO

		throw new ExitThreadExecution();
	}

	public class ExitThreadExecution extends RuntimeException {

		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

	}

}
