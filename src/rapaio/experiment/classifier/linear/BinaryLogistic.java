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

package rapaio.experiment.classifier.linear;

import rapaio.data.Frame;
import rapaio.data.Numeric;
import rapaio.data.Var;
import rapaio.math.optimization.IRLSOptimizer;
import rapaio.ml.classifier.AbstractClassifier;
import rapaio.ml.classifier.CFit;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> at 2/3/15.
 */
@Deprecated
public class BinaryLogistic extends AbstractClassifier {

    private static final long serialVersionUID = 1609956190070125059L;
    private Numeric coef;
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

    private static double logit(double z) {
        return 1 / (1 + Math.exp(-z));
    }

    private double logitReg(Var input) {
        double z = coef.value(0);
        for (int i = 1; i < coef.rowCount(); i++)
            z += input.value(i - 1) * coef.value(i);
        return logit(z);
    }

    private final Function<Var, Double> logitF = this::logitReg;

    private final Function<Var, Double> logitFD = var -> {
        double y = logitReg(var);
        return y * (1 - y);
    };


    public double regress(Frame df, int row) {
        if (coef == null)
            throw new IllegalArgumentException("Model has not been trained");
        Numeric inst = Numeric.newEmpty();
        for (int i = 0; i < inputNames().length; i++) {
            inst.addValue(df.value(row, inputNames(i)));
        }
        return logitReg(inst);
    }

    @Override
    public BinaryLogistic learn(Frame df, Var weights, String... targetVarNames) {
        prepareLearning(df, weights, targetVarNames);

        if (df.spotStream().complete().count() != df.rowCount()) {
            throw new IllegalArgumentException("Incomplete data set is not allowed in binary logistic");
        }

        List<Var> inputs = new ArrayList<>(df.rowCount());
        for (int i = 0; i < df.rowCount(); i++) {
            Numeric line = Numeric.newEmpty();
            for (String inputName : inputNames())
                line.addValue(df.value(i, inputName));
            inputs.add(line);
        }

        coef = Numeric.newFill(inputNames().length + 1, 0);
        Numeric targetValues = Numeric.newEmpty();
        df.var(firstTargetName()).spotStream().forEach(s -> targetValues.addValue(s.index() == 1 ? 0 : 1));
        IRLSOptimizer optimizer = new IRLSOptimizer();

        coef = optimizer.optimize(tol, maxRuns, logitF, logitFD, coef, inputs, targetValues);
        return this;
    }

    @Override
    public CFit fit(Frame df, boolean withClasses, boolean withDistributions) {
        if (coef == null)
            throw new IllegalArgumentException("Model has not yet been trained");

        CFit cr = CFit.newEmpty(this, df, withClasses, withDistributions);
        cr.addTarget(firstTargetName(), firstDict());

        for (int i = 0; i < df.rowCount(); i++) {
            double p = regress(df, i);
            if (withClasses) {
                cr.firstClasses().setIndex(i, p < 0.5 ? 1 : 2);
            }
            if (withDistributions) {
                cr.firstDensity().setValue(i, 1, 1 - p);
                cr.firstDensity().setValue(i, 1, p);
            }
        }
        return cr;
    }
}
