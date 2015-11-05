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

import rapaio.data.Frame;
import rapaio.data.Var;
import rapaio.ml.regressor.AbstractRegression;
import rapaio.ml.regressor.Regression;
import rapaio.ml.regressor.RegressionFit;

import static rapaio.sys.WS.formatFlex;

/**
 * User: Aurelian Tutuianu <padreati@yahoo.com>
 */
@Deprecated
public class ConstantRegression extends AbstractRegression {

    double constantValue;

    @Override
    public Regression newInstance() {
        return new ConstantRegression();
    }

    @Override
    public String name() {
        return "ConstantRegression";
    }

    @Override
    public String fullName() {
        return String.format("ConstantRegression(constant=%s)", formatFlex(constantValue));
    }

    public double constantValue() {
        return constantValue;
    }

    public ConstantRegression withConstantValue(double customValue) {
        this.constantValue = customValue;
        return this;
    }

    @Override
    public void learn(Frame df, Var weights, String... targetVarNames) {
        prepareTraining(df, weights, targetVarNames);
    }

    @Override
    public RegressionFit fit(final Frame df, final boolean withResiduals) {
        RegressionFit pred = RegressionFit.newEmpty(this, df, withResiduals);
        for (String targetName : targetNames()) {
            pred.addTarget(targetName);
        }
        pred.buildComplete();
        return pred;
    }
}
