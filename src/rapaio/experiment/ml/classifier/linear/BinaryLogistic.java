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

package rapaio.experiment.ml.classifier.linear;

import rapaio.data.Frame;
import rapaio.data.VarDouble;
import rapaio.data.Var;
import rapaio.data.VarType;
import rapaio.experiment.math.optimization.IRLSOptimizer;
import rapaio.ml.classifier.AbstractClassifier;
import rapaio.ml.classifier.CPrediction;
import rapaio.ml.common.Capabilities;
import rapaio.util.func.SFunction;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> at 2/3/15.
 */
public class BinaryLogistic extends AbstractClassifier {

    private static final long serialVersionUID = 1609956190070125059L;

    private VarDouble coef;

    private int maxRuns = 1_000_000;
    private double tol = 1e-5;

    @Override
    public BinaryLogistic newInstance() {
        return new BinaryLogistic()
                .withMaxRuns(maxRuns)
                .withTol(tol);
    }

    @Override
    public String name() {
        return "BinaryLogistic";
    }

    @Override
    public String fullName() {
        StringBuilder sb = new StringBuilder();
        sb.append(name()).append("{");
        sb.append("tol=").append(tol).append(", ");
        sb.append("maxRuns=").append(maxRuns).append(", ");
        sb.append("}");
        return sb.toString();
    }

    @Override
    public Capabilities capabilities() {
        return new Capabilities()
                .withInputTypes(VarType.BINARY, VarType.INT, VarType.DOUBLE, VarType.NOMINAL)
                .withInputCount(1, 10000)
                .withTargetTypes(VarType.NOMINAL)
                .withTargetCount(1, 1)
                .withAllowMissingInputValues(false)
                .withAllowMissingTargetValues(true);
    }

    /**
     * Maximum number of iterations if optimum was not met yet
     * (default value is 10_000)
     */
    public BinaryLogistic withMaxRuns(int maxRuns) {
        this.maxRuns = maxRuns;
        return this;
    }

    /**
     * Tolerance used to check the solution optimality
     * (default value 1e-5).
     */
    public BinaryLogistic withTol(double tol) {
        this.tol = tol;
        return this;
    }

    private double logit(double z) {
        return 1 / (1 + Math.exp(-z));
    }

    private double logitReg(Var input) {
        double z = coef.getDouble(0);
        for (int i = 1; i < coef.rowCount(); i++)
            z += input.getDouble(i - 1) * coef.getDouble(i);
        return logit(z);
    }

    private double regress(Frame df, int row) {
        if (coef == null)
            throw new IllegalArgumentException("Model has not been trained");
        VarDouble inst = VarDouble.empty();
        for (int i = 0; i < inputNames().length; i++) {
            inst.addDouble(df.getDouble(row, inputName(i)));
        }
        return logitReg(inst);
    }

    private final SFunction<Var, Double> logitF = this::logitReg;
    private final SFunction<Var, Double> logitFD = var -> {
        double y = logitReg(var);
        return y * (1 - y);
    };

    @Override
    protected boolean coreFit(Frame df, Var weights) {

        // inputs contains transposed X

        List<Var> inputs = new ArrayList<>(df.rowCount());
        for (int i = 0; i < df.rowCount(); i++) {
            VarDouble line = VarDouble.empty();
            for (String inputName : inputNames())
                line.addDouble(df.getDouble(i, inputName));
            inputs.add(line);
        }

        coef = VarDouble.fill(inputNames().length + 1, 0);

        VarDouble targetValues = VarDouble.empty();
        df.rvar(firstTargetName()).stream().forEach(s -> targetValues.addDouble(s.getInt() == 1 ? 0 : 1));
        IRLSOptimizer optimizer = new IRLSOptimizer();

        coef = optimizer.optimize(tol, maxRuns, logitF, logitFD, coef, inputs, targetValues);
        return true;
    }

    @Override
    protected CPrediction corePredict(Frame df, boolean withClasses, boolean withDistributions) {
        CPrediction cr = CPrediction.build(this, df, withClasses, withDistributions);
        for (int i = 0; i < df.rowCount(); i++) {
            double p = regress(df, i);
            if (withClasses) {
                cr.firstClasses().setInt(i, p < 0.5 ? 1 : 2);
            }
            if (withDistributions) {
                cr.firstDensity().setDouble(i, 1, 1 - p);
                cr.firstDensity().setDouble(i, 2, p);
            }
        }
        return cr;
    }
}
