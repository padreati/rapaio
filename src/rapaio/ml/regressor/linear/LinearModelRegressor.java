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

package rapaio.ml.regressor.linear;

import rapaio.core.ColRange;
import rapaio.data.*;
import rapaio.data.matrix.Matrix;
import rapaio.data.matrix.QRDecomposition;
import rapaio.ml.regressor.Regressor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * User: Aurelian Tutuianu <padreati@yahoo.com>
 */
//@Deprecated
public class LinearModelRegressor implements Regressor {

    List<String> predictors = new ArrayList<>();
    List<String> targets = new ArrayList<>();
    Frame coefficients;
    Var fittedValues;

    @Override
    public Regressor newInstance() {
        return new LinearModelRegressor();
    }

    @Override
    public void learn(Frame df, String targetCols) {
        targets = new ColRange(targetCols).parseColumnNames(df);

        predictors = Arrays.stream(df.colNames())
                .filter(c -> !targetCols.contains(c) && df.col(c).type().isNumeric())
                .collect(Collectors.toList());

        Matrix X = buildX(df);
        Matrix Y = buildY(df);
        Matrix beta = new QRDecomposition(X).solve(Y);
        Var betaC = Numeric.newEmpty();
        Var betaN = Nominal.newEmpty();
        for (int i = 0; i < predictors.size(); i++) {
            betaN.addLabel(predictors.get(i));
            betaC.addValue(beta.get(i, 0));
        }
        coefficients = new SolidFrame(predictors.size(), new Var[]{betaN, betaC}, new String[]{"Term", "Coefficients"}, null);

        fittedValues = buildFit(df);
    }

    private Var buildFit(Frame df) {
        Var result = Numeric.newFill(df.rowCount());
        for (int i = 0; i < df.rowCount(); i++) {
            double acc = 0;
            for (int k = 0; k < predictors.size(); k++) {
                acc += coefficients.value(k, "Coefficients") * df.value(i, predictors.get(k));
            }
            result.setValue(i, acc);
        }
        return result;
    }

    private Matrix buildY(Frame df) {
        return new Matrix(targets.stream()
                .map(colName -> (Numeric) df.col(colName))
                .collect(Collectors.toList())
        );
    }

    private Matrix buildX(Frame df) {
        return new Matrix(predictors.stream()
                .map(colName -> (Numeric) df.col(colName))
                .collect(Collectors.toList())
        );
    }

    @Override
    public void predict(Frame df) {
        fittedValues = buildFit(df);
    }

    public Frame getCoefficients() {
        return coefficients;
    }

    @Override
    public Var getFitValues() {
        return fittedValues;
    }

    @Override
    public Frame getAllFitValues() {
        return null;
    }
}
