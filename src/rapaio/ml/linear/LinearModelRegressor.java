/*
 * Apache License
 * Version 2.0, January 2004
 * http://www.apache.org/licenses/
 *
 *    Copyright 2013 Aurelian Tutuianu
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package rapaio.ml.linear;

import rapaio.data.*;
import rapaio.data.matrix.Matrix;
import rapaio.data.matrix.QRDecomposition;
import rapaio.ml.AbstractRegressor;
import rapaio.ml.Regressor;

import java.util.ArrayList;
import java.util.List;

/**
 * User: Aurelian Tutuianu <padreati@yahoo.com>
 */
//@Deprecated
public class LinearModelRegressor extends AbstractRegressor {

    List<String> predictors = new ArrayList<>();
    List<String> targets = new ArrayList<>();
    Frame coefficients;
    Vector fittedValues;

    @Override
    public Regressor newInstance() {
        return new LinearModelRegressor();
    }

    @Override
    public void learn(Frame df, List<Double> weights, String targetColNames) {
        targets.clear();
        predictors.clear();
        for (String targetColName : targetColNames.split(",", -1)) {
            targets.add(targetColName);
        }
        for (String colName : df.colNames()) {
            if (!targetColNames.contains(colName) && df.col(colName).type().isNumeric()) {
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
    }

    private Vector buildFit(Frame df) {
        Vector result = Vectors.newNum(df.rowCount(), 0);
        for (int i = 0; i < df.rowCount(); i++) {
            double acc = 0;
            for (int k = 0; k < predictors.size(); k++) {
                acc += coefficients.value(k, "Coeff") * df.value(i, predictors.get(k));
            }
            result.setValue(i, acc);
        }
        return result;
    }

    private Matrix buildY(Frame df) {
        Numeric[] vectors = new Numeric[targets.size()];
        int pos = 0;
        for (String targetColName : targets) {
            vectors[pos++] = (Numeric) df.col(targetColName);
        }
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
    public Frame getAllFitValues() {
        return null;
    }
}
