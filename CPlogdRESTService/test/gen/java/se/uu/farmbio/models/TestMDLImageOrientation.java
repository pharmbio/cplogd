package se.uu.farmbio.models;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;

import javax.ws.rs.core.Response;

import org.apache.commons.io.IOUtils;
import org.javatuples.Pair;
import org.junit.Assert;
import org.junit.Test;
import org.openscience.cdk.interfaces.IAtomContainer;

import com.genettasoft.chem.io.out.MoleculeFigure;
import com.genettasoft.chem.io.out.MoleculeGradientDepictor;
import com.genettasoft.chem.io.out.MoleculeFigure.GradientFigureBuilder;
import com.genettasoft.depict.GradientFactory;

import se.uu.farmbio.api.predict.ChemUtils;

public class TestMDLImageOrientation {
	
	@Test
	public void testOrientation() throws Exception {
		Pair<IAtomContainer, Response> molOrResp = ChemUtils.parseMolecule(IOUtils.toString(new File("/Users/staffan/Downloads/new.txt").toURI(), StandardCharsets.UTF_8));
		
		Assert.assertNull(molOrResp.getValue1());
		Assert.assertNotNull(molOrResp.getValue0());
		
		MoleculeGradientDepictor depictor = new MoleculeGradientDepictor(GradientFactory.getDefaultBloomGradient());
		depictor.setImageHeight(400);
		depictor.setImageWidth(400);
		GradientFigureBuilder builder = new GradientFigureBuilder(depictor);
		MoleculeFigure fig = builder.build(molOrResp.getValue0(), new HashMap<Object,Double>());
		fig.saveToFile(new File("/Users/staffan/Downloads/cplogD.png"));
	}

}
