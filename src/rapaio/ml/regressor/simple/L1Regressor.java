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

package rapaio.ml.regressor.simple;

import rapaio.core.stat.Quantiles;
import rapaio.data.Frame;
import rapaio.data.Var;
import rapaio.ml.regressor.AbstractRegressor;
import rapaio.ml.regressor.Regressor;
import rapaio.ml.regressor.RegressorFit;

/**
 * Simple regressor which predicts with the median value of the target columns.
 * <p>
 * This simple regressor is used alone for simple prediction or as a
 * starting point for other more complex regression algorithms.
 * <p>
 * Tis regressor implements the regression by a constant paradigm using
 * sum of absolute deviations loss function: L1(y - y_hat) = \sum(|y - y_hat|).
 * <p>
 * User: Aurelian Tutuianu <padreati@yahoo.com>
 */
public class L1Regressor extends AbstractRegressor {

    private double[] medians;

    @Override
    public Regressor newInstance() {
        return new L1Regressor();
    }

    @Override
    public String name() {
        return "L1Regressor";
    }

    @Override
    public String fullName() {
        return name();
    }

    @Override
    public void learn(Frame df, Var weights, String... targetVarNames) {
        prepareLearning(df, weights, targetVarNames);
        medians = new double[targetNames().length];
        for (int i = 0; i < targetNames().length; i++) {
            String target = targetName(i);
            medians[i] = new Quantiles(df.var(target), new double[]{0.5}).values()[0];
        }
    }

    @Override
    public RegressorFit predict(final Frame df, final boolean withResiduals) {
        RegressorFit pred = RegressorFit.newEmpty(this, df, withResiduals);
        for (String targetName : targetNames()) {
            pred.addTarget(targetName);
        }
        for (int i = 0; i < targetNames().length; i++) {
            String target = targetName(i);
            double median = medians[i];
            pred.fit(target).stream().forEach(s -> s.setValue(median));
        }
        pred.buildComplete();
        return pred;
    }
}
