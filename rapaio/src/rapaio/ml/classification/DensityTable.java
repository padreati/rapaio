/*
 * Apache License
 * Version 2.0, January 2004
 * http://www.apache.org/licenses/
 *
 *    Copyright 2013 Aurelian Tutuianu
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
 */
package rapaio.ml.classification;

import rapaio.data.Frame;

import java.util.List;

import static rapaio.core.BaseMath.log2;

/**
 * @author <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a>
 */
public final class DensityTable {

    public static final String[] NUMERIC_DEFAULT_LABELS = new String[]{"?", "less-equals", "greater"};

    private final String[] testLabels;
    private final String[] classLabels;

    private final double[][] values;

    public DensityTable(String[] testLabels, String[] classLabels) {
        this.testLabels = testLabels;
        this.classLabels = classLabels;
        values = new double[testLabels.length][classLabels.length];
    }

    public DensityTable(Frame df, String testColName, String classColName) {
        this(df, null, testColName, classColName);
    }

    public DensityTable(Frame df, List<Double> weights, String testColName, String classColName) {
        this(df.getCol(testColName).getDictionary(), df.getCol(classColName).getDictionary());
        for (int i = 0; i < df.getRowCount(); i++) {
            update(df.getIndex(i, testColName), df.getIndex(i, classColName), weights != null ? weights.get(i) : 1.);
        }
    }

    public void reset() {
        for (int i = 0; i < testLabels.length; i++) {
            for (int j = 0; j < classLabels.length; j++) {
                values[i][j] = 0;
            }
        }
    }

    public void update(int row, int col, double weight) {
        values[row][col] += weight;
    }

    public void move(int row1, int row2, int col, double weight) {
        update(row1, col, -weight);
        update(row2, col, weight);
    }

    public double getEntropy() {
        return getEntropy(false);
    }

    public double getEntropy(boolean useMissing) {
        double entropy = 0;
        double[] totals = new double[classLabels.length];
        for (int i = 1; i < testLabels.length; i++) {
            for (int j = 1; j < classLabels.length; j++) {
                totals[j] += values[i][j];
            }
        }
        double total = 0;
        for (int i = 1; i < totals.length; i++) {
            total += totals[i];
        }
        for (int i = 1; i < totals.length; i++) {
            if (totals[i] > 0) {
                entropy += -log2(totals[i] / total) * totals[i] / total;
            }
        }
        double factor = 1.;
        if (useMissing) {
            double missing = 0;
            for (int i = 1; i < classLabels.length; i++) {
                missing += values[0][i];
            }
            factor = total / (missing + total);
        }
        return factor * entropy;
    }

    public double getInfoXGain() {
        return getInfoXGain(false);
    }

    public double getInfoXGain(boolean useMissing) {
        double[] totals = new double[testLabels.length];
        for (int i = 1; i < testLabels.length; i++) {
            for (int j = 1; j < classLabels.length; j++) {
                totals[i] += values[i][j];
            }
        }
        double total = 0;
        for (int i = 1; i < totals.length; i++) {
            total += totals[i];
        }
        double gain = 0;
        for (int i = 1; i < testLabels.length; i++) {
            for (int j = 1; j < classLabels.length; j++) {
                if (values[i][j] > 0)
                    gain += -log2(values[i][j] / totals[i]) * values[i][j] / total;
            }
        }
        double factor = 1.;
        if (useMissing) {
            double missing = 0;
            for (int i = 0; i < classLabels.length; i++) {
                missing += values[0][i];
            }
            factor = total / (missing + total);
        }
        return factor * gain;
    }

    public double getInfoGain() {
        return getInfoGain(false);
    }

    public double getInfoGain(boolean useMissing) {
        return getEntropy(useMissing) - getInfoXGain(useMissing);
    }

    public double getSplitInfo() {
        return getSplitInfo(false);
    }

    public double getSplitInfo(boolean useMissing) {
        int start = useMissing ? 0 : 1;
        double[] totals = new double[testLabels.length];
        for (int i = start; i < testLabels.length; i++) {
            for (int j = 1; j < classLabels.length; j++) {
                totals[i] += values[i][j];
            }
        }
        double total = 0;
        for (int i = 1; i < totals.length; i++) {
            total += totals[i];
        }
        double splitInfo = 0;
        for (int i = 1; i < totals.length; i++) {
            if (totals[i] > 0) {
                splitInfo += -log2(totals[i] / total) * totals[i] / total;
            }
        }
        return splitInfo;
    }

    public double getGainRatio() {
        return getGainRatio(false);
    }

    public double getGainRatio(boolean useMissing) {
        return getInfoGain(useMissing) / getSplitInfo(useMissing);
    }

    /**
     * Computes the number of columns which have totals equal or greater than minWeight
     *
     * @param useMissing if on counting the missing row is used
     * @return number of columns which meet criteria
     */
    public int countWithMinimum(boolean useMissing, double minWeight) {
        int start = useMissing ? 0 : 1;
        double[] totals = new double[testLabels.length];
        for (int i = start; i < testLabels.length; i++) {
            for (int j = 1; j < classLabels.length; j++) {
                totals[i] += values[i][j];
            }
        }
        int count = 0;
        for (int i = 1; i < totals.length; i++) {
            if (totals[i] >= minWeight) {
                count++;
            }
        }
        return count;
    }
}
