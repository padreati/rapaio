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

package rapaio.printer.format;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import rapaio.sys.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 11/5/18.
 */
public class TextTableRenderer {

    public static TextTableRenderer empty(int rows, int cols) {
        return new TextTableRenderer(rows, cols, 0, 0);
    }

    public static TextTableRenderer empty(int rows, int cols, int headerRows, int headerCols) {
        return new TextTableRenderer(rows, cols, headerRows, headerCols);
    }

    private static final char NO_ANCHOR = '\0';

    private final int rows;
    private final int cols;
    private final int headerRows;
    private final int headerCols;

    private final String[][] left;
    private final String[][] right;

    private final String[][] finalText;
    private final int[] finalLen;

    private boolean computedLayout = false;

    private TextTableRenderer(int rows, int cols, int headerRows, int headerCols) {

        this.rows = rows;
        this.cols = cols;
        this.headerRows = headerRows;
        this.headerCols = headerCols;

        left = new String[rows][cols];
        right = new String[rows][cols];

        finalText = new String[rows][cols];
        finalLen = new int[cols];

        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                left[i][j] = "";
                right[i][j] = "";
            }
        }
    }

    public String value(int row, int col) {
        return left[row][col] + right[row][col];
    }

    /**
     * This method is required to implement custom things:
     * <p>
     * * if left is null, right is not - text from right will be rights aligned
     * * if right is null, left is not - text from left will be left aligned
     * * if left and right are non nulls - left will be right aligned, right will be left aligned
     */
    public void set(int row, int col, String left, String right) {
        computedLayout = false;
        this.left[row][col] = left;
        this.right[row][col] = right;
    }

    public void set(int row, int col, String value, int align) {
        set(row, col, value, align, '\0');
    }

    public void set(int row, int col, String value, int align, char anchor) {
        computedLayout = false;
        if (anchor == NO_ANCHOR) {
            // no anchor
            if (align < 0) {
                left[row][col] = value;
                right[row][col] = null;
                return;
            }
            if (align > 0) {
                left[row][col] = null;
                right[row][col] = value;
                return;
            }
            int mid = (int) Math.ceil(value.length() / 2.0);
            left[row][col] = value.substring(0, mid);
            right[row][col] = value.substring(mid);
        } else {
            // anchor
            IntArrayList indexes = new IntArrayList();
            for (int i = 0; i < value.length(); i++) {
                if (value.charAt(i) == anchor) {
                    indexes.add(i);
                }
            }
            // by default if no anchor is found we take mid
            int mid = (int) Math.ceil(value.length() / 2.);
            if (!indexes.isEmpty()) {
                if (align < 0) {
                    mid = indexes.getInt(0);
                } else {
                    if (align > 0) {
                        mid = indexes.getInt(indexes.size() - 1);
                    } else {
                        mid = indexes.getInt(indexes.size() / 2);
                    }
                }
            }
            left[row][col] = value.substring(0, mid);
            right[row][col] = value.substring(mid);
        }
    }

    private void computeFinalTexts() {
        if (!computedLayout) {
            computedLayout = true;

            Arrays.fill(finalLen, -1);
            int[] len_left = Arrays.copyOf(finalLen, cols);
            int[] len_right = Arrays.copyOf(finalLen, cols);

            // fit global and to pieces lengths

            for (int i = 0; i < rows; i++) {
                for (int j = 0; j < cols; j++) {
                    if (left[i][j] == null) {
                        finalLen[j] = Math.max(finalLen[j], right[i][j].length());
                        continue;
                    }
                    if (right[i][j] == null) {
                        finalLen[j] = Math.max(finalLen[j], left[i][j].length());
                        continue;
                    }
                    len_left[j] = Math.max(len_left[j], left[i][j].length());
                    len_right[j] = Math.max(len_right[j], right[i][j].length());
                }
            }

            // adjust lengths global and from two pieces

            for (int i = 0; i < cols; i++) {
                if (len_left[i] + len_right[i] > finalLen[i]) {
                    finalLen[i] = len_left[i] + len_right[i];
                }
                if (len_left[i] + len_right[i] < finalLen[i]) {
                    len_left[i] += (finalLen[i] - len_left[i] - len_right[i]) / 2;
                    len_right[i] += finalLen[i] - len_left[i];
                }
            }

            // adjust text values

            for (int i = 0; i < rows; i++) {
                for (int j = 0; j < cols; j++) {
                    if (left[i][j] == null) {
                        finalText[i][j] = fillLeft(right[i][j], finalLen[j]);
                        continue;
                    }
                    if (right[i][j] == null) {
                        finalText[i][j] = fillRight(left[i][j], finalLen[j]);
                        continue;
                    }
                    finalText[i][j] = fillLeft(left[i][j], len_left[j]) + fillRight(right[i][j], len_right[j]);
                }
            }
        }
    }

    /**
     * Fill the string with spaces to the left until the given length
     */
    private String fillLeft(String value, int len) {
        int sz = value.length();
        if (sz >= len) {
            return value;
        }
        return spaces(len - sz) + value;
    }

    /**
     * Fill the string with spaces to the right until the given length
     */
    private String fillRight(String value, int len) {
        int sz = value.length();
        if (sz >= len) {
            return value;
        }
        return value + spaces(len - sz);
    }

    private String spaces(int len) {
        char[] bytes = new char[len];
        Arrays.fill(bytes, ' ');
        return String.valueOf(bytes);
    }

    public String getRawText() {
        return getText(-1);
    }

    public String getDefaultText() {
        return getText(WS.getPrinter().textWidth());
    }

    public String getText(int consoleWidth) {
        computeFinalTexts();
        int totalColLen = Arrays.stream(finalLen).sum();
        if (consoleWidth == -1 || consoleWidth == totalColLen) {
            return computeRawText();
        }
        if (totalColLen > consoleWidth) {
            return computeSplitText(consoleWidth);
        }
        return computeMergeText(consoleWidth);
    }

    private String computeRawText() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                sb.append(finalText[i][j]).append(' ');
            }
            sb.append('\n');
        }
        return sb.toString();
    }

    private String computeSplitText(int consoleWidth) {
        int headerColLen = Arrays.stream(finalLen).limit(headerCols).sum();

        StringBuilder sb = new StringBuilder();

        int lastCol = headerCols;
        while (lastCol < cols) {
            List<Integer> selectedColumns = new ArrayList<>();
            int currentLen = 0;
            for (int i = 0; i < headerCols; i++) {
                currentLen += finalLen[i];
                selectedColumns.add(i);
            }
            selectedColumns.add(lastCol);
            currentLen += finalLen[lastCol];
            lastCol++;
            while (lastCol < cols && currentLen <= consoleWidth) {
                if (currentLen + finalLen[lastCol] <= consoleWidth) {
                    selectedColumns.add(lastCol);
                    currentLen += finalLen[lastCol];
                    lastCol++;
                }
                break;
            }

            // draw selected columns
            for (int i = 0; i < rows; i++) {
                for (int col : selectedColumns) {
                    sb.append(finalText[i][col]).append(' ');
                }
                sb.append('\n');
            }
            sb.append('\n');
        }
        return sb.toString();
    }

    private String computeMergeText(int consoleWidth) {
        StringBuilder sb = new StringBuilder();

        int totoalColLen = Arrays.stream(finalLen).sum();
        int nonHeaderRows = rows - headerRows;

        int sets = 1;
        int currentLen = totoalColLen;

        while (true) {
            int newSets = sets + 1;
            int newCurrentLen = currentLen + totoalColLen;
            int newNonHeaderRows = (int) Math.ceil(((double) nonHeaderRows) / newSets);
            if (newNonHeaderRows > newSets && newCurrentLen <= consoleWidth) {
                sets = newSets;
                currentLen = newCurrentLen;
                nonHeaderRows = newNonHeaderRows;
                continue;
            }
            break;
        }

        String[] rows = new String[headerRows + nonHeaderRows];
        for (int i = 0; i < rows.length; i++) {
            rows[i] = "";
        }

        for (int s = 0; s < sets; s++) {
            for (int i = 0; i < headerRows; i++) {
                for (int j = 0; j < cols; j++) {
                    rows[i] += finalText[i][j] + ' ';
                }
            }
            for (int i = 0; i < nonHeaderRows; i++) {
                for (int j = 0; j < cols; j++) {
                    rows[i + headerRows] += finalText[s * nonHeaderRows + i + headerRows][j] + ' ';
                }
            }
        }
        for (String line : rows) {
            sb.append(line).append('\n');
        }

        return sb.toString();
    }
}
