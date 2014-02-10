package rapaio.ml.simple;

import rapaio.core.ColRange;
import rapaio.core.stat.Quantiles;
import rapaio.data.Frame;
import rapaio.data.Numeric;
import rapaio.data.SolidFrame;
import rapaio.data.Vector;
import rapaio.ml.AbstractRegressor;
import rapaio.ml.Regressor;

import java.util.ArrayList;
import java.util.List;

/**
 * Simple regressor which predicts with the median value of the target columns.
 * <p>
 * This simple regressor is used alone for simple prediction or as a
 * starting point for other more complex regressors.
 * <p>
 * Tis regressor implements the regression by a constant paradigm using
 * sum of absolute deviations loss function: L1(y - y_hat) = \sum(|y - y_hat|).
 * <p>
 * User: Aurelian Tutuianu <padreati@yahoo.com>
 */
public class L1ConstantRegressor extends AbstractRegressor {

	private List<String> targets;
	private List<Double> medians;
	private List<Vector> fitValues;
	private List<Vector> residualValues;

	@Override
	public Regressor newInstance() {
		return new L1ConstantRegressor();
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

		medians = new ArrayList<>();
		fitValues = new ArrayList<>();
		residualValues = new ArrayList<>();
		for (String target : targets) {
			double median = new Quantiles(df.getCol(target), new double[]{0.5}).getValues()[0];
			medians.add(median);
			fitValues.add(new Numeric(df.getCol(target).getRowCount(), df.getCol(target).getRowCount(), median));
			Numeric residual = new Numeric(df.getCol(target).getRowCount());
			for (int j = 0; j < df.getRowCount(); j++) {
				residual.setValue(j, df.getCol(target).getValue(j) - median);
			}
			residualValues.add(residual);
		}
	}

	@Override
	public void predict(Frame df) {
		fitValues = new ArrayList<>();
		residualValues = new ArrayList<>();
		for (int i = 0; i < targets.size(); i++) {
			fitValues.add(new Numeric(
					df.getCol(targets.get(i)).getRowCount(),
					df.getCol(targets.get(i)).getRowCount(),
					medians.get(i)));
			Numeric residual = new Numeric(df.getCol(targets.get(i)).getRowCount());
			for (int j = 0; j < df.getRowCount(); j++) {
				residual.setValue(j, df.getCol(targets.get(i)).getValue(j) - medians.get(i));
			}
			residualValues.add(residual);
		}
	}

	@Override
	public Numeric getFitValues() {
		return (Numeric) fitValues.get(0);
	}

	@Override
	public Numeric getResidualValues() {
		return (Numeric) residualValues.get(0);
	}

	@Override
	public Frame getAllFitValues() {
		return new SolidFrame(fitValues.get(0).getRowCount(), fitValues, targets);
	}

	@Override
	public Frame getAllResidualValues() {
		return new SolidFrame(residualValues.get(0).getRowCount(), residualValues, targets);
	}
}
