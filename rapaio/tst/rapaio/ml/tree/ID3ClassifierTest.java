
package rapaio.ml.tree;

import junit.framework.Assert;
import org.junit.Test;
import rapaio.data.Frame;
import rapaio.data.filters.BaseFilters;
import rapaio.datasets.Datasets;
import rapaio.ml.tools.DensityTable;
import rapaio.workspace.Summary;

import java.io.IOException;

/**
 * @author <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a>
 */
public class ID3ClassifierTest {

	@Test
	public void testBasicID3Entropy() throws IOException {
		Frame df = Datasets.loadPlay();
		df = BaseFilters.retainNominal(df);
		final String className = "class";

		ID3Classifier id3 = new ID3Classifier().setSelection(ID3Classifier.SELECTION_ENTROPY);
		id3.learn(df, className);
		id3.predict(df);

		DensityTable dtWindy = new DensityTable(df, "windy", "class");
		DensityTable dtOutlook = new DensityTable(df, "outlook", "class");
		String splitCol = (dtWindy.getInfoXGain() < dtOutlook.getInfoXGain()) ? "windy" : "outlook";
		Assert.assertEquals(splitCol, id3.root.splitCol);

		Summary.summary(id3);
	}

	@Test
	public void testBasicID3InfoGain() throws IOException {
		Frame df = Datasets.loadPlay();
		df = BaseFilters.retainNominal(df);
		final String className = "class";

		ID3Classifier id3 = new ID3Classifier().setSelection(ID3Classifier.SELECTION_INFOGAIN);
		id3.learn(df, className);
		id3.predict(df);

		DensityTable dtWindy = new DensityTable(df, "windy", "class");
		DensityTable dtOutlook = new DensityTable(df, "outlook", "class");
		String splitCol = (dtWindy.getInfoGain() > dtOutlook.getInfoGain()) ? "windy" : "outlook";
		Assert.assertEquals(splitCol, id3.root.splitCol);

		Summary.summary(id3);
	}
}
