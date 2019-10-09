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
 *    Copyright 2018 Aurelian Tutuianu
 *    Copyright 2019 Aurelian Tutuianu
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

package rapaio.printer;

import rapaio.core.stat.*;
import rapaio.data.*;
import rapaio.printer.format.*;
import rapaio.printer.standard.*;

import static rapaio.sys.WS.*;

/**
 * TODO: this class should not exist anymore, the code should be placed at each proper class
 *
 * @author tutuianu
 */
public class Summary {

    private static TypeStrategy typeStrategy;

    private Summary() {
    }

    @Deprecated
    public static String getSummary(Frame df) {
        return getSummary(df, df.varNames());
    }

    @Deprecated
    public static String getSummary(Frame df, String... names) {

        StringBuilder buffer = new StringBuilder();

        buffer.append("Frame Summary\n");
        buffer.append("=============\n");

        if (df == null) {
            buffer.append("null instance of frame.\n");
            return buffer.toString();
        }

        buffer.append("* rowCount: ").append(df.rowCount()).append("\n");
        buffer.append("* complete: ").append(df.stream().complete().count()).append("/").append(df.rowCount()).append("\n");
        buffer.append("* varCount: ").append(df.varCount()).append("\n");
        buffer.append("* varNames: \n");

        TextTable tt = TextTable.empty(df.varCount(), 5);
        for (int i = 0; i < df.varCount(); i++) {
            tt.textRight(i, 0, i + ".");
            tt.textRight(i, 1, df.rvar(i).name());
            tt.textLeft(i, 2, ":");
            tt.textLeft(i, 3, df.rvar(i).type().code());
            tt.textRight(i, 4, "|");
        }
        buffer.append("\n").append(tt.getDefaultText()).append("\n");

        String[][] first = new String[names.length][7];
        String[][] second = new String[names.length][7];
        for (int i = 0; i < 7; i++) {
            for (int j = 0; j < names.length; j++) {
                first[j][i] = " ";
                second[j][i] = " ";
            }
        }

        for (int k = 0; k < names.length; k++) {
            int i = df.varIndex(names[k]);

            Var v = df.rvar(i);

            if (v.type() == VType.BINARY) {
                typeStrategy = new BinaryTypeStrategy();
                typeStrategy.getVarSummary(df, v, first, second, k);
            }

            if (v.type().isNumeric()) {
                typeStrategy = new NumericTypeStrategy();
                typeStrategy.getVarSummary(df, v, first, second, k);
            }

            if (v.type().isNominal()) {
                typeStrategy = new NominalTypeStrategy();
                typeStrategy.getVarSummary(df, v, first, second, k);
            }
        }

        // learn layout
        int[] width = new int[names.length];
        int[] wfirst = new int[names.length];
        int[] wsecond = new int[names.length];
        for (int i = 0; i < names.length; i++) {
            width[i] = names[i].length();
        }
        for (int j = 0; j < 7; j++) {
            for (int i = 0; i < names.length; i++) {
                wfirst[i] = Math.max(wfirst[i], first[i][j].length());
                wsecond[i] = Math.max(wsecond[i], second[i][j].length());
            }
        }
        for (int i = 0; i < names.length; i++) {
            width[i] = Math.max(width[i], wfirst[i] + wsecond[i] + 3);
            wfirst[i] = width[i] - 3 - wsecond[i];
        }

        int witdh = getPrinter().textWidth();

        int pos = 0;

        while (pos < names.length) {
            int last = pos;
            int remain = witdh;
            while (true) {
                if (last < names.length && remain >= width[last]) {
                    remain -= width[last];
                    last++;
                    continue;
                }
                break;
            }
            if (last == pos) {
                last++;
            }

            // output text from pos to last
            StringBuilder sb = new StringBuilder();
            for (int i = pos; i < last; i++) {
                String colName = names[i];
                if (sb.length() != 0) {
                    sb.append(" ");
                }
                sb.append(String.format("%" + width[i] + "s ", colName));
            }
            buffer.append(sb.toString()).append("\n");
            for (int j = 0; j < 7; j++) {
                sb.append("\n");
                sb = new StringBuilder();
                for (int i = pos; i < last; i++) {
                    if (sb.length() != 0) {
                        sb.append(" ");
                    }
                    sb.append(String.format("%" + wfirst[i] + "s", first[i][j]));
                    if (" ".equals(first[i][j]) && " ".equals(second[i][j])) {
                        sb.append("   ");
                    } else {
                        sb.append(" : ");
                    }
                    sb.append(String.format("%" + wsecond[i] + "s", second[i][j]));
                    sb.append(" ");
                }
                buffer.append(sb.toString());
                if (last != names.length || j != 6) {
                    buffer.append("\n");
                }
            }

            pos = last;
        }
        buffer.append("\n");
        return buffer.toString();
    }

    @Deprecated
    public static String getSummary(Var v) {

        StringBuilder sb = new StringBuilder();
        sb.append("> printSummary(var: ").append(v.name()).append(")\n");
        sb.append("name: ").append(v.name()).append("\n");
        sb.append("type: ").append(v.type().name()).append("\n");
        int complete = (int) v.stream().complete().count();
        sb.append("rows: ").append(v.rowCount()).append(", complete: ").append(complete).append(", missing: ").append(v.rowCount() - complete).append("\n");

        String[] first = new String[7];
        String[] second = new String[7];
        for (int i = 0; i < 7; i++) {
            first[i] = " ";
            second[i] = " ";
        }

        if (v.type() == VType.BINARY) {
            typeStrategy = new BinaryTypeStrategy();
            typeStrategy.getPrintSummary(v, first, second);
        }

        if (v.type() == VType.INT || v.type() == VType.DOUBLE) {
            typeStrategy = new NumericTypeStrategy();
            typeStrategy.getPrintSummary(v, first, second);
        }

        if (v.type().isNominal()) {
            typeStrategy = new NominalTypeStrategy();
            typeStrategy.getPrintSummary(v, first, second);
        }

        // learn layout
        int wfirst = 0;
        int wsecond = 0;

        for (int j = 0; j < 7; j++) {
            wfirst = Math.max(wfirst, first[j].length());
            wsecond = Math.max(wsecond, second[j].length());
        }

        // output text from pos to last
        for (int j = 0; j < 7; j++) {
            StringBuilder sb2 = new StringBuilder();
            sb2.append(String.format("%" + wfirst + "s", first[j]));
            if (" ".equals(first[j]) && " ".equals(second[j])) {
                sb2.append("   ");
            } else {
                sb2.append(" : ");
            }
            sb2.append(String.format("%" + wsecond + "s", second[j]));
            sb2.append("\n");
            String next = sb2.toString();
            if (!next.trim().isEmpty())
                sb.append(next);
        }

        return sb.toString();
    }

    public static String getHorizontalSummary5(Var var) {
        TextTable tt1 = TextTable.empty(2, 5, 1, 0);

        String[] headers1 = new String[]{"Min", "1Q", "Median", "3Q", "Max"};
        double[] values1 = Quantiles.of(var, 0, 0.25, 0.5, 0.75, 1).values();
        for (int i = 0; i < 5; i++) {
            tt1.textRight(0, i, headers1[i]);
            tt1.floatFlex(1, i, values1[i]);
        }
        return tt1.getDefaultText();
    }
}
