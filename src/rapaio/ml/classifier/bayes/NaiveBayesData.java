package rapaio.ml.classifier.bayes;

import java.util.Set;

import rapaio.data.Frame;

public abstract class NaiveBayesData {

	public abstract Set<String> keySet();
	public abstract double calcSumLog(String testCol, Frame df, int i, String firstTargetLevel);
	
}