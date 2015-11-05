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
 *
 */

package rapaio.ml.regressor.simple;

import rapaio.core.stat.Mean;
import rapaio.data.Frame;
import rapaio.data.Var;
import rapaio.ml.regressor.AbstractRegression;
import rapaio.ml.regressor.RFit;
import rapaio.ml.regressor.Regression;

/**
 * User: Aurelian Tutuianu <padreati@yahoo.com>
 */
@Deprecated
public class L2Regression extends AbstractRegression {

    private double[] means;

    @Override
    public Regression newInstance() {
        return new L2Regression();
    }

    @Override
    public String name() {
        return "L2Regression";
    }

    @Override
    public String fullName() {
        return name();
    }

    @Override
    public void learn(Frame df, Var weights, String... targetVarNames) {
        prepareTraining(df, weights, targetVarNames);
        means = new double[targetNames().length];
        for (int i = 0; i < targetNames().length; i++) {
            double mean = new Mean(df.var(targetName(i))).value();
            means[i] = mean;
        }
    }

    @Override
    public RFit fit(final Frame df, final boolean withResiduals) {
        RFit pred = RFit.newEmpty(this, df, withResiduals);
        for (String targetName : targetNames()) {
            pred.addTarget(targetName);
        }
        for (int i = 0; i < targetNames().length; i++) {
            double mean = means[i];
            pred.fit(targetName(i)).stream().forEach(s -> s.setValue(mean));
        }
        pred.buildComplete();
        return pred;
    }

    @Override
    public String summary() {
        throw new IllegalArgumentException("not implemented");
    }
}
