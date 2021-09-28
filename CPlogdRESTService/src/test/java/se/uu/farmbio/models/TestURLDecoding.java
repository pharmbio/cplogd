package se.uu.farmbio.models;

import java.net.MalformedURLException;

import org.junit.Test;
import org.openscience.cdk.exception.InvalidSmilesException;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.silent.SilentChemObjectBuilder;
import org.openscience.cdk.smiles.SmilesParser;

import se.uu.farmbio.api.predict.Utils;

public class TestURLDecoding {
	
	@Test
	public void testOriginalSMILES() throws InvalidSmilesException {
		String theSMILES = "FC(C=C1)=CC=C1C2=CN(C3=N2)N=C(N4CC[N+](C)([H])CC4)C=C3C5=CN(C)N=C5.[Cl-]";
		IAtomContainer mol = new SmilesParser(SilentChemObjectBuilder.getInstance()).parseSmiles(theSMILES);
	}
	
	@Test
	public void testWhenDecoded() throws InvalidSmilesException, MalformedURLException {
		String theSMILES = "Cn5cc%28c3cc%28N1CC%5BNH%2B%5D%28C%29CC1%29nn4cc%28c2ccc%28F%29cc2%29nc34%29cn5.%5BCl-%5D";
		String decoded = Utils.decodeURL(theSMILES);
		System.err.println("decoded: " + decoded);
		IAtomContainer mol = new SmilesParser(SilentChemObjectBuilder.getInstance()).parseSmiles(decoded);
	}
	
	@Test
	public void testWhenDecoded_original_SMILES() throws InvalidSmilesException, MalformedURLException {
		String theSMILES = "FC(C%3DC1)%3DCC%3DC1C2%3DCN(C3%3DN2)N%3DC(N4CC%5BN%2B%5D(C)(%5BH%5D)CC4)C%3DC3C5%3DCN(C)N%3DC5.%5BCl-%5D";
		String decoded = Utils.decodeURL(theSMILES);
		System.err.println("decoded: " + decoded);
		IAtomContainer mol = new SmilesParser(SilentChemObjectBuilder.getInstance()).parseSmiles(decoded);
	}

}
