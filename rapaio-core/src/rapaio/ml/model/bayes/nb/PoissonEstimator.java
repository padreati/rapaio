/*
 * Apache License
 * Version 2.0, January 2004
 * http://www.apache.org/licenses/
 *
 * Copyright 2013 - 2022 Aurelian Tutuianu
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package rapaio.ml.model.bayes.nb;

import java.io.Serial;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import rapaio.core.distributions.Poisson;
import rapaio.core.tools.DensityVector;
import rapaio.data.Frame;
import rapaio.data.Var;
import rapaio.data.VarRange;
import rapaio.printer.Format;

/**
 * Poisson event based estimator for Naive Bayes. This estimator fits a Poisson
 * distribution on the feature which must be an integer positive valued variable.
 * <p>
 * Using other types of variables will not crush the estimator, but obviously
 * it will produce useless results.
 * <p>
 * The Poisson distribution is fitted by approximating the lambda (frequency) parameter
 * with the sample mean value. This MLE estimator is efficient in statistical sense.
 * <p>
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 3/4/20.
 */
public class PoissonEstimator extends AbstractEstimator {

    public static PoissonEstimator forName(String testName) {
        return new PoissonEstimator(testName);
    }

    public static List<PoissonEstimator> forNames(String... testNames) {
        return Arrays.stream(testNames).map(PoissonEstimator::new).collect(Collectors.toList());
    }

    public static List<PoissonEstimator> forRange(Frame df, VarRange varRange) {
        return varRange.parseVarNames(df).stream().map(PoissonEstimator::new).collect(Collectors.toList());
    }

    @Serial
    private static final long serialVersionUID = -75942136113239906L;
    private static final double eps = 1e-100;
    private static final Logger LOGGER = Logger.getLogger(PoissonEstimator.class.getName());

    private final String testName;
    private final HashMap<String, Poisson> lambdaMap = new HashMap<>();

    private PoissonEstimator(String testName) {
        super(Collections.singletonList(testName));
        this.testName = testName;
    }

    public String getTestName() {
        return testName;
    }

    public HashMap<String, Poisson> getLambdaMap() {
        return lambdaMap;
    }

    @Override
    public PoissonEstimator newInstance() {
        return new PoissonEstimator(getTestName());
    }

    @Override
    public String name() {
        return "Poisson{test=" + testName + "}";
    }

    @Override
    public String fittedName() {
        StringBuilder sb = new StringBuilder();
        sb.append("Poisson{test=").append(testName);
        sb.append(", values=[");
        if (!lambdaMap.isEmpty()) {
            sb.append(lambdaMap.entrySet()
                    .stream()
                    .map(e -> "{level:" + e.getKey() + ",lambda:" + Format.floatFlexShort(e.getValue().getLambda()) + "}")
                    .collect(Collectors.joining(",")));
        }
        sb.append("]}");
        return sb.toString();
    }

    @Override
    public boolean fit(Frame df, Var weights, String targetName) {
        boolean valid = true;
        Var test = df.rvar(testName);
        for (int i = 0; i < test.size(); i++) {
            double value = test.getDouble(i);
            if (value < 0 || Math.abs(value - Math.rint(value)) > 1e-100) {
                valid = false;
                break;
            }
        }
        if (!valid) {
            LOGGER.fine("Variable does not contain only positive integer values: Poisson estimator cannot be fitted.");
            return false;
        }
        var counts = DensityVector.fromLevelCounts(false, df.rvar(targetName));
        var sums = DensityVector.fromLevelWeights(false, df.rvar(targetName), df.rvar(testName));
        for (String level : counts.index().getValues()) {
            double count = counts.get(level);
            double sum = sums.get(level);
            double lambda = sum / count;
            if (Double.isFinite(lambda) && lambda > 0) {
                lambdaMap.put(level, Poisson.of(lambda));
            } else {
                lambdaMap.put(level, Poisson.of(1e-20));
            }
        }
        return true;
    }

    @Override
    public double predict(Frame df, int row, String targetLevel) {
        if (lambdaMap.containsKey(targetLevel)) {
            if (df.isMissing(row, testName)) {
                return lambdaMap.get(targetLevel).pdf(0);
            }
            return lambdaMap.get(targetLevel).pdf(df.getDouble(row, testName));
        }
        return eps;
    }
}
