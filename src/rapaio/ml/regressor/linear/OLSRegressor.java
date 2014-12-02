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

import rapaio.data.*;
import rapaio.data.matrix.Matrix;
import rapaio.data.matrix.QRDecomposition;
import rapaio.ml.regressor.AbstractRegressor;
import rapaio.ml.regressor.Regressor;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * User: Aurelian Tutuianu <padreati@yahoo.com>
 */
public class OLSRegressor extends AbstractRegressor {

    List<String> predictors;
    Frame coefficients;
    Var fittedValues;

    @Override
    public Regressor newInstance() {
        return new OLSRegressor();
    }

    @Override
    public String name() {
        return "OLSRegressor";
    }

    @Override
    public String fullName() {
        StringBuilder sb = new StringBuilder();
        sb.append(name()).append("{");
        sb.append("TODO");
        sb.append("}");
        return sb.toString();
    }

    @Override
    public void learn(Frame df, Var weights, String... targetVarNames) {

        List<String> list = new VarRange(targetVarNames).parseVarNames(df);
        if (list.isEmpty()) {
            throw new IllegalArgumentException("OLS must specify at least one target variable name");
        }
        targetNames = list.toArray(new String[list.size()]);
        predictors = Arrays.stream(df.varNames())
                .filter(c -> !list.contains(c) && df.var(c).type().isNumeric())
                .collect(Collectors.toList());

        Matrix X = buildX(df);
        Matrix Y = buildY(df);
        Matrix beta = new QRDecomposition(X).solve(Y);
        Var betaN = Nominal.newEmpty().withName("Term");
        Var betaC = Numeric.newEmpty().withName("Coefficient");
        for (int i = 0; i < predictors.size(); i++) {
            betaN.addLabel(predictors.get(i));
            betaC.addValue(beta.get(i, 0));
        }
        coefficients = SolidFrame.newWrapOf(predictors.size(), betaN, betaC);
    }

    private Matrix buildY(Frame df) {
        return new Matrix(Arrays.stream(targetNames)
                .map(colName -> (Numeric) df.var(colName))
                .collect(Collectors.toList())
        );
    }

    private Matrix buildX(Frame df) {
        return new Matrix(predictors.stream()
                .map(colName -> (Numeric) df.var(colName))
                .collect(Collectors.toList())
        );
    }

    @Override
    public RPredictionOLS predict(Frame df) {
        RPredictionOLS rp = RPredictionOLS.newEmpty(df, targetNames);
        for (int i = 0; i < df.rowCount(); i++) {
            double acc = 0;
            for (int k = 0; k < predictors.size(); k++) {
                double c = coefficients.value(k, "Coefficient");
                double v = df.value(i, predictors.get(k));
                acc += c * v;
            }
            rp.firstFit().setValue(i, acc);
        }
        rp.buildComplete();
        return rp;
    }

    public Frame getCoefficients() {
        return coefficients;
    }
}
