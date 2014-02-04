package rapaio.ml.tree;

import junit.framework.Assert;
import org.junit.Test;
import rapaio.core.stat.ConfusionMatrix;
import rapaio.data.Frame;
import rapaio.data.filters.BaseFilters;
import rapaio.datasets.Datasets;
import rapaio.ml.tools.DensityTable;
import rapaio.workspace.Summary;

import java.io.IOException;

/**
 * User: Aurelian Tutuianu <padreati@yahoo.com>
 */
public class C45ClassifierTest {

	//	@Test
	public void testNominalInfoGain() throws IOException {
		Frame df = Datasets.loadPlay();
		df = BaseFilters.retainNominal(df);
		final String className = "class";

		C45Classifier classifier = new C45Classifier().setSelection(C45Classifier.SELECTION_GAINRATIO);
		classifier.learn(df, className);
		classifier.predict(df);

		DensityTable dtWindy = new DensityTable(df, "windy", "class");
		DensityTable dtOutlook = new DensityTable(df, "outlook", "class");
		String splitCol = (dtWindy.getInfoGain() > dtOutlook.getInfoGain()) ? "windy" : "outlook";
		Assert.assertEquals(splitCol, classifier.root.testColName);

		Summary.summary(classifier);

		ConfusionMatrix cm = new ConfusionMatrix(df.getCol("class"), classifier.getPrediction());
		Summary.summary(cm);
	}

	//	@Test
	public void testNumericInfoGain() throws IOException {
		Frame df = Datasets.loadPlay();
		df = BaseFilters.retainCols(df, "temp,humidity,class");
		final String className = "class";

		C45Classifier classifier = new C45Classifier().setSelection(C45Classifier.SELECTION_INFOGAIN);
		classifier.learn(df, className);
		Summary.summary(classifier);

		classifier.predict(df);

		ConfusionMatrix cm = new ConfusionMatrix(df.getCol("class"), classifier.getPrediction());
		Summary.summary(cm);
	}

	@Test
	public void testAllInfoGain() throws IOException {
		Frame df = Datasets.loadPlay();
		final String className = "class";

		C45Classifier classifier = new C45Classifier().setSelection(C45Classifier.SELECTION_INFOGAIN);
		classifier.learn(df, className);
		Summary.summary(classifier);

		classifier.predict(df);

		ConfusionMatrix cm = new ConfusionMatrix(df.getCol("class"), classifier.getPrediction());
		Summary.summary(cm);
	}

}
