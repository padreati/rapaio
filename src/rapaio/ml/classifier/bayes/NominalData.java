package rapaio.ml.classifier.bayes;

import java.io.Serializable;
import java.util.Map;
import java.util.Set;

import rapaio.data.Frame;
import rapaio.ml.classifier.bayes.estimator.NominalEstimator;

public class NominalData extends NaiveBayesData implements Serializable{
	public NominalEstimator nomEstimator;
	public Map<String, NominalEstimator> nomMap;

	public NominalData(NominalEstimator nomEstimator) {
		this.nomEstimator = nomEstimator;
	}
	
	public Set<String> keySet() {
		return nomMap.keySet();
	}

	@Override
	public double calcSumLog(String testCol, Frame df, int i, String firstTargetLevel) {
		return nomMap.get(testCol).cpValue(df.label(i, testCol), firstTargetLevel);
	}
}