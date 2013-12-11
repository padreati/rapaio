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
import rapaio.data.NominalVector;
import rapaio.data.Vector;

import java.util.Arrays;
import java.util.HashSet;

import static rapaio.workspace.Workspace.*;

/**
 * @author tutuianu
 */
public class Summary {

    public static void summary(Frame df) {
        summary(df, df.getColNames());
    }

    public static void summary(Frame df, String... names) {
        StringBuilder buffer = new StringBuilder();
        buffer.append(String.format(">>summary(frame, %s)\n", Arrays.deepToString(names)));
        if (df == null) {
            buffer.append("null instance of frame.\n");
            code(buffer.toString());
            return;
        }
        buffer.append(String.format("rows: %d, cols: %d%n", df.getRowCount(), df.getColCount()));

        String[][] first = new String[names.length][7];
        String[][] second = new String[names.length][7];
        for (int i = 0; i < 7; i++) {
            for (int j = 0; j < names.length; j++) {
                first[j][i] = " ";
                second[j][i] = " ";
            }
        }

        for (int k = 0; k < names.length; k++) {
            int i = df.getColIndex(names[k]);

            Vector v = df.getCol(i);
            if (v.isNumeric()) {
                double[] p = new double[]{0., 0.25, 0.50, 0.75, 1.00};
                double[] perc = new Quantiles(v, p).getValues();
                double mean = new Mean(v).getValue();

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

            if (v.isNominal()) {
                int[] hits = new int[v.getDictionary().length];
                int[] indexes = new int[v.getDictionary().length];
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

        // build layout
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

        if (v.isNumeric()) {
            double[] p = new double[]{0., 0.25, 0.50, 0.75, 1.00};
            double[] perc = new Quantiles(v, p).getValues();
            double mean = new Mean(v).getValue();

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

        if (v.isNominal()) {
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

        // build layout
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
        for (int i = 0; i < df.getColCount(); i++) {
            buffer.append(df.getColNames()[i]).append("\n");
        }
        code(buffer.toString());
    }

    public static void contingencyTable(Vector a, Vector b) {
        Vector[] vectors = new Vector[b.getRowCount() + 1];

        // build first column
        HashSet<String> labels = new HashSet<>();
        for (int i = 0; i < a.getRowCount(); i++) {
            labels.add(a.getLabel(i));
        }
        labels.add("Totals");
        vectors[0] = new NominalVector(a.getRowCount() + 1, labels);
        for (int i = 0; i < a.getDictionary().length; i++) {
            vectors[0].setLabel(i, a.getDictionary()[i]);
        }
        vectors[0].setLabel(a.getRowCount(), "Totals");

        // build numerical columns
    }

    public static void summary(Summarizable result) {
        result.summary();
    }

    public static void head(int lines, Frame df) {
        Vector[] vectors = new Vector[df.getColCount()];
        for (int i = 0; i < vectors.length; i++) {
            vectors[i] = df.getCol(i);
        }
        head(lines, vectors);
    }

    public static void head(int lines, Vector... vectors) {
        if (lines == -1) {
            lines = vectors[0].getRowCount();
        }

        int[] max = new int[vectors.length];
        for (int i = 0; i < vectors.length; i++) {
            max[i] = ("V" + i).length() + 1;
            for (int j = 0; j < vectors[i].getRowCount(); j++) {
                if (vectors[i].isNominal() && max[i] < vectors[i].getLabel(j).length()) {
                    max[i] = vectors[i].getLabel(j).length();
                }
                if (vectors[i].isNumeric()) {
                    String value = String.format("%s", String.format("%.10f", vectors[i].getValue(j)));
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
                String value = String.format("%" + max[j] + "s", "V" + j);
                sb.append(value).append(" ");
            }
            sb.append("\n");

            for (int i = 0; i < lines; i++) {
                for (int j = start; j <= pos; j++) {
                    String value;
                    if (vectors[j].isNominal()) {
                        value = String.format("%" + max[j] + "s", vectors[j].getLabel(i));
                    } else {
                        value = String.format("%" + max[j] + "s", String.format("%.10f", vectors[j].getValue(i)));
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
