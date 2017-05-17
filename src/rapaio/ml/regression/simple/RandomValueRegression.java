/*
 * Apache License
 * Version 2.0, January 2004
 * http://www.apache.org/licenses/
 *
 *    Copyright 2013 Aurelian Tutuianu
 *    Copyright 2014 Aurelian Tutuianu
 *    Copyright 2015 Aurelian Tutuianu
 *    Copyright 2016 Aurelian Tutuianu
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

import rapaio.core.distributions.Distribution;
import rapaio.core.distributions.Uniform;
import rapaio.data.Frame;
import rapaio.data.Var;
import rapaio.data.VarType;
import rapaio.ml.common.Capabilities;
import rapaio.ml.regression.AbstractRegression;
import rapaio.ml.regression.RFit;
import rapaio.ml.regression.Regression;

/**
 * A trivial regression which predicts using random
 * values provided by a distribution.
 *
 * User: Aurelian Tutuianu <padreati@yahoo.com>
 */
public class RandomValueRegression extends AbstractRegression {

    private static final long serialVersionUID = 819192240406617594L;
    private Distribution distribution = new Uniform(0, 1);

    public static RandomValueRegression create() {
        return new RandomValueRegression();
    }

    private RandomValueRegression() {
    }

    @Override
    public Regression newInstance() {
        return new RandomValueRegression();
    }

    @Override
    public String name() {
        return "RandomValueRegression";
    }

    @Override
    public String fullName() {
        return name() + String.format("(%s)", distribution.name());
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

    public Distribution distribution() {
        return distribution;
    }

    public RandomValueRegression withDistribution(final Distribution distribution) {
        this.distribution = distribution;
        return this;
    }

    @Override
    protected boolean coreTrain(Frame df, Var weights) {
        return true;
    }

    @Override
    protected RFit coreFit(final Frame df, final boolean withResiduals) {
        RFit pred = RFit.build(this, df, withResiduals);
        for (String targetName : targetNames()) {
            pred.fit(targetName).stream().forEach(s -> s.setValue(distribution.sampleNext()));
        }
        pred.buildComplete();
        return pred;
    }

    @Override
    public String getSummary() {
        StringBuilder sb = new StringBuilder();
        sb.append(name()).append(" Summary\n");
        sb.append("=========================\n");

        sb.append("TODO: complete\n");
        return sb.toString();
    }
}
