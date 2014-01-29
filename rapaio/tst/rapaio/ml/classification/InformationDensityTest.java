package rapaio.ml.classification;

import org.junit.Test;
import rapaio.data.Frame;
import rapaio.datasets.Datasets;

import java.io.IOException;

import static org.junit.Assert.assertEquals;

/**
 * @author <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a>
 */
public class InformationDensityTest {

	@Test
	public void testPlayNoMissing() throws IOException {

		Frame df = Datasets.loadPlay();

		DensityTable id = new DensityTable(df, "outlook", "class");
		assertEquals(0.940, id.getEntropy(), 1e-3);
		assertEquals(0.694, id.getInfoXGain(), 1e-3);
		assertEquals(0.246, id.getInfoGain(), 1e-3);

		assertEquals(1.577, id.getSplitInfo(), 1e-3);
		assertEquals(0.156, id.getGainRatio(), 1e-3);

		id = new DensityTable(df, "windy", "class");
		assertEquals(0.940, id.getEntropy(), 1e-3);
		assertEquals(0.892, id.getInfoXGain(), 1e-3);
		assertEquals(0.048, id.getInfoGain(), 1e-3);

		assertEquals(0.985, id.getSplitInfo(), 1e-3);
		assertEquals(0.048, id.getGainRatio(), 1e-3);
	}

	@Test
	public void testPlayWithMissing() throws IOException {

		Frame df = Datasets.loadPlay();
		df.getCol("outlook").setMissing(5);

		DensityTable id = new DensityTable(df, "outlook", "class");
		assertEquals(0.892, id.getEntropy(true), 1e-3);
		assertEquals(0.693, id.getInfoXGain(true), 1e-3);
		assertEquals(0.199, id.getInfoGain(true), 1e-3);

		assertEquals(1.809, id.getSplitInfo(true), 1e-3);
		assertEquals(0.110, id.getGainRatio(true), 1e-3);
	}
}
