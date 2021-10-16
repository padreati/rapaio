/*
 * Apache License
 * Version 2.0, January 2004
 * http://www.apache.org/licenses/
 *
 * Copyright 2013 - 2021 Aurelian Tutuianu
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package rapaio.ml.supervised.bayes.nb;

import java.io.Serial;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import rapaio.core.tools.DensityTable;
import rapaio.data.Frame;
import rapaio.data.Var;
import rapaio.data.VarRange;
import rapaio.data.VarType;
import rapaio.printer.Format;

/**
 * Binomial pmf estimator.
 * <p>
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 5/18/15.
 */
public class BernoulliEstimator extends AbstractEstimator {

    public static BernoulliEstimator forName(String testVarName) {
        return new BernoulliEstimator(testVarName, 1);
    }

    public static BernoulliEstimator forName(String testVarName, double laplaceSmoother) {
        return new BernoulliEstimator(testVarName, laplaceSmoother);
    }

    public static List<BernoulliEstimator> forNames(String... testVarNames) {
        return Arrays.stream(testVarNames).map(name -> new BernoulliEstimator(name, 1)).collect(Collectors.toList());
    }

    public static List<BernoulliEstimator> forNames(double laplaceSmoother, String... testVarNames) {
        return Arrays.stream(testVarNames).map(name -> new BernoulliEstimator(name, laplaceSmoother)).collect(Collectors.toList());
    }

    public static List<BernoulliEstimator> forRange(Frame df, VarRange varRange) {
        return forRange(1, df, varRange);
    }

    public static List<BernoulliEstimator> forRange(double laplaceSmoother, Frame df, VarRange varRange) {
        List<String> varNames = varRange.parseVarNames(df);
        return forNames(laplaceSmoother, varNames.toArray(String[]::new));
    }

    @Serial
    private static final long serialVersionUID = 3019563706421891472L;

    private final String testName;
    private final double laplaceSmoother;

    private DensityTable<String, String> density;

    private BernoulliEstimator(String testName, double laplaceSmoother) {
        super(Collections.singletonList(testName));
        this.testName = testName;
        this.laplaceSmoother = laplaceSmoother;
    }

    @Override
    public BernoulliEstimator newInstance() {
        return new BernoulliEstimator(testName, laplaceSmoother);
    }

    @Override
    public String name() {
        return "Bernoulli{test=" + testName + "}";
    }

    @Override
    public String fittedName() {
        StringBuilder sb = new StringBuilder();
        sb.append("Bernoulli{test=").append(testName);
        sb.append(", laplaceSmoother=").append(Format.floatFlex(laplaceSmoother));
        sb.append(", values=[");
        if (density != null) {
            for (String targetLevel : density.colIndex().getValues()) {
                sb.append("{targetLevel:").append(targetLevel).append(",[");
                for (String testLevel : density.rowIndex().getValues()) {
                    sb.append(testLevel).append(":").append(Format.floatFlexShort(density.get(testLevel, targetLevel))).append(",");
                }
                sb.append("},");
            }
        }
        sb.append("]}");
        return sb.toString();
    }

    @Override
    public boolean fit(Frame df, Var weights, String targetName) {
        if (!df.type(testName).equals(VarType.BINARY)) {
            return false;
        }
        var density = DensityTable.fromLevelCounts(true, df, testName, targetName);
        for (String targetLevel : density.colIndex().getValues()) {
            for (String testLevel : density.rowIndex().getValues()) {
                density.increment(testLevel, targetLevel, laplaceSmoother);
            }
        }
        this.density = density.normalizeOnRows();
        this.hasLearned = true;
        return true;
    }

    @Override
    public double predict(Frame df, int row, String targetLevel) {
        String testLabel = df.getLabel(row, testName);
        if (!density.colIndex().containsValue(targetLevel)) {
            return 0.0;
        }
        return density.get(testLabel, targetLevel);
    }
}
