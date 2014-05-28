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
import rapaio.data.Frame;
import rapaio.data.Numeric;
import rapaio.data.Vector;

import java.util.Arrays;

import static rapaio.data.filters.BaseFilters.sort;
import static rapaio.workspace.W.getPrinter;

/**
 * Spearman's rank correlation coefficient.
 * <p>
 * You can compute coefficient for multiple vectors at the same time.
 * <p>
 * See: http://en.wikipedia.org/wiki/Spearman%27s_rank_correlation_coefficient
 * <p>
 * User: <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a>
 */
public class RhoCorr implements Printable {

    private final String[] names;
    private final Vector[] vectors;
    private final double[][] rho;

    public RhoCorr(Vector... vectors) {
        this.names = new String[vectors.length];
        for (int i = 0; i < names.length; i++) {
            names[i] = "V" + i;
        }
        this.vectors = vectors;
        this.rho = compute();
    }

    public RhoCorr(Frame df) {
        this.names = df.colNames();
        this.vectors = new Vector[df.colCount()];
        for (int i = 0; i < df.colCount(); i++) {
            vectors[i] = df.col(i);
        }
        this.rho = compute();
    }

    private double[][] compute() {
        Vector[] sorted = new Vector[vectors.length];
        Vector[] ranks = new Vector[vectors.length];
        for (int i = 0; i < sorted.length; i++) {
            sorted[i] = sort(vectors[i]);
            ranks[i] = new Numeric(vectors[i].rowCount());
        }

        // compute ranks
        for (int i = 0; i < sorted.length; i++) {
            int start = 0;
            while (start < sorted[i].rowCount()) {
                int end = start;
                while (end < sorted[i].rowCount() - 1 && sorted[i].value(end) == sorted[i].value(end + 1)) {
                    end++;
                }
                double value = 1 + (start + end) / 2.;
                for (int j = start; j <= end; j++) {
                    ranks[i].setValue(sorted[i].rowId(j), value);
                }
                start = end + 1;
            }
        }

        // compute Pearson on ranks
        return new PearsonRCorrelation(ranks).getValues();
    }

    public double[][] getValues() {
        return rho;
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
        sb.append(String.format("spearman[%s] - Spearman's rank correlation coefficient\n", names[0]));
        sb.append("1\n");
        sb.append("spearman's rank correlation is 1 for identical vectors\n");
    }

    private void summaryTwo(StringBuilder sb) {
        sb.append(String.format("spearman[%s, %s] - Spearman's rank correlation coefficient\n",
                names[0], names[1]));
        sb.append(String.format("%.6f\n", rho[0][1]));
    }

    private void summaryMore(StringBuilder sb) {
        sb.append(String.format("spearman[%s] - Spearman's rank correlation coefficient\n",
                Arrays.deepToString(names)));

        String[][] table = new String[vectors.length + 1][vectors.length + 1];
        table[0][0] = "";
        for (int i = 1; i < vectors.length + 1; i++) {
            table[0][i] = i + ".";
            table[i][0] = i + "." + names[i - 1];
            for (int j = 1; j < vectors.length + 1; j++) {
                table[i][j] = String.format("%.6f", rho[i - 1][j - 1]);
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
