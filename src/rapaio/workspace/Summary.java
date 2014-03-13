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

package rapaio.workspace;

import rapaio.core.Summarizable;
import rapaio.core.stat.Mean;
import rapaio.core.stat.Quantiles;
import rapaio.data.Frame;
import rapaio.data.Nominal;
import rapaio.data.Vector;

import java.util.Arrays;
import java.util.HashSet;

import static rapaio.workspace.Workspace.code;
import static rapaio.workspace.Workspace.getPrinter;

/**
 * @author tutuianu
 */
public class Summary {

    public static void summary(Frame df) {
        summary(df, df.colNames());
    }

    public static void summary(Frame df, String... names) {

        StringBuilder buffer = new StringBuilder();
        buffer.append(String.format(">>summary(frame, %s)\n", Arrays.deepToString(names)));
        if (df == null) {
            buffer.append("null instance of frame.\n");
            code(buffer.toString());
            return;
        }
        buffer.append(String.format("rowCount: %d, colCount: %d%n", df.rowCount(), df.colCount()));

        String[][] first = new String[names.length][7];
        String[][] second = new String[names.length][7];
        for (int i = 0; i < 7; i++) {
            for (int j = 0; j < names.length; j++) {
                first[j][i] = " ";
                second[j][i] = " ";
            }
        }

        for (int k = 0; k < names.length; k++) {
            int i = df.colIndex(names[k]);

            Vector v = df.col(i);
            if (v.type().isNumeric()) {
                double[] p = new double[]{0., 0.25, 0.50, 0.75, 1.00};
                double[] perc = new Quantiles(v, p).getValues();
                double mean = new Mean(v).getValue();

                int nas = 0;
                for (int j = 0; j < df.rowCount(); j++) {
                    if (v.missing(j)) {
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

            if (v.type().isNominal()) {
                int[] hits = new int[v.dictionary().length];
                int[] indexes = new int[v.dictionary().length];
                for (int j = 0; j < df.rowCount(); j++) {
                    hits[v.index(j)]++;
                    indexes[v.index(j)] = j;
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
                for (int j = 0; j < df.rowCount(); j++) {
                    if (v.missing(j)) {
                        nas++;
                    }
                }

                int other = df.rowCount();
                int pos = 0;
                for (int j = 0; j < 6; j++) {
                    if (tophit[j] != 0) {
                        other -= tophit[j];
                        first[k][j] = v.label(indexes[topindex[j]]);
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

        int witdh = getPrinter().getTextWidth();

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
        code(buffer.toString());
    }

    public static void summary(Vector v) {

        StringBuilder buffer = new StringBuilder();
        buffer.append(">>summary(vector)\n");

        String[] first = new String[7];
        String[] second = new String[7];
        for (int i = 0; i < 7; i++) {
            first[i] = " ";
            second[i] = " ";
        }

        if (v.type().isNumeric()) {
            double[] p = new double[]{0., 0.25, 0.50, 0.75, 1.00};
            double[] perc = new Quantiles(v, p).getValues();
            double mean = new Mean(v).getValue();

            int nas = 0;
            for (int j = 0; j < v.rowCount(); j++) {
                if (v.missing(j)) {
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

        if (v.type().isNominal()) {
            int[] hits = new int[v.rowCount() + 1];
            int[] indexes = new int[v.rowCount() + 1];
            for (int j = 0; j < v.rowCount(); j++) {
                hits[v.index(j)]++;
                indexes[v.index(j)] = j;
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
            for (int j = 0; j < v.rowCount(); j++) {
                if (v.missing(j)) {
                    nas++;
                }
            }

            int other = v.rowCount();
            int pos = 0;
            for (int j = 0; j < 6; j++) {
                if (tophit[j] != 0) {
                    other -= tophit[j];
                    first[j] = v.label(indexes[topindex[j]]);
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
            StringBuilder sb = new StringBuilder();
            sb.append(String.format("%" + wfirst + "s", first[j]));
            if (" ".equals(first[j]) && " ".equals(second[j])) {
                sb.append("   ");
            } else {
                sb.append(" : ");
            }
            sb.append(String.format("%" + wsecond + "s", second[j]));
            sb.append("\n");
            buffer.append(sb.toString());
        }

        code(buffer.toString());
    }

    public static void names(Frame df) {
        StringBuilder buffer = new StringBuilder();
        buffer.append(String.format(">>names(frame)\n"));
        for (int i = 0; i < df.colCount(); i++) {
            buffer.append(df.colNames()[i]).append("\n");
        }
        code(buffer.toString());
    }

    public static void contingencyTable(Vector a, Vector b) {
        Vector[] vectors = new Vector[b.rowCount() + 1];

        // learn first column
        HashSet<String> labels = new HashSet<>();
        for (int i = 0; i < a.rowCount(); i++) {
            labels.add(a.label(i));
        }
        labels.add("Totals");
        vectors[0] = new Nominal(a.rowCount() + 1, labels);
        for (int i = 0; i < a.dictionary().length; i++) {
            vectors[0].setLabel(i, a.dictionary()[i]);
        }
        vectors[0].setLabel(a.rowCount(), "Totals");

        // learn numerical columns
    }

    public static void summary(Summarizable result) {
        result.summary();
    }

    public static void lines(Vector v) {
        head(v.rowCount(), new Vector[]{v}, new String[]{""});
    }

    public static void head(int lines, Vector v) {
        head(lines, new Vector[]{v}, new String[]{""});
    }

    public static void lines(Frame df) {
        Vector[] vectors = new Vector[df.colCount()];
        String[] names = df.colNames();
        for (int i = 0; i < vectors.length; i++) {
            vectors[i] = df.col(i);
        }
        head(df.rowCount(), vectors, names);
    }

    public static void head(int lines, Frame df) {
        Vector[] vectors = new Vector[df.colCount()];
        String[] names = df.colNames();
        for (int i = 0; i < vectors.length; i++) {
            vectors[i] = df.col(i);
        }
        head(lines, vectors, names);
    }

    public static void head(int lines, Vector[] vectors, String[] names) {
        if (lines == -1) {
            lines = vectors[0].rowCount();
        }

        int[] max = new int[vectors.length];
        for (int i = 0; i < vectors.length; i++) {
            max[i] = names[i].length() + 1;
            for (int j = 0; j < vectors[i].rowCount(); j++) {
                if (vectors[i].type().isNominal() && max[i] < vectors[i].label(j).length()) {
                    max[i] = vectors[i].label(j).length();
                }
                if (vectors[i].type().isNumeric()) {
                    String value = String.format("%s", String.format("%.10f", vectors[i].value(j)));
                    if (max[i] < value.length()) {
                        max[i] = value.length();
                    }
                }
            }
        }

        StringBuilder sb = new StringBuilder();

        sb.append("\n");

        int pos = 0;
        while (pos < vectors.length) {
            int maxWidth = getPrinter().getTextWidth();
            int width = 0;
            int start = pos;
            while ((pos < vectors.length - 1) && (width + max[pos + 1] + 1 < maxWidth)) {
                width += max[pos + 1] + 1;
                pos++;
            }

            for (int j = start; j <= pos; j++) {
                String value = String.format("%" + max[j] + "s", names[j]);
                sb.append(value).append(" ");
            }
            sb.append("\n");

            for (int i = 0; i < lines; i++) {
                for (int j = start; j <= pos; j++) {
                    String value;
                    if (vectors[j].type().isNominal()) {
                        value = String.format("%" + max[j] + "s", vectors[j].label(i));
                    } else {
                        value = String.format("%" + max[j] + "s", String.format("%.10f", vectors[j].value(i)));
                    }
                    sb.append(value).append(" ");
                }
                sb.append("\n");
            }
            pos++;
            sb.append("\n");
        }

        code(sb.toString());
    }
}
