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

package rapaio.printer.format;

import rapaio.printer.Printable;
import rapaio.sys.WS;
import rapaio.util.Pair;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> at 12/16/14.
 */
public class TextTable implements Printable {

    public static TextTable newEmpty(int rows, int cols) {
        return new TextTable(rows, cols);
    }

    private final int rows;
    private final int cols;
    private final String[][] values;
    private final int[][] mergeCols;
    private final int[][] alignCells;

    private int headerRows = 0;
    private int headerCols = 0;

    private int hSplitSize = -1;
    private int hMergeSize = -1;

    private TextTable(int rows, int cols) {
        this.rows = rows;
        this.cols = cols;
        values = new String[rows][cols];
        mergeCols = new int[rows][cols];
        alignCells = new int[rows][cols];
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                values[i][j] = "";
                mergeCols[i][j] = 1;
                alignCells[i][j] = -1;
            }
        }
    }

    public int rows() {
        return rows;
    }

    public int cols() {
        return cols;
    }

    public TextTable withSplit() {
        return withSplit(0);
    }

    public TextTable withSplit(int width) {
        this.hSplitSize = width == 0 ? WS.getPrinter().getTextWidth() : width;
        return this;
    }

    public TextTable withMerge() {
        return withMerge(0);
    }

    public TextTable withMerge(int width) {
        this.hMergeSize = width == 0 ? WS.getPrinter().getTextWidth() : width;
        return this;
    }

    public TextTable withHeaderRows(int headerRows) {
        if (headerRows < 0)
            headerRows = 0;
        if (headerRows > rows) {
            throw new IllegalArgumentException("cannot set header rows greater than the number of rows");
        }
        this.headerRows = headerRows;
        return this;
    }

    public TextTable withHeaderCols(int headerCols) {
        if (headerCols < 0)
            headerCols = 0;
        if (headerCols > cols) {
            throw new IllegalArgumentException("cannot set header cols greater than the number of cols");
        }
        this.headerCols = headerCols;
        return this;
    }

    public String get(int row, int col) {
        return values[row][col];
    }

    public void set(int row, int col, String x, int align) {
        values[row][col] = x;
        alignCells[row][col] = align;
    }

    public void mergeCols(int row, int col, int size) {
        if (size < 1) {
            throw new IllegalArgumentException("cannot merge with size less than 1");
        }
        if (size + col - 1 >= cols) {
            throw new IllegalArgumentException("merge size goes outside boundaries");
        }
        mergeCols[row][col] = size;
    }

    @Override
    public String summary() {
        StringBuilder sb = new StringBuilder();
        if (hSplitSize != -1 && hMergeSize != -1) {
            throw new IllegalArgumentException("Cannot set hSplitSize >= 0 and hMergeSize >= 0 in the same time");
        }
        if (hSplitSize >= 0) {
            summaryHSplit(sb);
        } else if (hMergeSize >= 0) {
            summaryHMerge(sb);
        } else {
            summarySame(sb);
        }
        return sb.toString();
    }

    private void summaryHSplit(StringBuilder sb) {

        int[] ws = computeLayout();
        boolean[] cannotSplit = new boolean[cols];

        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                for (int k = 1; k < mergeCols[i][j]; k++) {
                    cannotSplit[j + k - 1] = true;
                }
            }
        }

        List<List<Integer>> splits = new ArrayList<>();

        int s = 0;
        int c = headerCols;
        int w = 0;

        while (c < cols) {
            if (splits.size() < s + 1) {
                splits.add(new ArrayList<>());

                if (headerCols > 0) {
                    for (int i = 0; i < headerCols; i++) {
                        w += ws[i];
                        splits.get(s).add(i);
                    }
                }
            }

            int wNext = 0;
            int cc = c;
            wNext += ws[cc];
            cc++;
            while (cc < cols && cannotSplit[cc]) {
                wNext += ws[cc];
                cc++;
            }

            if (splits.get(s).isEmpty() || w + wNext <= hSplitSize) {
                for (int i = c; i < cc; i++) {
                    splits.get(s).add(i);
                    w += wNext;
                }
                c = cc;
            } else {
                w = 0;
                s++;
            }
        }

        int offset = 0;
        for (List<Integer> indexes : splits) {
            TextTable tt = new TextTable(rows, indexes.size());
            for (int j = 0; j < rows; j++) {
                for (int k = 0; k < indexes.size(); k++) {

                    tt.set(j, k, get(j, indexes.get(k)), alignCells[j][indexes.get(k)]);
                    tt.mergeCols(j, k, mergeCols[j][indexes.get(k)]);
                }
            }
            tt.summarySame(sb);
            offset += indexes.size();
            sb.append("\n");
        }
    }

    private void summaryHMerge(StringBuilder sb) {

        int[] ws = computeLayout();
        int total = Arrays.stream(ws).sum();
        int all = hMergeSize - total;
        int times = 1;
        while (all > total) {
            times++;
            all -= total;
        }
        if (times == 1) {
            summarySame(sb);
            return;
        }

        int contentRows = rows - headerRows;
        times = Math.min(contentRows, times);
        int maxContent = (int) Math.ceil(1.0 * contentRows / times);

        TextTable tt = TextTable.newEmpty(headerRows + maxContent, cols * times);
        tt.withHeaderRows(headerRows);

        int start = headerRows;
        for (int i = 0; i < times; i++) {

            // copy header

            for (int j = 0; j < headerRows; j++) {
                for (int k = 0; k < cols; k++) {
                    tt.set(j, i * cols + k, get(j, k), alignCells[j][k]);
                    tt.mergeCols(j, i * cols + k, mergeCols[j][k]);
                }
            }

            // copy content

            for (int j = 0; j < maxContent; j++) {
                for (int k = 0; k < cols; k++) {
                    if (start < rows) {
                        tt.set(j + headerRows, i * cols + k, get(start, k), alignCells[start][k]);
                        tt.mergeCols(j + headerRows, i * cols + k, mergeCols[start][k]);
                    } else {
                        break;
                    }
                }
                start++;
            }

        }

        tt.summarySame(sb);
    }

    private void summarySame(StringBuilder sb) {
        int[] ws = computeLayout();
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                if (mergeCols[i][j] > 1) {
                    int w = 0;
                    for (int k = j; k < j + mergeCols[i][j]; k++) {
                        w += ws[k];
                    }
                    sb.append(align(alignCells[i][j], w, values[i][j]));
                    j += (mergeCols[i][j] - 1);
                } else {
                    sb.append(" ").append(align(alignCells[i][j], ws[j] - 1, values[i][j]));
                }
            }
            sb.append("\n");
        }
    }

    private String align(int align, int width, String text) {
        if (align < 0) {
            if (text.length() < width) {
                return text + spaces(width - text.length());
            }
        } else if (align > 0) {
            if (text.length() < width) {
                return spaces(width - text.length()) + text;
            }
        } else {
            if (text.length() < width) {
                int half = (width - text.length()) / 2;
                return spaces(width - text.length() - half) + text + spaces(half);
            }
        }
        return text;
    }

    private String spaces(int n) {
        StringBuilder outputBuffer = new StringBuilder(n);
        for (int i = 0; i < n; i++) {
            outputBuffer.append(" ");
        }
        return outputBuffer.toString();
    }

    private int[] computeLayout() {
        int[] ws = new int[cols];
        int[] wm = new int[cols];
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                if (mergeCols[i][j] > 1) {
                    wm[j] = Math.max(values[i][j].length(), wm[j]);
                    j += (mergeCols[i][j] - 1);
                } else {
                    ws[j] = Math.max(values[i][j].length() + 1, ws[j]);
                }
            }
        }
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                if (mergeCols[i][j] > 1) {
                    int sum = 0;
                    for (int k = j + 1; k < j + mergeCols[i][j]; k++) {
                        sum += ws[k];
                    }
                    ws[j] = Math.max(wm[j] - sum, ws[j]);
                }
            }
        }
        return ws;
    }
}
