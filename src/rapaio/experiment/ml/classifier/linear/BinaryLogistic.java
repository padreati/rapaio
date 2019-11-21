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

package rapaio.experiment.ml.classifier.linear;

import rapaio.data.Frame;
import rapaio.data.VType;
import rapaio.data.Var;
import rapaio.data.VarDouble;
import rapaio.datasets.Datasets;
import rapaio.experiment.math.optimization.IRLSOptimizer;
import rapaio.math.linear.RV;
import rapaio.math.linear.dense.SolidRV;
import rapaio.ml.classifier.AbstractClassifierModel;
import rapaio.ml.classifier.ClassifierResult;
import rapaio.ml.common.Capabilities;
import rapaio.printer.Printable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> at 2/3/15.
 */
public class BinaryLogistic extends AbstractClassifierModel<BinaryLogistic, ClassifierResult<BinaryLogistic>> implements Printable {

    private static final long serialVersionUID = 1609956190070125059L;

    private VarDouble coef;

    private int maxRuns = 1_000_000;
    private double tolerance = 1e-10;

    @Override
    public BinaryLogistic newInstance() {
        return newInstanceDecoration(new BinaryLogistic())
                .withMaxRuns(maxRuns)
                .withTolerance(tolerance);
    }

    @Override
    public String name() {
        return "BinaryLogistic";
    }

    @Override
    public String fullName() {
        StringBuilder sb = new StringBuilder();
        sb.append(name()).append("{");
        sb.append("tol=").append(tolerance).append(", ");
        sb.append("maxRuns=").append(maxRuns).append(", ");
        sb.append("}");
        return sb.toString();
    }

    @Override
    public Capabilities capabilities() {
        return new Capabilities()
                .withInputTypes(VType.BINARY, VType.INT, VType.DOUBLE, VType.NOMINAL)
                .withInputCount(1, 10000)
                .withTargetTypes(VType.NOMINAL)
                .withTargetCount(1, 1)
                .withAllowMissingInputValues(false)
                .withAllowMissingTargetValues(false);
    }

    public int getMaxRuns() {
        return maxRuns;
    }

    /**
     * Maximum number of iterations if optimum was not met yet
     * (default value is 10_000)
     */
    public BinaryLogistic withMaxRuns(int maxRuns) {
        this.maxRuns = maxRuns;
        return this;
    }

    public double getTolerance() {
        return tolerance;
    }

    /**
     * Tolerance used to check the solution optimality
     * (default value 1e-10).
     */
    public BinaryLogistic withTolerance(double tolerance) {
        this.tolerance = tolerance;
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

    private final Function<Var, Double> logitF = this::logitReg;
    private final Function<Var, Double> logitFD = var -> {
        double y = logitReg(var);
        return y * (1 - y);
    };

    private RV computeTarget(Var target) {
        switch (target.type()) {
            case BINARY:
                return SolidRV.from(target);
            case NOMINAL:
                if (target.levels().size() != 3) {
                    // we allow only binary outputs
                    throw new RuntimeException("Target variable cannot be nominal with more than 2 levels.");
                }
                SolidRV result = SolidRV.empty(target.rowCount());
                for (int i = 0; i < target.rowCount(); i++) {
                    result.set(i, target.getInt(i) - 1);
                }
                return result;
            default:
                throw new IllegalArgumentException("Target variable must be nominal or binary.");
        }
    }

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

        coef = optimizer.optimize(tolerance, maxRuns, logitF, logitFD, coef, inputs, targetValues);
        return true;
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

    @Override
    protected ClassifierResult<BinaryLogistic> corePredict(Frame df, boolean withClasses, boolean withDistributions) {
        ClassifierResult<BinaryLogistic> cr = ClassifierResult.build(this, df, withClasses, withDistributions);
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

    public static void main(String[] args) {

        Frame df = Datasets.loasSAheart()
                .removeVars(0)
                .removeVars("adiposity,typea");

        df.printSummary();

        BinaryLogistic lr = new BinaryLogistic().withTolerance(1e-20);
        lr.fit(df, "chd");
        lr.predict(df).printSummary();
        lr.coef.printContent();
    }
}
