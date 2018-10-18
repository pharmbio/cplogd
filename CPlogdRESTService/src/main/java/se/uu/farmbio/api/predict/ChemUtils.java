package se.uu.farmbio.api.predict;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Arrays;

import javax.ws.rs.core.Response;

import org.javatuples.Pair;
import org.openscience.cdk.exception.CDKException;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.io.MDLV2000Reader;
import org.openscience.cdk.io.MDLV3000Reader;
import org.openscience.cdk.silent.AtomContainer;
import org.openscience.cdk.smiles.SmiFlavor;
import org.openscience.cdk.smiles.SmilesGenerator;
import org.slf4j.Logger;

import com.genettasoft.modeling.CPSignFactory;

import se.uu.farmbio.models.BadRequestError;

public class ChemUtils {

	private static Logger logger = org.slf4j.LoggerFactory.getLogger(ChemUtils.class);
	private static SmilesGenerator sg = new SmilesGenerator(SmiFlavor.Canonical);

	public static Pair<IAtomContainer, Response> parseMolecule(String moleculeData) {

		if (moleculeData==null || moleculeData.isEmpty())
			return new Pair<>(null, Response.status(400).entity( new BadRequestError(400, "Invalid query molecule '" + moleculeData + "'", Arrays.asList("molecule")).toString() ).build());
		try {
			CDKMutexLock.requireLock();

			if (moleculeData.split("\\s",2).length > 1) {
				// MDL file format
				IAtomContainer mol = null;
				if (moleculeData.contains("V2000")) {
					logger.debug("molecule data given in MDL v2000 format");
					try (MDLV2000Reader reader = new MDLV2000Reader(new ByteArrayInputStream(moleculeData.getBytes()));){
						mol = reader.read(new AtomContainer());
					} catch (CDKException | IOException e) {
						logger.debug("Failed to read molecule as MDL v2000");
						return new Pair<>(null, Response.status(400).entity( new BadRequestError(400, "Invalid query MDL", Arrays.asList("molecule")).toString() ).build());
					} 
				} else if (moleculeData.contains("V3000")) {
					logger.debug("molecule data given in MDL v3000 format");
					try (MDLV3000Reader reader = new MDLV3000Reader(new ByteArrayInputStream(moleculeData.getBytes()));){
						mol = reader.read(new AtomContainer());
					} catch (CDKException | IOException e) {
						logger.debug("Failed to read molecule as MDL 3000");
						return new Pair<>(null, Response.status(400).entity( new BadRequestError(400, "Invalid query MDL", Arrays.asList("molecule")).toString() ).build());
					} 
				} else {
					return new Pair<>(null, Response.status(400).entity( new BadRequestError(400, "molecule given in unrecognized format", Arrays.asList("molecule")).toString() ).build());
				}

				return new Pair<>(mol, null);

			} else {
				// Simply a single SMILES
				try {
					return new Pair<>(CPSignFactory.parseSMILES(moleculeData), null);
				} catch(IllegalArgumentException e){
					logger.debug("Got exception when parsing smiles:\n" + Utils.getStackTrace(e));
					return new Pair<> (null,Response.status(400).entity( new BadRequestError(400, "Invalid query SMILES '" + moleculeData + "'", Arrays.asList("molecule")).toString() ).build());
				} 

			}
		} finally {
			CDKMutexLock.releaseLock();
		}

	}
	
	public static synchronized String getAsSmiles(IAtomContainer mol, String originalMol) throws CDKException {
		if (originalMol.split("\\s",2).length > 1)
			return originalMol;
		return sg.create(mol);
	}

}
