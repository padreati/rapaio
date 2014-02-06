package rapaio.ml.linear;

import rapaio.data.*;
import rapaio.data.matrix.Matrix;
import rapaio.data.matrix.QRDecomposition;
import rapaio.ml.AbstractRegressor;

import java.util.ArrayList;
import java.util.List;

/**
 * User: Aurelian Tutuianu <padreati@yahoo.com>
 */
@Deprecated
public class LinearModelRegressor extends AbstractRegressor {

	List<String> predictors = new ArrayList<>();
	List<String> targets = new ArrayList<>();
	Frame coefficients;
	Vector fittedValues;
	Vector residualValues;

	@Override
	public void learn(Frame df, List<Double> weights, String targetColNames) {
		targets.clear();
		predictors.clear();
		for (String targetColName : targetColNames.split(",", -1)) {
			targets.add(targetColName);
		}
		for (String colName : df.getColNames()) {
			if (!targetColNames.contains(colName) && df.getCol(colName).getType().isNumeric()) {
				predictors.add(colName);
			}
		}

		Matrix X = buildX(df);
		Matrix Y = buildY(df);
		Matrix beta = new QRDecomposition(X).solve(Y);
		Vector bcoeff = new Numeric();
		Vector bnames = new Nominal();
		for (int i = 0; i < predictors.size(); i++) {
			bnames.addLabel(predictors.get(i));
			bcoeff.addValue(beta.get(i, 0));
		}
		coefficients = new SolidFrame(predictors.size(), new Vector[]{bnames, bcoeff}, new String[]{"Term", "Coeff"});

		fittedValues = buildFit(df);
		residualValues = buildResidual(Y, fittedValues);
	}

	private Vector buildResidual(Matrix actual, Vector predict) {
		Vector result = Vectors.newNum(predict.getRowCount(), 0);
		for (int i = 0; i < result.getRowCount(); i++) {
			result.setValue(i, actual.get(i, 0) - predict.getValue(i));
		}
		return result;
	}

	private Vector buildFit(Frame df) {
		Vector result = Vectors.newNum(df.getRowCount(), 0);
		for (int i = 0; i < df.getRowCount(); i++) {
			double acc = 0;
			for (int k = 0; k < predictors.size(); k++) {
				acc += coefficients.getValue(k, "Coeff") * df.getValue(i, predictors.get(k));
			}
			result.setValue(i, acc);
		}
		return result;
	}

	private Matrix buildY(Frame df) {
		Numeric[] vectors = new Numeric[targets.size()];
		int pos = 0;
		for (String targetColName : targets) {
			vectors[pos++] = (Numeric) df.getCol(targetColName);
		}
		return new Matrix(vectors);
	}

	private Matrix buildX(Frame df) {
		Numeric[] vectors = new Numeric[predictors.size()];
		int pos = 0;
		for (String colName : predictors) {
			vectors[pos++] = (Numeric) df.getCol(colName);
		}
		return new Matrix(vectors);
	}

	@Override
	public void predict(Frame df) {
		fittedValues = buildFit(df);
	}

	public Frame getCoeff() {
		return coefficients;
	}

	@Override
	public Vector getFitValues() {
		return fittedValues;
	}

	@Override
	public Vector getResidualValues() {
		return residualValues;
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
