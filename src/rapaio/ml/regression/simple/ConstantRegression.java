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

package rapaio.ml.regression.simple;

import rapaio.data.Frame;
import rapaio.data.Var;
import rapaio.data.VarType;
import rapaio.ml.common.Capabilities;
import rapaio.ml.regression.AbstractRegression;
import rapaio.ml.regression.RFit;
import rapaio.ml.regression.Regression;

import static rapaio.sys.WS.formatFlex;

/**
 * User: Aurelian Tutuianu <padreati@yahoo.com>
 */
@Deprecated
public class ConstantRegression extends AbstractRegression {

    private static final long serialVersionUID = -2537862585258148528L;

    double constant = Double.NaN;

    @Override
    public Regression newInstance() {
        return new ConstantRegression()
                .withConstant(constant);
    }

    @Override
    public String name() {
        return "ConstantRegression";
    }

    @Override
    public String fullName() {
        return String.format("ConstantRegression {\n" +
                "   constant=%s\n" +
                "}\n", formatFlex(constant));
    }

    @Override
    public Capabilities capabilities() {
        return new Capabilities()
                .withInputCount(0, 1_000_000)
                .withTargetCount(1, 1)
                .withInputTypes(VarType.NUMERIC, VarType.ORDINAL, VarType.BINARY, VarType.INDEX, VarType.NOMINAL, VarType.STAMP, VarType.TEXT)
                .withTargetTypes(VarType.NUMERIC)
                .withAllowMissingInputValues(true)
                .withAllowMissingTargetValues(true);
    }

    public double constantValue() {
        return constant;
    }

    public ConstantRegression withConstant(double customValue) {
        this.constant = customValue;
        return this;
    }

    @Override
    protected boolean coreTrain(Frame df, Var weights) {
        return true;
    }

    @Override
    protected RFit coreFit(final Frame df, final boolean withResiduals) {
        RFit fit = RFit.build(this, df, withResiduals);
        fit.buildComplete();
        return fit;
    }

    @Override
    public String summary() {
        return fullName();
    }
}
