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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import rapaio.core.tools.DensityTable;
import rapaio.data.Frame;
import rapaio.data.Var;
import rapaio.data.VarRange;
import rapaio.data.VarType;
import rapaio.printer.Format;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 2/28/20.
 */
public class MultinoulliEstimator extends AbstractEstimator {

    public static MultinoulliEstimator forName(String testName) {
        return new MultinoulliEstimator(Collections.singletonList(testName), 1);
    }

    public static MultinoulliEstimator forName(double laplaceSmoother, String testName) {
        return new MultinoulliEstimator(Collections.singletonList(testName), laplaceSmoother);
    }

    public static MultinoulliEstimator forNames(String... testNames) {
        return new MultinoulliEstimator(Arrays.asList(testNames), 1);
    }

    public static MultinoulliEstimator forNames(double laplaceSmoother, String... testNames) {
        return new MultinoulliEstimator(Arrays.asList(testNames), laplaceSmoother);
    }

    public static MultinoulliEstimator forRange(Frame df, VarRange varRange) {
        return new MultinoulliEstimator(varRange.parseVarNames(df), 1);
    }

    public static MultinoulliEstimator forRange(double laplaceSmoother, Frame df, VarRange varRange) {
        return new MultinoulliEstimator(varRange.parseVarNames(df), laplaceSmoother);
    }

    @Serial
    private static final long serialVersionUID = 4232189912660290961L;
    private final double laplaceSmoother;

    // learning artifacts
    private int ourCase = -1; // 1 - single nominal, 2 - multiple binary
    private DensityTable<String, String> density;

    private MultinoulliEstimator(List<String> varNames, double laplaceSmoother) {
        super(varNames);
        this.laplaceSmoother = laplaceSmoother;
    }

    @Override
    public MultinoulliEstimator newInstance() {
        return new MultinoulliEstimator(getTestNames(), getLaplaceSmoother());
    }

    @Override
    public String name() {
        return "Multinoulli{tests=[" + String.join(",", getTestNames()) + "], " +
                "lapaceSmoother=" + Format.floatFlex(getLaplaceSmoother()) + "}";
    }

    @Override
    public String fittedName() {
        StringBuilder sb = new StringBuilder();
        sb.append("Multinoulli{tests=[").append(String.join(",", getTestNames())).append("], ");
        sb.append("laplaceSmoother=").append(Format.floatFlex(laplaceSmoother));
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

    public double getLaplaceSmoother() {
        return laplaceSmoother;
    }

    @Override
    public boolean fit(Frame df, Var weights, String targetName) {
        // validate variables
        ourCase = validate(df, targetName);
        return switch (ourCase) {
            case 1 ->
                    // one nominal variable
                    fitNominal(df, targetName);
            case 2 ->
                    // multiple binary variables summed to 1
                    fitBinary(df, targetName);
            default -> false;
        };
    }

    private int validate(Frame df, String targetName) {
        Frame selection = df.mapVars(getTestNames());
        if (getTestNames().size() == 1) {
            // single nominal variable
            String testVarName = getTestNames().get(0);
            if (selection.type(testVarName).equals(VarType.BINARY) || selection.type(testVarName).equals(VarType.NOMINAL)) {
                return 1;
            }
            throw new IllegalArgumentException("Selected test variable does not have binary or nominal type.");
        }
        // all variables must be binary and obey sum to 1 rule
        for (String testVarName : selection.varNames()) {
            if (!selection.type(testVarName).equals(VarType.BINARY)) {
                throw new IllegalArgumentException("Selected test variables are not binary.");
            }
        }
        for (int i = 0; i < selection.rowCount(); i++) {
            double sum = 0;
            for (String testVarName : selection.varNames()) {
                sum += Math.abs(selection.getDouble(i, testVarName));
            }
            if (sum >= 1 + 1e-12) {
                throw new IllegalArgumentException("Selected binary variables does not sum to 1.");
            }
        }
        return 2;
    }

    private boolean fitNominal(Frame df, String targetName) {
        DensityTable<String, String> dt = DensityTable.fromLevelCounts(true, df, getTestNames().get(0), targetName);
        for (int i = 0; i < dt.rowCount(); i++) {
            for (int j = 0; j < dt.colCount(); j++) {
                dt.increment(i, j, laplaceSmoother);
            }
        }
        density = dt.normalizeOnCols();
        return true;
    }

    private boolean fitBinary(Frame df, String targetName) {
        List<String> fullTestVarNames = new ArrayList<>(getTestNames());

        DensityTable<String, String> dt = DensityTable.emptyByLabel(true, fullTestVarNames, df.levels(targetName));

        // increment lapace smoother
        for (int i = 0; i < dt.rowCount(); i++) {
            for (int j = 0; j < dt.colCount(); j++) {
                dt.increment(i, j, laplaceSmoother);
            }
        }

        // increment with counts from data
        for (int i = 0; i < df.rowCount(); i++) {
            String testName = "";
            for (String testVarName : dt.rowIndex().getValues()) {
                if (df.getInt(i, testVarName) == 1) {
                    testName = testVarName;
                    break;
                }
            }
            if (testName.isEmpty()) {
                throw new IllegalArgumentException("No binary value equals 1, from all candidate values.");
            }
            dt.increment(testName, df.getLabel(i, targetName), 1);
        }
        density = dt.normalizeOnCols();
        return true;
    }

    @Override
    public double predict(Frame df, int row, String targetLevel) {
        if (!density.colIndex().containsValue(targetLevel)) {
            return Double.NaN;
        }
        String testLabel = "?";
        switch (ourCase) {
            case 1:
                // single nominal variable
                String testLabelNominal = df.getLabel(row, getTestNames().get(0));
                if (density.rowIndex().containsValue(testLabelNominal)) {
                    testLabel = testLabelNominal;
                }
                break;
            case 2:
                // multiple binary variables
                for (String testLabelBinary : density.rowIndex().getValues()) {
                    if (df.getInt(row, testLabelBinary) == 1) {
                        testLabel = testLabelBinary;
                        break;
                    }
                }
                break;
        }
        return density.get(testLabel, targetLevel);
    }
}
