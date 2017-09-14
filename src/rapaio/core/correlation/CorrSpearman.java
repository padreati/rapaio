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

package rapaio.core.correlation;

import rapaio.core.tools.DistanceMatrix;
import rapaio.data.*;
import rapaio.data.filter.var.VFRefSort;
import rapaio.printer.Printable;

import java.util.Arrays;
import java.util.stream.IntStream;

import static rapaio.sys.WS.*;

/**
 * Spearman's rank correlation coefficient.
 * <p>
 * You can compute coefficient for multiple vectors at the same time.
 * <p>
 * See: http://en.wikipedia.org/wiki/Spearman%27s_rank_correlation_coefficient
 * <p>
 * User: <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a>
 */
public class CorrSpearman implements Correlation, Printable {

    public static CorrSpearman from(Frame df) {
        return new CorrSpearman(df.varStream().toArray(Var[]::new));
    }

    public static CorrSpearman from(Var... vars) {
        return new CorrSpearman(vars);
    }

    private final DistanceMatrix d;

    private CorrSpearman(Var... variables) {

        int rowCount = Integer.MAX_VALUE;
        for (Var var : variables) {
            rowCount = Math.min(var.getRowCount(), rowCount);
        }

        Mapping map = Mapping.copy(IntStream.range(0, rowCount)
                .filter(row -> {
                    for (Var var : variables) {
                        if (var.isMissing(row))
                            return false;
                    }
                    return true;
                })
                .toArray());

        Var[] vars = new Var[variables.length];
        for (int i = 0; i < vars.length; i++) {
            vars[i] = variables[i].mapRows(map);
        }
        d = compute(vars);
    }

    private DistanceMatrix compute(Var[] vars) {
        String[] names = new String[vars.length];
        for (int i = 0; i < names.length; i++) {
            names[i] = vars[i].getName();
        }

        Var[] sorted = new Var[vars.length];
        Var[] ranks = new Var[vars.length];
        for (int i = 0; i < sorted.length; i++) {
            IndexVar index = IndexVar.seq(vars[i].getRowCount());
            sorted[i] = new VFRefSort(RowComparators.numeric(vars[i], true)).fitApply(index);
            ranks[i] = NumericVar.fill(vars[i].getRowCount());
        }

        // compute ranks
        for (int i = 0; i < sorted.length; i++) {
            int start = 0;
            while (start < sorted[i].getRowCount()) {
                int end = start;
                while (end < sorted[i].getRowCount() - 1 && sorted[i].getValue(end) == sorted[i].getValue(end + 1)) {
                    end++;
                }
                double value = 1 + (start + end) / 2.;
                for (int j = start; j <= end; j++) {
                    ranks[i].setValue(sorted[i].getIndex(j), value);
                }
                start = end + 1;
            }
        }

        // compute Pearson on ranks
        return CorrPearson.from(ranks).getMatrix();
    }

    @Override
    public DistanceMatrix getMatrix() {
        return d;
    }

    @Override
    public String getSummary() {
        StringBuilder sb = new StringBuilder();
        switch (d.getNames().length) {
            case 1:
                summaryOne(sb);
                break;
            case 2:
                summaryTwo(sb);
                break;
            default:
                summaryMore(sb);
        }
        return sb.toString();
    }

    private void summaryOne(StringBuilder sb) {
        sb.append(String.format("\n" +
                "> spearman[%s] - Spearman's rank correlation coefficient\n", d.getName(0)));
        sb.append("1\n");
        sb.append("spearman's rank correlation is 1 for identical vectors\n");
    }

    private void summaryTwo(StringBuilder sb) {
        sb.append(String.format("\n" +
                        "> spearman[%s, %s] - Spearman's rank correlation coefficient\n",
                d.getName(0), d.getName(1)));
        sb.append(formatFlex(d.get(0, 1))).append("\n");
    }

    private void summaryMore(StringBuilder sb) {
        sb.append(String.format("\n" +
                        "> spearman[%s] - Spearman's rank correlation coefficient\n",
                Arrays.deepToString(d.getNames())));

        String[][] table = new String[d.getNames().length + 1][d.getNames().length + 1];
        table[0][0] = "";
        for (int i = 1; i < d.getNames().length + 1; i++) {
            table[0][i] = i + ".";
            table[i][0] = i + "." + d.getName(i - 1);
            for (int j = 1; j < d.getNames().length + 1; j++) {
                table[i][j] = formatFlex(d.get(i - 1, j - 1));
                if (i == j) {
                    table[i][j] = "x";
                }
            }
        }

        int width = getPrinter().textWidth();
        int start = 0;
        int end = start;
        int[] ws = new int[table[0].length];
        for (int i = 0; i < table.length; i++) {
            for (int j = 0; j < table[0].length; j++) {
                ws[i] = Math.max(ws[i], table[i][j].length());
            }
        }
        while (start < d.getNames().length + 1) {
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

    public double singleValue() {
        if (d.getNames().length == 1)
            return 1;
        return d.get(0, 1);
    }
}
