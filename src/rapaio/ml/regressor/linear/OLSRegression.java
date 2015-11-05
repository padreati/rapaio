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

package rapaio.ml.regressor.linear;

import rapaio.data.Frame;
import rapaio.data.Var;
import rapaio.math.linear.Linear;
import rapaio.math.linear.QR;
import rapaio.math.linear.RM;
import rapaio.ml.regressor.AbstractRegression;
import rapaio.ml.regressor.Regression;

/**
 * User: Aurelian Tutuianu <padreati@yahoo.com>
 */
@Deprecated
public class OLSRegression extends AbstractRegression {

    RM beta;

    @Override
    public Regression newInstance() {
        return new OLSRegression();
    }

    @Override
    public String name() {
        return "OLSRegression";
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
        prepareTraining(df, weights, targetVarNames);
        if (targetNames().length == 0) {
            throw new IllegalArgumentException("OLS must specify at least one target variable name");
        }
        RM X = Linear.newRMCopyOf(df.mapVars(inputNames()));
        RM Y = Linear.newRMCopyOf(df.mapVars(targetNames()));
        beta = new QR(X).solve(Y);
    }

    @Override
    public OLSRFit fit(Frame df) {
        return fit(df, true);
    }

    @Override
    public OLSRFit fit(Frame df, boolean withResiduals) {
        OLSRFit rp = new OLSRFit(this, df);
        rp.buildComplete();
        return rp;
    }

    @Override
    public String summary() {
        throw new IllegalArgumentException("not implemented");
    }
}
