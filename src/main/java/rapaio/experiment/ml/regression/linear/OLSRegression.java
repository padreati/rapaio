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

package rapaio.experiment.ml.regression.linear;

import rapaio.data.Frame;
import rapaio.data.Var;
import rapaio.data.VarType;
import rapaio.math.linear.RV;
import rapaio.math.linear.dense.QR;
import rapaio.math.linear.RM;
import rapaio.math.linear.dense.SolidRM;
import rapaio.ml.common.Capabilities;
import rapaio.ml.regression.AbstractRegression;
import rapaio.ml.regression.Regression;

/**
 * User: Aurelian Tutuianu <padreati@yahoo.com>
 */
@Deprecated
public class OLSRegression extends AbstractRegression {

    private static final long serialVersionUID = 8610329390138787530L;

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
    public Capabilities capabilities() {
        return new Capabilities()
                .withInputTypes(VarType.NUMERIC, VarType.INDEX, VarType.BINARY, VarType.ORDINAL)
                .withTargetTypes(VarType.NUMERIC)
                .withInputCount(1, 1_000_000)
                .withTargetCount(1, 1_000_000)
                .withAllowMissingInputValues(false)
                .withAllowMissingTargetValues(false);
    }

    public RV firstCoeff() {
        return beta.mapCol(0);
    }

    @Override
    protected boolean coreTrain(Frame df, Var weights) {
        if (targetNames().length == 0) {
            throw new IllegalArgumentException("OLS must specify at least one target variable name");
        }
        RM X = SolidRM.copyOf(df.mapVars(inputNames()));
        RM Y = SolidRM.copyOf(df.mapVars(targetNames()));
        beta = new QR(X).solve(Y);
        return true;
    }

    @Override
    public OLSRFit fit(Frame df) {
        return (OLSRFit) super.fit(df);
    }

    @Override
    public OLSRFit fit(Frame df, boolean withResiduals) {
        return (OLSRFit) super.fit(df, withResiduals);
    }

    @Override
    protected OLSRFit coreFit(Frame df, boolean withResiduals) {
        OLSRFit rp = new OLSRFit(this, df);

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
        throw new IllegalArgumentException("not implemented");
    }
}
