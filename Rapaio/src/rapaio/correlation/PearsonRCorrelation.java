/*
 * Copyright 2013 Aurelian Tutuianu <padreati@yahoo.com>
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

package rapaio.correlation;

import rapaio.core.Summarizable;
import rapaio.core.stat.Mean;
import rapaio.core.stat.Variance;
import rapaio.data.Frame;
import rapaio.data.Vector;

import static rapaio.core.BaseMath.max;
import static rapaio.core.BaseMath.sqrt;
import static rapaio.explore.Workspace.*;

import java.util.Arrays;

/**
 * /**
 * Sample Pearson product-moment correlation coefficient.
 * <p/>
 * See
 * http://en.wikipedia.org/wiki/Pearson_product-moment_correlation_coefficient
 * <p/>
 * User: <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a>
 */
public class PearsonRCorrelation implements Summarizable {
    private final Vector[] vectors;
    private final double[][] pearson;

    public PearsonRCorrelation(Frame df) {
        this.vectors = new Vector[df.getColCount()];
        for (int i = 0; i < df.getColCount(); i++) {
            vectors[i] = df.getCol(i);
        }
        this.pearson = new double[vectors.length][vectors.length];
        for (int i = 0; i < vectors.length; i++) {
            pearson[i][i] = 1;
            for (int j = i + 1; j < vectors.length; j++) {
                pearson[i][j] = compute(vectors[i], vectors[j]);
                pearson[j][i] = pearson[i][j];
            }
        }
    }

    public PearsonRCorrelation(Vector... vectors) {
        this.vectors = vectors;
        this.pearson = new double[vectors.length][vectors.length];
        for (int i = 0; i < vectors.length; i++) {
            pearson[i][i] = 1;
            for (int j = i + 1; j < vectors.length; j++) {
                pearson[i][j] = compute(vectors[i], vectors[j]);
                pearson[j][i] = pearson[i][j];
            }
        }
    }

    private double compute(Vector x, Vector y) {
        double xMean = new Mean(x).getValue();
        double yMean = new Mean(y).getValue();
        double sum = 0;
        int len = max(x.getRowCount(), y.getRowCount());
        double sdp = sqrt(new Variance(x).getValue()) * sqrt(new Variance(y).getValue());
        double count = 0;
        for (int i = 0; i < len; i++) {
            if (x.isMissing(i) || y.isMissing(i)) {
                continue;
            }
            sum += ((x.getValue(i) - xMean) * (y.getValue(i) - yMean));
            count++;
        }
        return sum / (sdp * (count - 1));
    }

    public double[][] getValues() {
        return pearson;
    }

    @Override
    public void summary() {
        if (vectors.length == 1) {
            summaryOne();
            return;
        }
        if (vectors.length == 2) {
            summaryTwo();
            return;
        }
        summaryMore();
    }

    private void summaryOne() {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("pearson[%s] - Pearson product-moment correlation coefficient\n",
                vectors[0].getName()));
        sb.append("1\n");
        sb.append("pearson correlation is for identical vectors");
        code(sb.toString());
    }

    private void summaryTwo() {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("pearson[%s, %s] - Pearson product-moment correlation coefficient\n",
                vectors[0].getName(), vectors[1].getName()));
        sb.append(String.format("%.6f", pearson[0][1]));
        code(sb.toString());
    }

    private void summaryMore() {
        StringBuilder sb = new StringBuilder();
        String[] names = new String[vectors.length];
        for (int i = 0; i < names.length; i++) {
            names[i] = vectors[i].getName();
        }
        sb.append(String.format("pearson[%s] - Pearson product-moment correlation coefficient\n",
                Arrays.deepToString(names)));

        String[][] table = new String[vectors.length + 1][vectors.length + 1];
        table[0][0] = "";
        for (int i = 1; i < vectors.length + 1; i++) {
            table[0][i] = i + ".";
            table[i][0] = i + "." + vectors[i - 1].getName();
            for (int j = 1; j < vectors.length + 1; j++) {
                table[i][j] = String.format("%.6f", pearson[i - 1][j - 1]);
            }
        }

        int width = getPrinter().getTextWidth();
        int start = 0;
        int end = start;
        int[] ws = new int[table[0].length];
        for (int i = 0; i < table.length; i++) {
            for (int j = 0; j < table[0].length; j++) {
                ws[i] = max(ws[i], table[i][j].length());
            }
        }
        while (start < vectors.length + 1) {
            int w = 0;
            while ((end < (table[0].length - 1)) && ws[end + 1] + w + 1 < width) {
                w += ws[end + 1] + 1;
                end++;
            }
            for (int j = 0; j < table.length; j++) {
                for (int i = start; i <= end; i++) {
                    sb.append(String.format("%" + ws[i] + "s", table[i][j])).append(" ");
                }
                sb.append("\n");
            }
            start = end + 1;
        }
        code(sb.toString());
    }
}
