package rapaio.ml.boost;

import rapaio.data.Frame;
import rapaio.data.Frames;
import rapaio.data.Numeric;
import rapaio.data.Vector;
import rapaio.data.filters.BaseFilters;
import rapaio.ml.AbstractRegressor;
import rapaio.ml.Regressor;
import rapaio.ml.boost.gbt.BTRegressor;
import rapaio.ml.boost.gbt.BoostingLossFunction;
import rapaio.ml.boost.gbt.L1BoostingLossFunction;
import rapaio.ml.tree.DecisionStumpRegressor;

import java.util.ArrayList;
import java.util.List;

/**
 * Gradient Boosting Tree
 * <p>
 * User: Aurelian Tutuianu <padreati@yahoo.com>
 */
public class GBTRegressor extends AbstractRegressor {

	int rounds = 10; // number of rounds
	BoostingLossFunction lossFunction = new L1BoostingLossFunction();
	BTRegressor regressor = new DecisionStumpRegressor();

	Regressor initialRegressor;
	List<BTRegressor> trees;
	Numeric fitLearn;

	Numeric fitValues;
	Numeric residualValues;
	Frame df;
	String targetColName;

	@Override
	public Regressor newInstance() {
		throw new RuntimeException("Not implemented");
	}

	@Override
	public void learn(Frame df, List<Double> weights, String targetColName) {
		throw new IllegalArgumentException("Does not accept weights");
	}

	@Override
	public void learn(Frame df, String targetColName) {
		this.df = df;
		this.targetColName = targetColName;

		Vector y = df.getCol(targetColName);
		Frame x = BaseFilters.removeCols(df, targetColName);
		initialRegressor = lossFunction.getInitialRegressor();
		initialRegressor.learn(df, targetColName);
		trees = new ArrayList<>();

		fitLearn = new Numeric(df.getRowCount());
		for (int i = 0; i < df.getRowCount(); i++) {
			fitLearn.setValue(i, initialRegressor.getFitValues().getValue(i));
		}

		for (int i = 1; i < rounds; i++) {
			Numeric gradient = new Numeric(df.getRowCount());
			for (int j = 0; j < df.getRowCount(); j++) {
				gradient.setValue(i, lossFunction.computeInvGradient(y.getValue(i), fitLearn.getValue(i)));
			}
			Frame xm = Frames.addCol(x, gradient, "target", x.getColCount());
			BTRegressor tree = createBTRegressor();
			tree.learn(xm, "target");
			tree.boostFit(x, y, fitLearn);
			for (int j = 0; j < df.getRowCount(); j++) {
				fitLearn.setValue(j, fitLearn.getValue(j) + tree.getFitValues().getValue(j));
			}
		}

		fitValues = new Numeric(df.getRowCount());
		residualValues = new Numeric(df.getRowCount());
		for (int i = 0; i < fitLearn.getRowCount(); i++) {
			fitValues.addValue(fitLearn.getValue(i));
			residualValues.addValue(y.getValue(i) - fitLearn.getValue(i));
		}
	}

	public void learnFurther(int additionalRounds) {
		rounds += additionalRounds;

		Vector y = df.getCol(targetColName);
		Frame x = BaseFilters.removeCols(df, targetColName);

		for (int i = 0; i < additionalRounds; i++) {
			Numeric gradient = new Numeric(df.getRowCount());
			for (int j = 0; j < df.getRowCount(); j++) {
				gradient.setValue(i, lossFunction.computeInvGradient(y.getValue(i), fitLearn.getValue(i)));
			}
			Frame xm = Frames.addCol(x, gradient, "target", x.getColCount());
			BTRegressor tree = createBTRegressor();
			tree.learn(xm, "target");
			tree.boostFit(x, y, fitLearn);
			for (int j = 0; j < df.getRowCount(); j++) {
				fitLearn.setValue(j, fitLearn.getValue(j) + tree.getFitValues().getValue(j));
			}
		}

		fitValues = new Numeric(df.getRowCount());
		residualValues = new Numeric(df.getRowCount());
		for (int i = 0; i < fitLearn.getRowCount(); i++) {
			fitValues.addValue(fitLearn.getValue(i));
			residualValues.addValue(y.getValue(i) - fitLearn.getValue(i));
		}
	}

	private BTRegressor createBTRegressor() {
		return regressor.newInstance();
	}

	@Override
	public void predict(Frame df) {
		initialRegressor.predict(df);
		fitValues = new Numeric(df.getRowCount());
		residualValues = new Numeric(df.getRowCount());
		for (int i = 0; i < df.getRowCount(); i++) {
			fitValues.setValue(i, initialRegressor.getFitValues().getValue(i));
		}
		for (int m = 0; m < trees.size(); m++) {
			Regressor tree = trees.get(m);
			tree.predict(df);
			for (int i = 0; i < df.getRowCount(); i++) {
				fitValues.setValue(i, fitValues.getValue(i) + tree.getFitValues().getValue(i));
			}
		}
		Vector y = df.getCol(targetColName);
		for (int i = 0; i < df.getRowCount(); i++) {
			residualValues.setValue(i, y.getValue(i) - fitValues.getValue(i));
		}
	}

	@Override
	public Numeric getFitValues() {
		return fitValues;
	}

	@Override
	public Numeric getResidualValues() {
		return residualValues;
	}

	@Override
	public Frame getAllFitValues() {
		throw new RuntimeException("Not implemented for multiple outputs");
	}

	@Override
	public Frame getAllResidualValues() {
		throw new RuntimeException("Not implemented for multiple outputs");
	}
}
