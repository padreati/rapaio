/*
 * Apache License
 * Version 2.0, January 2004
 * http://www.apache.org/licenses/
 *
 *    Copyright 2013 - 2021 Aurelian Tutuianu
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

package rapaio.ml.classifier.bayes.nb;

import rapaio.core.distributions.Distribution;
import rapaio.core.distributions.Normal;
import rapaio.core.stat.OnlineStat;
import rapaio.data.Frame;
import rapaio.data.VRange;
import rapaio.data.VType;
import rapaio.data.Var;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Naive Bayes Gaussian estimator uses the Normal distribution to model
 * a numerical feature. The used Gaussian distribution is fitted on test variable
 * conditioned by target label. Thus, this event model assumes each conditional
 * distribution is Gaussian and they are fitted separately.
 * <p>
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 5/18/15.
 */
public class GaussianEstimator extends AbstractEstimator {

    public static GaussianEstimator forName(String testName) {
        return new GaussianEstimator(testName);
    }

    public static List<GaussianEstimator> forRange(Frame df, VRange varRange) {
        return varRange.parseVarNames(df).stream().map(GaussianEstimator::new).collect(Collectors.toList());
    }

    public static List<GaussianEstimator> forType(Frame df, VType type) {
        return forRange(df, VRange.onlyTypes(type));
    }

    public static List<GaussianEstimator> forNames(String... names) {
        return Arrays.stream(names).map(GaussianEstimator::new).collect(Collectors.toList());
    }

    private static final long serialVersionUID = -5974296887792054267L;

    private final Map<String, Normal> normals = new HashMap<>();
    private final String testName;

    private GaussianEstimator(String testName) {
        super(Collections.singletonList(testName));
        this.testName = testName;
    }

    @Override
    public Estimator newInstance() {
        return new GaussianEstimator(testName);
    }

    @Override
    public String name() {
        return "Gaussian{test=" + testName + "}";
    }

    @Override
    public String fittedName() {
        return "Gaussian{test=" + testName + ", " +
                "values=[" + normals.entrySet().stream().map(e -> e.getKey() + ":" + e.getValue().name()).collect(Collectors.joining(", ")) + "]}";
    }

    public List<String> getTargetLevels() {
        return new ArrayList<>(normals.keySet());
    }

    public Normal getFittedNormal(String targetValue) {
        return normals.get(targetValue);
    }

    @Override
    public boolean fit(Frame df, Var weights, String targetName) {

        String[] varNames = df.varNames();
        boolean foundTest = false;
        boolean foundTarget = false;
        for (String varName : varNames) {
            if (varName.equals(testName)) {
                foundTest = true;
            }
            if (varName.equals(targetName)) {
                foundTarget = true;
            }
        }
        if (!(foundTest && foundTarget)) {
            return false;
        }

        normals.clear();

        Map<String, OnlineStat> stats = new HashMap<>();
        for (int i = 0; i < df.rowCount(); i++) {
            if (df.isMissing(i, targetName)) {
                continue;
            }
            String label = df.getLabel(i, targetName);
            stats.computeIfAbsent(label, key -> OnlineStat.empty())
                    .update(df.getDouble(i, testName));
        }
        for (String label : df.levels(targetName)) {
            if ("?".equals(label)) {
                continue;
            }
            double mu = stats.get(label).mean();
            double sd = stats.get(label).sd();
            normals.put(label, Normal.of(mu, sd));
        }
        hasLearned = true;
        return true;
    }

    @Override
    public double predict(Frame df, int row, String targetLevel) {
        Distribution normal = normals.get(targetLevel);
        double testValue = df.getDouble(row, getTestNames().get(0));
        if (Math.abs(normal.var()) < 1e-20) {
            return (Math.abs(normal.mean() - testValue) < 1e-20) ? 1.0 : 0.0;
        }
        return normals.get(targetLevel).pdf(testValue);
    }
}
