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

package rapaio.core.correlation;

import rapaio.core.Printable;
import rapaio.core.stat.Mean;
import rapaio.core.stat.Variance;
import rapaio.data.Frame;
import rapaio.data.Vector;

import java.util.Arrays;

import static rapaio.workspace.W.getPrinter;

/**
 * /**
 * Pearson product-moment correlation coefficient.
 * <p>
 * See
 * http://en.wikipedia.org/wiki/Pearson_product-moment_correlation_coefficient
 * <p>
 * User: <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a>
 */
public class PearsonRCorrelation implements Printable {

    private final String[] names;
    private final Vector[] vectors;
    private final double[][] pearson;

    public PearsonRCorrelation(Frame df) {
        this.names = df.colNames();
        this.vectors = new Vector[df.colCount()];
        for (int i = 0; i < df.colCount(); i++) {
            vectors[i] = df.col(i);
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
        this.names = new String[vectors.length];
        for (int i = 0; i < names.length; i++) {
            names[i] = "V" + i;
        }
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
        int len = Math.min(x.rowCount(), y.rowCount());
        double sdp = Math.sqrt(new Variance(x).getValue()) * Math.sqrt(new Variance(y).getValue());
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
    public void buildSummary(StringBuilder sb) {
        switch (vectors.length) {
            case 1:
                summaryOne(sb);
                break;
            case 2:
                summaryTwo(sb);
                break;
            default:
                summaryMore(sb);
        }
    }

    private void summaryOne(StringBuilder sb) {
        sb.append(String.format("> pearson[%s] - Pearson product-moment correlation coefficient\n",
                names[0]));
        sb.append("1\n");
        sb.append("pearson correlation is 1 for identical vectors\n");
    }

    private void summaryTwo(StringBuilder sb) {
        sb.append(String.format("> pearson[%s, %s] - Pearson product-moment correlation coefficient\n",
                names[0], names[1]));
        sb.append(String.format("%.6f\n", pearson[0][1]));
    }

    private void summaryMore(StringBuilder sb) {
        sb.append(String.format("> pearson[%s] - Pearson product-moment correlation coefficient\n",
                Arrays.deepToString(names)));

        String[][] table = new String[vectors.length + 1][vectors.length + 1];
        table[0][0] = "";
        for (int i = 1; i < vectors.length + 1; i++) {
            table[0][i] = i + ".";
            table[i][0] = i + "." + names[i - 1];
            for (int j = 1; j < vectors.length + 1; j++) {
                table[i][j] = String.format("%.2f", pearson[i - 1][j - 1]);
                if (i == j) {
                    table[i][j] = "x";
                }
            }
        }

        int width = getPrinter().getTextWidth();
        int start = 0;
        int end = start;
        int[] ws = new int[table[0].length];
        for (int i = 0; i < table.length; i++) {
            for (int j = 0; j < table[0].length; j++) {
                ws[i] = Math.max(ws[i], table[i][j].length());
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
    }
}
