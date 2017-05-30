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

package rapaio.printer;

import rapaio.core.stat.Mean;
import rapaio.core.stat.Quantiles;
import rapaio.data.Frame;
import rapaio.data.Var;
import rapaio.data.VarType;
import rapaio.printer.Printable;
import rapaio.printer.format.TextTable;
import rapaio.sys.WS;

import java.util.Arrays;

import static rapaio.sys.WS.code;
import static rapaio.sys.WS.getPrinter;

/**
 * TODO: this class should not exist anymore, the code should be placed at each proper class
 *
 * @author tutuianu
 */
@Deprecated
public class Summary {

    public static String getSummary(Frame df) {
        return getSummary(df, df.getVarNames());
    }

    public static String getSummary(Frame df, String... names) {

        StringBuilder buffer = new StringBuilder();

        buffer.append("Frame Summary\n");
        buffer.append("=============\n");

        if (df == null) {
            buffer.append("null instance of frame.\n");
            return buffer.toString();
        }

        buffer.append("* rowCount: ").append(df.getRowCount()).append("\n");
        buffer.append("* complete: ").append(df.stream().complete().count()).append("/").append(df.getRowCount()).append("\n");
        buffer.append("* varCount: ").append(df.getVarCount()).append("\n");
        buffer.append("* varNames: \n");

        TextTable tt = TextTable.newEmpty(df.getVarCount(), 5);
        for (int i = 0; i < df.getVarCount(); i++) {
            tt.set(i, 0, i + ".", 1);
            tt.set(i, 1, df.getVar(i).getName(), 1);
            tt.set(i, 2, ":", -1);
            tt.set(i, 3, df.getVar(i).getType().getCode(), -1);
            tt.set(i, 4, "|", 1);
        }
        tt.withMerge();
        buffer.append("\n").append(tt.getSummary()).append("\n");


        String[][] first = new String[names.length][7];
        String[][] second = new String[names.length][7];
        for (int i = 0; i < 7; i++) {
            for (int j = 0; j < names.length; j++) {
                first[j][i] = " ";
                second[j][i] = " ";
            }
        }

        for (int k = 0; k < names.length; k++) {
            int i = df.getVarIndex(names[k]);

            Var v = df.getVar(i);
            if (v.getType() == VarType.BINARY) {
                first[k][0] = "0";
                first[k][1] = "1";
                first[k][2] = "NA's";

                int ones = 0;
                int zeros = 0;
                int missing = 0;
                for (int j = 0; j < v.getRowCount(); j++) {
                    if (v.isMissing(j)) {
                        missing++;
                    } else {
                        if (v.getBinary(j))
                            ones++;
                        else
                            zeros++;
                    }
                }
                second[k][0] = String.valueOf(zeros);
                second[k][1] = String.valueOf(ones);
                second[k][2] = String.valueOf(missing);
                continue;
            }

            if (v.getType() == VarType.INDEX || v.getType() == VarType.NUMERIC) {
                double[] p = new double[]{0., 0.25, 0.50, 0.75, 1.00};
                double[] perc = Quantiles.from(v, p).getValues();
                double mean = Mean.from(v).getValue();

                int nas = 0;
                for (int j = 0; j < df.getRowCount(); j++) {
                    if (v.isMissing(j)) {
                        nas++;
                    }
                }

                first[k][0] = "Min.";
                first[k][1] = "1st Qu.";
                first[k][2] = "Median";
                first[k][3] = "Mean";
                first[k][4] = "2nd Qu.";
                first[k][5] = "Max.";

                second[k][0] = String.format("%.3f", perc[0]);
                second[k][1] = String.format("%.3f", perc[1]);
                second[k][2] = String.format("%.3f", perc[2]);
                second[k][3] = String.format("%.3f", mean);
                second[k][4] = String.format("%.3f", perc[3]);
                second[k][5] = String.format("%.3f", perc[4]);

                if (nas != 0) {
                    first[k][6] = "NA's";
                    second[k][6] = String.format("%d", nas);
                }
            }

            if (v.getType().isNominal()) {
                int[] hits = new int[v.getLevels().length];
                int[] indexes = new int[v.getLevels().length];
                for (int j = 0; j < df.getRowCount(); j++) {
                    hits[v.getIndex(j)]++;
                    indexes[v.getIndex(j)] = j;
                }
                int[] tophit = new int[6];
                int[] topindex = new int[6];
                for (int j = 1; j < hits.length; j++) {
                    if (hits[j] != 0) {
                        for (int l = 0; l < tophit.length; l++) {
                            if (tophit[l] < hits[j]) {
                                for (int m = tophit.length - 1; m > l; m--) {
                                    tophit[m] = tophit[m - 1];
                                    topindex[m] = topindex[m - 1];
                                }
                                tophit[l] = hits[j];
                                topindex[l] = j;
                                break;
                            }
                        }
                    }
                }
                int nas = 0;
                for (int j = 0; j < df.getRowCount(); j++) {
                    if (v.isMissing(j)) {
                        nas++;
                    }
                }

                int other = df.getRowCount();
                int pos = 0;
                for (int j = 0; j < 6; j++) {
                    if (tophit[j] != 0) {
                        other -= tophit[j];
                        first[k][j] = v.getLabel(indexes[topindex[j]]);
                        second[k][j] = String.valueOf(tophit[j]);
                        pos++;
                    }
                }
                if (nas != 0) {
                    if (other - nas != 0) {
                        if (pos == 6) {
                            pos--;
                        }
                        first[k][pos] = "(Other)";
                        second[k][pos] = String.valueOf(other - nas);
                        pos++;
                    }
                    first[k][pos] = "NA's";
                    second[k][pos] = String.valueOf(nas);
                } else {
                    if (other != 0) {
                        first[k][pos] = "(Other)";
                        second[k][pos] = String.valueOf(other);
                    }
                }
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

    public static String getSummary(Var v) {

        StringBuilder sb = new StringBuilder();
        sb.append("> printSummary(var: ").append(v.getName()).append(")\n");
        sb.append("name: ").append(v.getName()).append("\n");
        sb.append("type: ").append(v.getType().name()).append("\n");
        int complete = (int) v.stream().complete().count();
        sb.append("rows: ").append(v.getRowCount()).append(", complete: ").append(complete).append(", missing: ").append(v.getRowCount() - complete).append("\n");

        String[] first = new String[7];
        String[] second = new String[7];
        for (int i = 0; i < 7; i++) {
            first[i] = " ";
            second[i] = " ";
        }

        if (v.getType() == VarType.BINARY) {
            first[0] = "0";
            first[1] = "1";
            first[2] = "NA's";

            int ones = 0;
            int zeros = 0;
            int missing = 0;
            for (int i = 0; i < v.getRowCount(); i++) {
                if (v.isMissing(i)) {
                    missing++;
                } else {
                    if (v.getBinary(i))
                        ones++;
                    else
                        zeros++;
                }
            }
            second[0] = String.valueOf(zeros);
            second[1] = String.valueOf(ones);
            second[2] = String.valueOf(missing);
        }

        if (v.getType() == VarType.INDEX || v.getType() == VarType.NUMERIC) {
            double[] p = new double[]{0., 0.25, 0.50, 0.75, 1.00};
            double[] perc = Quantiles.from(v, p).getValues();
            double mean = Mean.from(v).getValue();

            int nas = 0;
            for (int j = 0; j < v.getRowCount(); j++) {
                if (v.isMissing(j)) {
                    nas++;
                }
            }

            first[0] = "Min.";
            first[1] = "1st Qu.";
            first[2] = "Median";
            first[3] = "Mean";
            first[4] = "2nd Qu.";
            first[5] = "Max.";

            second[0] = String.format("%.3f", perc[0]);
            second[1] = String.format("%.3f", perc[1]);
            second[2] = String.format("%.3f", perc[2]);
            second[3] = String.format("%.3f", mean);
            second[4] = String.format("%.3f", perc[3]);
            second[5] = String.format("%.3f", perc[4]);

            if (nas != 0) {
                first[6] = "NA's";
                second[6] = String.format("%d", nas);
            }
        }

        if (v.getType().isNominal()) {
            int[] hits = new int[v.getRowCount() + 1];
            int[] indexes = new int[v.getRowCount() + 1];
            for (int j = 0; j < v.getRowCount(); j++) {
                hits[v.getIndex(j)]++;
                indexes[v.getIndex(j)] = j;
            }
            int[] tophit = new int[6];
            int[] topindex = new int[6];
            for (int j = 1; j < hits.length; j++) {
                if (hits[j] != 0) {
                    for (int l = 0; l < tophit.length; l++) {
                        if (tophit[l] < hits[j]) {
                            for (int m = tophit.length - 1; m > l; m--) {
                                tophit[m] = tophit[m - 1];
                                topindex[m] = topindex[m - 1];
                            }
                            tophit[l] = hits[j];
                            topindex[l] = j;
                            break;
                        }
                    }
                }
            }
            int nas = 0;
            for (int j = 0; j < v.getRowCount(); j++) {
                if (v.isMissing(j)) {
                    nas++;
                }
            }

            int other = v.getRowCount();
            int pos = 0;
            for (int j = 0; j < 6; j++) {
                if (tophit[j] != 0) {
                    other -= tophit[j];
                    first[j] = v.getLabel(indexes[topindex[j]]);
                    second[j] = String.valueOf(tophit[j]);
                    pos++;
                }
            }
            if (nas != 0) {
                if (other - nas != 0) {
                    if (pos == 6) {
                        pos--;
                    }
                    first[pos] = "(Other)";
                    second[pos] = String.valueOf(other - nas);
                    pos++;
                }
                first[pos] = "NA's";
                second[pos] = String.valueOf(nas);
            } else {
                if (other != 0) {
                    first[pos] = "(Other)";
                    second[pos] = String.valueOf(other);
                }
            }
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
        for (int i = 0; i < df.getVarCount(); i++) {
            buffer.append(df.getVarNames()[i]).append("\n");
        }
        code(buffer.toString());
    }

    public static void printSummary(Printable result) {
        result.printSummary();
    }

    public static void lines(boolean merge, Var v) {
        head(merge, v.getRowCount(), new Var[]{v}, new String[]{""});
    }

    public static void head(boolean merge, int lines, Var v) {
        head(merge, lines, new Var[]{v}, new String[]{""});
    }

    public static void lines(Frame df) {
        lines(true, df);
    }

    public static void lines(boolean merge, Frame df) {
        Var[] vars = new Var[df.getVarCount()];
        String[] names = df.getVarNames();
        for (int i = 0; i < vars.length; i++) {
            vars[i] = df.getVar(i);
        }
        head(merge, df.getRowCount(), vars, names);
    }

    public static void head(boolean merge, int lines, Frame df) {
        Var[] vars = new Var[df.getVarCount()];
        String[] names = df.getVarNames();
        for (int i = 0; i < vars.length; i++) {
            vars[i] = df.getVar(i);
        }
        head(merge, Math.min(lines, df.getRowCount()), vars, names);
    }

    public static void head(boolean merge, int lines, Var[] vars, String[] names) {
        WS.code(headString(merge, lines, vars, names));
    }

    public static String headString(Frame df) {
        return headString(true, df.getRowCount(), df.varStream().toArray(Var[]::new), df.getVarNames());
    }

    public static String headString(boolean merge, Frame df) {
        return headString(merge, df.getRowCount(), df.varStream().toArray(Var[]::new), df.getVarNames());
    }

    public static String headString(int lines, Var[] vars, String[] names) {
        return headString(true, lines, vars, names);
    }

    public static String headString(boolean merge, int lines, Var[] vars, String[] names) {
        if (lines == -1) {
            lines = vars[0].getRowCount();
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
                tt.set(i + 1, j + 1, vars[j].getLabel(i), 1);
            }
        }
        return tt.getSummary();
    }
}
