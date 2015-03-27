/*
 * Apache License
 * Version 2.0, January 2004
 * http://www.apache.org/licenses/
 *
 *    Copyright 2013 Aurelian Tutuianu
 *    Copyright 2014 Aurelian Tutuianu
 *    Copyright 2015 Aurelian Tutuianu
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
import rapaio.math.linear.LinAlg;
import rapaio.math.linear.QRDecomposition;
import rapaio.math.linear.RMatrix;
import rapaio.ml.regressor.AbstractRegressor;
import rapaio.ml.regressor.Regressor;

import java.util.Arrays;

/**
 * User: Aurelian Tutuianu <padreati@yahoo.com>
 */
public class OLSRegressor extends AbstractRegressor {

    Frame coefficients;

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

        prepareLearning(df, weights, targetVarNames);
        if (targetNames().length == 0) {
            throw new IllegalArgumentException("OLS must specify at least one target variable name");
        }

        RMatrix X = LinAlg.newMatrixCopyOf(df.mapVars(inputNames()));
        RMatrix Y = LinAlg.newMatrixCopyOf(df.mapVars(targetNames()));
        RMatrix beta = new QRDecomposition(X).solve(Y);
        Var betaN = Nominal.newEmpty().withName("Term");
        Var betaC = Numeric.newEmpty().withName("Coefficient");
        for (int i = 0; i < inputNames().length; i++) {
            betaN.addLabel(inputName(i));
            betaC.addValue(beta.get(i, 0));
        }
        coefficients = SolidFrame.newWrapOf(inputNames().length, betaN, betaC);
    }

    @Override
    public OLSRegressorFit predict(Frame df) {
        return predict(df, true);
    }

    @Override
    public OLSRegressorFit predict(Frame df, boolean withResiduals) {
        OLSRegressorFit rp = OLSRegressorFit.newEmpty(this, df);
        Arrays.stream(targetNames()).forEach(rp::addTarget);
        for (int i = 0; i < df.rowCount(); i++) {
            double acc = 0;
            for (int k = 0; k < inputNames().length; k++) {
                double c = coefficients.value(k, "Coefficient");
                double v = df.value(i, inputName(k));
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
