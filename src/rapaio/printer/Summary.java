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

package rapaio.printer;

import rapaio.core.stat.Quantiles;
import rapaio.data.Frame;
import rapaio.data.Var;
import rapaio.data.VarType;
import rapaio.printer.format.TextTable;
import rapaio.sys.WS;

import static rapaio.sys.WS.code;
import static rapaio.sys.WS.getPrinter;

/**
 * TODO: this class should not exist anymore, the code should be placed at each proper class
 *
 * @author tutuianu
 */
public class Summary {
    private static TypeStrategy typeStrategy;

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

        TextTable tt = TextTable.newEmpty(df.varCount(), 5);
        for (int i = 0; i < df.varCount(); i++) {
            tt.set(i, 0, i + ".", 1);
            tt.set(i, 1, df.var(i).name(), 1);
            tt.set(i, 2, ":", -1);
            tt.set(i, 3, df.var(i).type().code(), -1);
            tt.set(i, 4, "|", 1);
        }
        tt.withMerge();
        buffer.append("\n").append(tt.summary()).append("\n");


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

            Var v = df.var(i);
            
            if (v.type() == VarType.BINARY) {
                typeStrategy = new BinaryTypeStrategy();
                typeStrategy.getVarSummary(df, v, first, second, k);
            }

            if (v.type() == VarType.INDEX || v.type() == VarType.NUMERIC) {
                typeStrategy = new NumbericTypeStrategy();
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

        if (v.type() == VarType.BINARY) {
             typeStrategy = new BinaryTypeStrategy();
             typeStrategy.getPrintSummary(v, first, second);;
        }

        if (v.type() == VarType.INDEX || v.type() == VarType.NUMERIC) {
            typeStrategy = new NumbericTypeStrategy();
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

    public static void printNames(Frame df) {
        StringBuilder buffer = new StringBuilder();
        buffer.append("\n > names(frame)\n");
        for (int i = 0; i < df.varCount(); i++) {
            buffer.append(df.varNames()[i]).append("\n");
        }
        code(buffer.toString());
    }

    @Deprecated
    public static void printSummary(Printable result) {
        result.printSummary();
    }

    @Deprecated
    public static void lines(boolean merge, Var v) {
        head(merge, v.rowCount(), new Var[]{v}, new String[]{""});
    }

    @Deprecated
    public static void head(boolean merge, int lines, Var v) {
        head(merge, lines, new Var[]{v}, new String[]{""});
    }

    @Deprecated
    public static void lines(Frame df) {
        lines(true, df);
    }

    @Deprecated
    public static void lines(boolean merge, Frame df) {
        Var[] vars = new Var[df.varCount()];
        String[] names = df.varNames();
        for (int i = 0; i < vars.length; i++) {
            vars[i] = df.var(i);
        }
        head(merge, df.rowCount(), vars, names);
    }

    public static void head(boolean merge, int lines, Frame df) {
        Var[] vars = new Var[df.varCount()];
        String[] names = df.varNames();
        for (int i = 0; i < vars.length; i++) {
            vars[i] = df.var(i);
        }
        head(merge, Math.min(lines, df.rowCount()), vars, names);
    }

    public static void head(boolean merge, int lines, Var[] vars, String[] names) {
        WS.code(headString(merge, lines, vars, names));
    }

    public static String headString(Frame df) {
        return headString(true, df.rowCount(), df.varStream().toArray(Var[]::new), df.varNames());
    }

    public static String headString(boolean merge, Frame df) {
        return headString(merge, df.rowCount(), df.varStream().toArray(Var[]::new), df.varNames());
    }

    public static String headString(int lines, Var[] vars, String[] names) {
        return headString(true, lines, vars, names);
    }

    public static String headString(boolean merge, int lines, Var[] vars, String[] names) {
        if (lines == -1) {
            lines = vars[0].rowCount();
        }

        TextTable tt = TextTable.newEmpty(lines + 1, vars.length + 1);
        if (merge)
            tt.withMerge(getPrinter().textWidth());
        tt.withHeaderRows(1);
        tt.withHeaderCols(1);

        for (int i = 0; i < vars.length; i++) {
            tt.set(0, i + 1, names[i], 0);
        }
        for (int i = 0; i < lines; i++) {
            tt.set(i + 1, 0, "[" + i + "]", 1);
        }
        for (int i = 0; i < lines; i++) {
            for (int j = 0; j < vars.length; j++) {
                tt.set(i + 1, j + 1, vars[j].label(i), 1);
            }
        }
        return tt.summary();
    }

    public static String getHorizontalSummary5(Var var) {
        TextTable tt1 = TextTable.newEmpty(2, 5).withHeaderRows(1);

        String[] headers1 = new String[] {"Min", "1Q", "Median", "3Q","Max"};
        double[] values1 = Quantiles.from(var, 0, 0.25, 0.5, 0.75, 1).values();
        for (int i = 0; i < 5; i++) {
            tt1.set(0, i, headers1[i], 1);
            tt1.set(1, i, WS.formatFlex(values1[i]), -1);
        }
        return tt1.summary();
    }
}
