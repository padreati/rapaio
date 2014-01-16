package rapaio.ml.regression;

import rapaio.data.*;
import rapaio.data.matrix.Matrix;
import rapaio.data.matrix.QRDecomposition;

import java.util.ArrayList;
import java.util.List;

/**
 * User: Aurelian Tutuianu <padreati@yahoo.com>
 */
public class LinearModelRegressor extends AbstractRegressor {

    List<String> predictors = new ArrayList<>();
    String targetColName;
    Frame coeff;
    Vector trainFitted;
    Vector trainResidual;
    Vector testFitted;

    @Override
    public void learn(Frame df, List<Double> weights, String targetColName) {
        predictors.clear();
        this.targetColName = targetColName;
        for (String colName : df.colNames()) {
            if (!targetColName.contains(colName) && df.col(colName).type().isNumeric()) {
                predictors.add(colName);
            }
        }

        Matrix X = buildX(df);
        Matrix Y = buildY(df);
        Matrix beta = new QRDecomposition(X).solve(Y);
        Vector bcoeff = Vectors.newNum(predictors.size(), 0);
        Vector bnames = new Nominal();
        for (int i = 0; i < predictors.size(); i++) {
            bcoeff.setValue(i, beta.get(i, 0));
            bnames.addLabel(predictors.get(i));
        }
        coeff = new SolidFrame(predictors.size(), new Vector[]{bnames, bcoeff}, new String[]{"Term", "Coeff"});

        trainFitted = buildFit(df);
        trainResidual = buildResidual(Y, trainFitted);
    }

    private Vector buildResidual(Matrix actual, Vector predict) {
        Vector result = Vectors.newNum(predict.rowCount(), 0);
        for (int i = 0; i < result.rowCount(); i++) {
            result.setValue(i, actual.get(i, 0) - predict.value(i));
        }
        return result;
    }

    private Vector buildFit(Frame df) {
        Vector result = Vectors.newNum(df.rowCount(), 0);
        for (int i = 0; i < df.rowCount(); i++) {
            double acc = 0;
            for (int k = 0; k < predictors.size(); k++) {
                acc += coeff.value(k, 1) * df.value(i, predictors.get(k));
            }
            result.setValue(i, acc);
        }
        return result;
    }

    private Matrix buildY(Frame df) {
        Numeric[] vectors = new Numeric[1];
        vectors[0] = (Numeric) df.col(targetColName);
        return new Matrix(vectors);
    }

    private Matrix buildX(Frame df) {
        Numeric[] vectors = new Numeric[predictors.size()];
        int pos = 0;
        for (String colName : predictors) {
            vectors[pos++] = (Numeric) df.col(colName);
        }
        return new Matrix(vectors);
    }

    @Override
    public void predict(Frame df) {
        testFitted = buildFit(df);
    }

    public Frame getCoeff() {
        return coeff;
    }

    public Vector getTrainFittedValues() {
        return trainFitted;
    }

    public Vector getTrainResidualValues() {
        return trainResidual;
    }

    @Override
    public Vector getTestFittedValues() {
        return testFitted;
    }
}
