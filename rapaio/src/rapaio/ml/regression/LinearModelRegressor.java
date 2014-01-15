package rapaio.ml.regression;

import rapaio.data.Frame;
import rapaio.data.NumVector;
import rapaio.data.Vector;
import rapaio.data.Vectors;
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
    Vector coeff;
    Vector trainFitted;
    Vector trainResidual;
    Vector testFitted;

    @Override
    public void learn(Frame df, List<Double> weights, String targetColName) {
        predictors.clear();
        this.targetColName = targetColName;
        for (String colName : df.getColNames()) {
            if (!targetColName.contains(colName) && df.getCol(colName).getType().isNumeric()) {
                predictors.add(colName);
            }
        }

        Matrix X = buildX(df);
        Matrix Y = buildY(df);
        Matrix beta = new QRDecomposition(X).solve(Y);
        coeff = Vectors.newNum(predictors.size(), 0);
        for (int i = 0; i < predictors.size(); i++) {
            coeff.setValue(i, beta.get(i, 0));
        }

        trainFitted = buildFit(df);
        trainResidual = buildResidual(Y, trainFitted);
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
                acc += coeff.getValue(k) * df.getValue(i, predictors.get(k));
            }
            result.setValue(i, acc);
        }
        return result;
    }

    private Matrix buildY(Frame df) {
        NumVector[] vectors = new NumVector[1];
        vectors[0] = (NumVector) df.getCol(targetColName);
        return new Matrix(vectors);
    }

    private Matrix buildX(Frame df) {
        NumVector[] vectors = new NumVector[predictors.size()];
        int pos = 0;
        for (String colName : predictors) {
            vectors[pos++] = (NumVector) df.getCol(colName);
        }
        return new Matrix(vectors);
    }

    @Override
    public void predict(Frame df) {
        testFitted = buildFit(df);
    }

    public Vector getCoeff() {
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
