/*
 * Apache License
 * Version 2.0, January 2004
 * http://www.apache.org/licenses/
 *
 *    Copyright 2013 Aurelian Tutuianu
 *    Copyright 2014 Aurelian Tutuianu
 *    Copyright 2015 Aurelian Tutuianu
 *    Copyright 2016 Aurelian Tutuianu
 *    Copyright 2017 Aurelian Tutuianu
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
 *
 */

package rapaio.ml.regression.linear;

import rapaio.data.Frame;
import rapaio.math.linear.RM;
import rapaio.math.linear.RV;
import rapaio.ml.regression.AbstractRegression;
import rapaio.printer.format.TextTable;
import rapaio.sys.WS;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 2/1/18.
 */
public abstract class AbstractLinearRegression extends AbstractRegression {

    private static final long serialVersionUID = 5740157710314998364L;

    protected RM beta;

    public RV firstCoeff() {
        return beta.mapCol(0);
    }

    public RV coefficients(int targetIndex) {
        return beta.mapCol(targetIndex);
    }

    public RM allCoefficients() {
        return beta;
    }

    @Override
    public LinearRFit fit(Frame df) {
        return (LinearRFit) super.fit(df);
    }

    @Override
    public LinearRFit fit(Frame df, boolean withResiduals) {
        return (LinearRFit) super.fit(df, withResiduals);
    }

    @Override
    protected LinearRFit coreFit(Frame df, boolean withResiduals) {
        LinearRFit rp = new LinearRFit(this, df, withResiduals);
        for (int i = 0; i < targetNames().length; i++) {
            String target = targetName(i);
            for (int j = 0; j < rp.fit(target).rowCount(); j++) {
                double fit = 0.0;
                for (int k = 0; k < inputNames().length; k++) {
                    fit += beta.get(k, i) * df.value(j, inputName(k));
                }
                rp.fit(target).setValue(j, fit);
            }
        }

        rp.buildComplete();
        return rp;
    }

    @Override
    public String summary() {
        StringBuilder sb = new StringBuilder();
        sb.append(headerSummary());
        sb.append("\n");

        if (!hasLearned) {
            return sb.toString();
        }

        for (int i = 0; i < targetNames.length; i++) {
            String targetName = targetNames[i];
            sb.append("Target <<< ").append(targetName).append(" >>>\n\n");
            sb.append("> Coefficients: \n");
            RV coeff = beta.mapCol(i);

            TextTable tt = TextTable
                    .newEmpty(coeff.count() + 1, 2)
                    .withHeaderRows(1);
            tt.set(0, 0, "Name", 0);
            tt.set(0, 1, "Estimate", 0);
            for (int j = 0; j < coeff.count(); j++) {
                tt.set(j + 1, 0, inputNames[j], -1);
                tt.set(j + 1, 1, WS.formatMedium(coeff.get(j)), 1);
            }
            sb.append(tt.summary());
            sb.append("\n");
        }
        return sb.toString();
    }
}
