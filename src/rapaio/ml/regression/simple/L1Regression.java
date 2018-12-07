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
 *    Copyright 2018 Aurelian Tutuianu
 *    Copyright 2019 Aurelian Tutuianu
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

package rapaio.ml.regression.simple;

import rapaio.core.stat.*;
import rapaio.data.*;
import rapaio.ml.common.*;
import rapaio.ml.regression.*;
import rapaio.printer.*;
import rapaio.printer.format.*;

/**
 * Simple regression which predicts with the median value of the target columns.
 * <p>
 * This simple regression is used alone for simple prediction or as a
 * starting point for other more complex regression algorithms.
 * <p>
 * This regression implements the regression by a constant paradigm using
 * sum of absolute deviations loss function: L1(y - y_hat) = \sum(|y - y_hat|).
 * <p>
 * User: Aurelian Tutuianu <padreati@yahoo.com>
 */
public class L1Regression extends AbstractRegression implements DefaultPrintable {

    private static final long serialVersionUID = 6125284399953219419L;

    public static L1Regression create() {
        return new L1Regression();
    }

    private double[] medians;

    private L1Regression() {
    }

    @Override
    public L1Regression newInstance() {
        return new L1Regression();
    }

    @Override
    public String name() {
        return "L1Regression";
    }

    @Override
    public String fullName() {
        return name();
    }

    @Override
    public Capabilities capabilities() {
        return new Capabilities()
                .withInputCount(0, 1_000_000)
                .withTargetCount(1, 1_000_000)
                .withInputTypes(VType.DOUBLE, VType.BINARY, VType.INT, VType.NOMINAL, VType.LONG, VType.TEXT)
                .withTargetTypes(VType.DOUBLE)
                .withAllowMissingInputValues(true)
                .withAllowMissingTargetValues(true);
    }

    @Override
    protected boolean coreFit(Frame df, Var weights) {
        medians = new double[targetNames().length];
        for (int i = 0; i < targetNames().length; i++) {
            String target = targetName(i);
            medians[i] = Quantiles.of(df.rvar(target), 0.5).values()[0];
        }
        return true;
    }

    @Override
    public RPrediction corePredict(final Frame df, final boolean withResiduals) {
        RPrediction pred = RPrediction.build(this, df, withResiduals);
        for (int i = 0; i < targetNames().length; i++) {
            String target = targetName(i);
            double median = medians[i];
            pred.fit(target).stream().forEach(s -> s.setDouble(median));
        }
        pred.buildComplete();
        return pred;
    }

    @Override
    public String summary() {
        StringBuilder sb = new StringBuilder();
        sb.append(headerSummary());
        sb.append("\n");

        if (isFitted()) {
            sb.append("Fitted values:\n");
            sb.append("\n");

            TextTable tt = TextTable.empty(1 + targetNames.length, 2);
            tt.textLeft(0, 0, "Target");
            tt.textRight(0, 1, "Estimate");

            for (int i = 0; i < targetNames().length; i++) {
                tt.textRight(1 + i, 0, targetName(i));
                tt.floatFlex(1 + i, 1, medians[i]);
            }
            sb.append(tt.getDefaultText());
        }
        sb.append("\n");
        return sb.toString();
    }
}
