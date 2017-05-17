/*
 * Apache License
 * Version 2.0, January 2004
 * http://www.apache.org/licenses/
 *
 *    Copyright 2013 Aurelian Tutuianu
 *    Copyright 2014 Aurelian Tutuianu
 *    Copyright 2015 Aurelian Tutuianu
 *    Copyright 2016 Aurelian Tutuianu
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
public class CorrSpearman implements Printable {

    public static CorrSpearman from(Frame df) {
        return new CorrSpearman(df);
    }

    public static CorrSpearman from(Var... vars) {
        return new CorrSpearman(vars);
    }

    private final String[] names;
    private final Var[] vars;
    private final double[][] rho;

    private CorrSpearman(Var... vars) {

        int rowCount = Integer.MAX_VALUE;
        for (Var var : vars) {
            rowCount = Math.min(var.getRowCount(), rowCount);
        }

        Mapping map = Mapping.copy(IntStream.range(0, rowCount)
                .filter(row -> {
                    for (Var var : vars) {
                        if (var.isMissing(row))
                            return false;
                    }
                    return true;
                })
                .toArray());

        this.names = new String[vars.length];
        for (int i = 0; i < names.length; i++) {
            names[i] = "V" + i;
        }
        this.vars = new Var[vars.length];
        for (int i = 0; i < vars.length; i++) {
            this.vars[i] = vars[i].mapRows(map);
        }
        this.rho = compute();
    }

    private CorrSpearman(Frame df) {

        Mapping map = Mapping.copy(IntStream.range(0, df.getRowCount())
                .filter(row -> !df.isMissing(row))
                .toArray());

        this.names = df.getVarNames();
        this.vars = new Var[df.getVarCount()];
        for (int i = 0; i < df.getVarCount(); i++) {
            vars[i] = df.getVar(i).mapRows(map);
        }
        this.rho = compute();
    }

    private double[][] compute() {
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
        return CorrPearson.from(ranks).values();
    }

    public double[][] values() {
        return rho;
    }

    @Override
    public String getSummary() {
        StringBuilder sb = new StringBuilder();
        switch (vars.length) {
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
                "> spearman[%s] - Spearman's rank correlation coefficient\n", names[0]));
        sb.append("1\n");
        sb.append("spearman's rank correlation is 1 for identical vectors\n");
    }

    private void summaryTwo(StringBuilder sb) {
        sb.append(String.format("\n" +
                        "> spearman[%s, %s] - Spearman's rank correlation coefficient\n",
                names[0], names[1]));
        sb.append(formatFlex(rho[0][1])).append("\n");
    }

    private void summaryMore(StringBuilder sb) {
        sb.append(String.format("\n" +
                        "> spearman[%s] - Spearman's rank correlation coefficient\n",
                Arrays.deepToString(names)));

        String[][] table = new String[vars.length + 1][vars.length + 1];
        table[0][0] = "";
        for (int i = 1; i < vars.length + 1; i++) {
            table[0][i] = i + ".";
            table[i][0] = i + "." + names[i - 1];
            for (int j = 1; j < vars.length + 1; j++) {
                table[i][j] = formatFlex(rho[i - 1][j - 1]);
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
        while (start < vars.length + 1) {
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
        if (names.length == 1)
            return 1;
        return rho[0][1];
    }
}
