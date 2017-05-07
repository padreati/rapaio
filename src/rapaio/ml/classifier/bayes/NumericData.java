package rapaio.ml.classifier.bayes;

import java.util.Map;
import java.util.Set;

import rapaio.data.Frame;
import rapaio.ml.classifier.bayes.estimator.NumericEstimator;

public class NumericData extends NaiveBayesData{
	public NumericEstimator numEstimator;
	public Map<String, NumericEstimator> numMap;

	public NumericData(NumericEstimator numEstimator) {
		this.numEstimator = numEstimator;
	}
	
	public Set<String> keySet() {
		return numMap.keySet();
	}

	@Override
	public double calcSumLog(String testCol, Frame df, int i, String firstTargetLevel) {
		return numMap.get(testCol).cpValue(df.value(i, testCol), firstTargetLevel);
	}
}