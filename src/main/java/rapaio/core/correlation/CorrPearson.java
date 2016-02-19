/*
 * Apache License
 * Version 2.0, January 2004
 * http://www.apache.org/licenses/
 *
 *    Copyright 2013 Aurelian Tutuianu
 *    Copyright 2014 Aurelian Tutuianu
 *    Copyright 2015 Aurelian Tutuianu
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

import rapaio.core.stat.Mean;
import rapaio.core.stat.Variance;
import rapaio.data.Frame;
import rapaio.data.Mapping;
import rapaio.data.Var;
import rapaio.printer.Printable;

import java.util.Arrays;
import java.util.List;
import java.util.stream.IntStream;

import static rapaio.sys.WS.*;

/**
 * /**
 * Pearson product-moment correlation coefficient.
 * <p>
 * See
 * http://en.wikipedia.org/wiki/Pearson_product-moment_correlation_coefficient
 * <p>
 * User: <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a>
 */
public class CorrPearson implements Printable {

    private final String[] names;
    private final double[][] pearson;

    public CorrPearson(Frame df) {
        List<Var> varList = df.varList();
        this.names = df.varNames();
        this.pearson = new double[varList.size()][varList.size()];
        for (int i = 0; i < df.varCount(); i++) {
            pearson[i][i] = 1;
            for (int j = i + 1; j < varList.size(); j++) {
                pearson[i][j] = compute(varList.get(i), varList.get(j));
                pearson[j][i] = pearson[i][j];
            }
        }
    }

    public CorrPearson(Var... vars) {
        List<Var> varList = Arrays.asList(vars);
        this.names = new String[vars.length];
        for (int i = 0; i < names.length; i++) {
            names[i] = vars[i].name();
            if (names[i].isEmpty())
                names[i] = "V" + i;
        }
        this.pearson = new double[vars.length][vars.length];
        for (int i = 0; i < vars.length; i++) {
            pearson[i][i] = 1;
            for (int j = i + 1; j < vars.length; j++) {
                pearson[i][j] = compute(vars[i], vars[j]);
                pearson[j][i] = pearson[i][j];
            }
        }
    }

    private double compute(Var x, Var y) {

        double sum = 0;
        int len = Math.min(x.rowCount(), y.rowCount());

        Mapping map = Mapping.copy(IntStream.range(0, len)
                .filter(i -> !(x.missing(i) || y.missing(i)))
                .toArray());
        double xMean = new Mean(x.mapRows(map)).value();
        double yMean = new Mean(y.mapRows(map)).value();

        double sdp = Math.sqrt(new Variance(x.mapRows(map)).value())
                * Math.sqrt(new Variance(y.mapRows(map)).value());
        for (int i = 0; i < map.size(); i++) {
            int pos = map.get(i);
            sum += ((x.value(pos) - xMean) * (y.value(pos) - yMean));
        }
        return sdp == 0 ? 0.0 : sum / (sdp * (map.size() - 1));
    }

    public double[][] values() {
        return pearson;
    }

    public double singleValue() {
        if (names.length == 1)
            return 1;
        return pearson[0][1];
    }

    @Override
    public String summary() {
        StringBuilder sb = new StringBuilder();
        switch (names.length) {
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
                        "> pearson[%s] - Pearson product-moment correlation coefficient\n",
                names[0]));
        sb.append("1\n");
        sb.append("pearson correlation is 1 for identical vectors\n");
    }

    private void summaryTwo(StringBuilder sb) {
        sb.append(String.format("\n" +
                        "> pearson[%s, %s] - Pearson product-moment correlation coefficient\n",
                names[0], names[1]));
        sb.append(formatFlex(pearson[0][1])).append("\n");
    }

    private void summaryMore(StringBuilder sb) {
        sb.append(String.format("\n" +
                        "> pearson[%s] - Pearson product-moment correlation coefficient\n",
                Arrays.deepToString(names)));

        String[][] table = new String[names.length + 1][names.length + 1];
        table[0][0] = "";
        for (int i = 1; i < names.length + 1; i++) {
            table[0][i] = i + ".";
            table[i][0] = i + "." + names[i - 1];
            for (int j = 1; j < names.length + 1; j++) {
                table[i][j] = formatShort(pearson[i - 1][j - 1]);
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
        while (start < names.length + 1) {
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
