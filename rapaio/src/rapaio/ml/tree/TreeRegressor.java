package rapaio.ml.tree;

import rapaio.core.ColRange;
import rapaio.core.stat.Mean;
import rapaio.core.stat.StatOnline;
import rapaio.data.*;
import rapaio.data.filters.BaseFilters;
import rapaio.data.mapping.MappedFrame;
import rapaio.data.mapping.Mapping;
import rapaio.ml.AbstractRegressor;
import rapaio.ml.Regressor;
import rapaio.ml.colselect.ColSelector;
import rapaio.ml.colselect.DefaultColSelector;

import java.util.ArrayList;
import java.util.List;

/**
 * This works for numeric attributes only with no missing values.
 * With this restriction it works like CART or C45Classifier regression trees.
 * <p>
 * User: Aurelian Tutuianu <paderati@yahoo.com>
 */
@Deprecated
public class TreeRegressor extends AbstractRegressor {

	double minWeight = 1;
	TreeRegressorNode root;
	Vector fitted;
	ColSelector colSelector;
	String targetColNames;

	public double getMinWeight() {
		return minWeight;
	}

	public TreeRegressor setMinWeight(double minWeight) {
		this.minWeight = minWeight;
		return this;
	}

	public ColSelector getColSelector() {
		return colSelector;
	}

	public TreeRegressor setColSelector(ColSelector colSelector) {
		this.colSelector = colSelector;
		return this;
	}

	@Override
	public Regressor newInstance() {
		throw new RuntimeException("Not implemented");
	}

	@Override
	public void learn(Frame df, List<Double> weights, String targetColName) {
		this.targetColNames = targetColName;
		root = new TreeRegressorNode();
		root.learn(this, df, weights, targetColName);
		if (colSelector == null) {
			colSelector = new DefaultColSelector(df, new ColRange(targetColName));
		}
	}

	@Override
	public void predict(Frame df) {
		fitted = new Numeric(new double[df.getRowCount()]);
		for (int i = 0; i < df.getRowCount(); i++) {
			fitted.setValue(i, root.predict(df, i));
		}
	}

	@Override
	public Numeric getFitValues() {
		return (Numeric) fitted;
	}

	@Override
	public Numeric getResidualValues() {
		throw new RuntimeException("Not implemented");
	}

	@Override
	public Frame getAllFitValues() {
		return null;
	}

	@Override
	public Frame getAllResidualValues() {
		return null;
	}
}

class TreeRegressorNode {
	boolean leaf;
	double pred;
	double eval = Double.MAX_VALUE;
	String splitColName;
	double splitValue;
	double totalWeight;
	TreeRegressorNode left;
	TreeRegressorNode right;

	public void learn(TreeRegressor parent, Frame df, List<Double> weights, String targetColNames) {
		totalWeight = 0;
		for (int i = 0; i < weights.size(); i++) {
			totalWeight += weights.get(i);
		}

		if (totalWeight < 2 * parent.minWeight) {
			leaf = true;
			pred = new Mean(df.getCol(targetColNames)).getValue();
			return;
		}

		String[] colNames = parent.colSelector.nextColNames();
		for (String testColName : colNames) {
			if (df.getCol(testColName).getType().isNumeric() && !targetColNames.equals(testColName)) {
				evaluateNumeric(parent, df, weights, targetColNames, testColName);
			}
		}

		// if we have a split
		if (splitColName != null) {
			Mapping leftMapping = new Mapping();
			Mapping rightMapping = new Mapping();
			List<Double> leftWeights = new ArrayList<>();
			List<Double> rightWeights = new ArrayList<>();

			for (int i = 0; i < df.getRowCount(); i++) {
				if (df.getValue(i, splitColName) <= splitValue) {
					leftMapping.add(df.getRowId(i));
					leftWeights.add(weights.get(i));
				} else {
					rightMapping.add(df.getRowId(i));
					rightWeights.add(weights.get(i));
				}
			}
			left = new TreeRegressorNode();
			right = new TreeRegressorNode();
			left.learn(parent, new MappedFrame(df.getSourceFrame(), leftMapping), leftWeights, targetColNames);
			right.learn(parent, new MappedFrame(df.getSourceFrame(), rightMapping), rightWeights, targetColNames);
			return;
		}

		// else do the default
		leaf = true;
		pred = new Mean(df.getCol(targetColNames)).getValue();
	}

	private void evaluateNumeric(TreeRegressor parent,
								 Frame df, List<Double> weights,
								 String targetColName,
								 String testColNames) {

		Vector testCol = df.getCol(testColNames);
		double[] var = new double[df.getRowCount()];
		StatOnline so = new StatOnline();
		Vector sort = Vectors.newSeq(df.getRowCount());
		sort = BaseFilters.sort(sort, RowComparators.numericComparator(testCol, true));
		double w = 0;
		for (int i = 0; i < df.getRowCount(); i++) {
			int pos = sort.getRowId(i);
			so.update(testCol.getValue(pos));
			w += weights.get(pos);
			if (i > 0) {
				var[i] = so.getStandardDeviation() * w / totalWeight;
			}
		}
		so.clean();
		w = 0;
		for (int i = df.getRowCount() - 1; i >= 0; i--) {
			int pos = sort.getRowId(i);
			so.update(testCol.getValue(pos));
			w += weights.get(pos);
			if (i < df.getRowCount() - 1) {
				var[i] += so.getStandardDeviation() * w / totalWeight;
			}
		}
		w = 0;
		for (int i = 0; i < df.getRowCount(); i++) {
			int pos = sort.getRowId(i);
			w += weights.get(pos);

			if (w >= parent.minWeight && totalWeight - w >= parent.minWeight) {
				if (var[i] < eval && i > 0 && testCol.getValue(sort.getRowId(i - 1)) != testCol.getValue(sort.getRowId(i))) {
					eval = var[i];
					splitColName = testColNames;
					splitValue = testCol.getValue(pos);
				}
			}
		}
	}

	public double predict(Frame df, int row) {
		if (leaf) {
			return pred;
		}
		if (df.getValue(row, splitColName) <= splitValue) {
			return left.predict(df, row);
		} else {
			return right.predict(df, row);
		}
	}
}