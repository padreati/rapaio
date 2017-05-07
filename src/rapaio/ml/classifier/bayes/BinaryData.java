package rapaio.ml.classifier.bayes;

import java.util.Map;
import java.util.Set;

import rapaio.data.Frame;
import rapaio.ml.classifier.bayes.estimator.BinaryEstimator;

public class BinaryData extends NaiveBayesData{
	public BinaryEstimator binEstimator;
	public Map<String, BinaryEstimator> binMap;

	public BinaryData(BinaryEstimator binEstimator) {
		this.binEstimator = binEstimator;
	}
	
	public Set<String> keySet() {
		return binMap.keySet();
	}

	@Override
	public double calcSumLog(String testCol, Frame df, int i, String firstTargetLevel) {
		return binMap.get(testCol).cpValue(df.label(i, testCol), firstTargetLevel);
	}
}