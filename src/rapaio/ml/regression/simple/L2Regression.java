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
 * User: Aurelian Tutuianu <padreati@yahoo.com>
 */
public class L2Regression extends AbstractRegression implements Printable {

    private static final long serialVersionUID = -8666168876139028337L;

    public static L2Regression newL2() {
        return new L2Regression();
    }

    private double[] means;

    private L2Regression() {
    }

    @Override
    public L2Regression newInstance() {
        return newInstanceDecoration(new L2Regression());
    }

    @Override
    public String name() {
        return "L2Regression";
    }

    @Override
    public String fullName() {
        return name() + "()";
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
    public L2Regression fit(Frame df, String... targetVars) {
        return (L2Regression) super.fit(df, targetVars);
    }

    @Override
    public L2Regression fit(Frame df, Var weights, String... targetVarNames) {
        return (L2Regression) super.fit(df, weights, targetVarNames);
    }

    @Override
    protected boolean coreFit(Frame df, Var weights) {
        means = new double[targetNames().length];
        for (int i = 0; i < targetNames().length; i++) {
            double mean = Mean.of(df.rvar(targetName(i))).value();
            means[i] = mean;
        }
        return true;
    }

    @Override
    protected RegResult corePredict(final Frame df, final boolean withResiduals) {
        RegResult fit = RegResult.build(this, df, withResiduals);
        for (int i = 0; i < targetNames().length; i++) {
            double mean = means[i];
            Var v = fit.prediction(targetName(i));
            v.stream().forEach(s -> s.setDouble(mean));
        }
        fit.buildComplete();
        return fit;
    }

    public double[] getMeans() {
        return means;
    }

    @Override
    public String toString() {
        if (isFitted()) {
            StringBuilder sb = new StringBuilder();
            sb.append(fullName());
            sb.append("; fitted values={");
            for (int i = 0; i < Math.min(5, targetNames.length); i++) {
                sb.append(targetName(i)).append(":").append(Format.floatFlex(means[i]));
                if (i < targetNames.length - 1) {
                    sb.append(",");
                }
            }
            if (targetNames.length > 5) {
                sb.append("...");
            }
            sb.append("}");
            return sb.toString();
        }
        return fullName() + "; not fitted";
    }

    @Override
    public String content() {
        StringBuilder sb = new StringBuilder();
        sb.append(headerSummary());
        sb.append("\n");

        if (isFitted()) {
            sb.append("Fitted values:\n");
            sb.append("\n");

            Var target = VarNominal.empty().withName("Target");
            Var median = VarDouble.empty().withName("Fitted value");
            for (int i = 0; i < means.length; i++) {
                target.addLabel(targetName(i));
                median.addDouble(means[i]);
            }
            sb.append(SolidFrame.byVars(target, median).content());
        }
        sb.append("\n");
        return sb.toString();
    }

    @Override
    public String fullContent() {
        StringBuilder sb = new StringBuilder();
        sb.append(headerSummary());
        sb.append("\n");

        if (isFitted()) {
            sb.append("Fitted values:\n");
            sb.append("\n");

            Var target = VarNominal.empty().withName("Target");
            Var median = VarDouble.empty().withName("Fitted value");
            for (int i = 0; i < means.length; i++) {
                target.addLabel(targetName(i));
                median.addDouble(means[i]);
            }
            sb.append(SolidFrame.byVars(target, median).fullContent());
        }
        sb.append("\n");
        return sb.toString();
    }

    @Override
    public String summary() {
        return content();
    }
}
