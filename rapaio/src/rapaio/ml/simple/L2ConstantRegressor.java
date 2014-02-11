package rapaio.ml.simple;

import rapaio.core.ColRange;
import rapaio.core.stat.Mean;
import rapaio.data.Frame;
import rapaio.data.Numeric;
import rapaio.data.SolidFrame;
import rapaio.data.Vector;
import rapaio.ml.AbstractRegressor;
import rapaio.ml.Regressor;

import java.util.ArrayList;
import java.util.List;

/**
 * User: Aurelian Tutuianu <padreati@yahoo.com>
 */
public class L2ConstantRegressor extends AbstractRegressor {

	private List<String> targets;
	private List<Double> means;
	private List<Vector> fitValues;

	@Override
	public Regressor newInstance() {
		return new L2ConstantRegressor();
	}

	@Override
	public void learn(Frame df, List<Double> weights, String targetColName) {
		throw new RuntimeException("Not implemented");
	}

	@Override
	public void learn(Frame df, String targetColNames) {
		ColRange colRange = new ColRange(targetColNames);
		List<Integer> colIndexes = colRange.parseColumnIndexes(df);

		targets = new ArrayList<>();
		for (int i = 0; i < colIndexes.size(); i++) {
			targets.add(df.getColNames()[colIndexes.get(i)]);
		}

		means = new ArrayList<>();
		fitValues = new ArrayList<>();
		for (String target : targets) {
			double mean = new Mean(df.getCol(target)).getValue();
			means.add(mean);
			fitValues.add(new Numeric(df.getCol(target).getRowCount(), df.getCol(target).getRowCount(), mean));
		}
	}

	@Override
	public void predict(Frame df) {
		fitValues = new ArrayList<>();
		for (int i = 0; i < targets.size(); i++) {
			fitValues.add(new Numeric(
					df.getRowCount(),
					df.getRowCount(),
					means.get(i)));
		}
	}

	@Override
	public Numeric getFitValues() {
		return (Numeric) fitValues.get(0);
	}

	@Override
	public Frame getAllFitValues() {
		return new SolidFrame(fitValues.get(0).getRowCount(), fitValues, targets);
	}
}
