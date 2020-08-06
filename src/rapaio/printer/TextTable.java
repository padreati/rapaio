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

import rapaio.data.Frame;
import rapaio.data.VType;
import rapaio.data.Var;
import rapaio.data.VarInt;
import rapaio.printer.opt.POption;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Helper tool to build text in tabular format.
 * <p>
 * A text table has rows an columns. First rows and columns could be header columns. Header rows or columns are like
 * regular rows or columns with the observation that they are replicated on splitting tables for printing.
 * <p>
 * Each cell can have an alignment which could be one of the following:
 * <ul>
 *     <li>left aligned with no anchor: text is left aligned</li>
 *     <li>right aligned with no anchor: text is right aligned</li>
 *     <li>center aligned with no anchor: text is centered</li>
 *     <li>anchored: text is aligned to the first occurrence of the anchor character, if the anchor character
 *     is not found then text is centered</li>
 * </ul>
 * <p>
 * Text tables can be split, merged or raw output, depending on parameters at rendering.
 * <ul>
 *     <li>raw text does not consider the text width of the printer and outputs the table text
 *     as it is, in the tabular data format</li>
 *     <li>dynamic text eventually splits the table to accomodate the printer console text width, thus,
 *     if the total width of the columns are less than printer text width, than tries to split split rows
 *     in chunks and concatenate them at left, until no space is left, alternatively, if the total columns
 *     width are greater than printer text width, it will split table into column groups and display them
 *     sequentially, repeating headers</li>
 * </ul>
 * <p>
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 11/5/18.
 */
public class TextTable {

    /**
     * Builds a text table with given rows and columns and no headers
     *
     * @param rows number of rows
     * @param cols number of columns
     * @return text table instance
     */
    public static TextTable empty(int rows, int cols) {
        return new TextTable(rows, cols, 0, 0);
    }

    /**
     * Builds a text table with given number of rows and columns from which
     * some of rows and columns are headers.
     *
     * @param rows       number of rows
     * @param cols       number of columns
     * @param headerRows number of header rows from total number of rows
     * @param headerCols number of header columns from total number of columns
     * @return text table instance
     */
    public static TextTable empty(int rows, int cols, int headerRows, int headerCols) {
        return new TextTable(rows, cols, headerRows, headerCols);
    }

    private static final char NO_ANCHOR = '\0';

    private final int rows;
    private final int cols;
    private final int headerRows;
    private final int headerCols;

    private final String[][] center;
    private final String[][] left;
    private final String[][] right;

    private final String[][] finalText;
    private final int[] finalLen;

    private boolean computedLayout = false;

    private TextTable(int rows, int cols, int headerRows, int headerCols) {

        this.rows = rows;
        this.cols = cols;
        this.headerRows = headerRows;
        this.headerCols = headerCols;

        center = new String[rows][cols];
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

    public void textCenter(int r, int c, String x) {
        set(r, c, x, 0);
    }

    public void textLeft(int r, int c, String x) {
        set(r, c, x, null);
    }

    public void textRight(int r, int c, String x) {
        set(r, c, null, x);
    }

    public void floatString(int r, int c, String text) {
        if (text.indexOf('.') > -0) {
            set(r, c, text, 1, '.');
        } else {
            set(r, c, text, "");
        }
    }

    public void floatFlex(int r, int c, double x) {
        String text = Format.floatFlex(x);
        floatString(r, c, text);
    }

    public void floatFlexLong(int r, int c, double x) {
        String text = Format.floatFlexLong(x);
        if (text.indexOf('.') > -0) {
            set(r, c, text, 1, '.');
        } else {
            set(r, c, text, "");
        }
    }

    public void intRow(int r, int c, int row) {
        textRight(r, c, '[' + String.valueOf(row) + ']');
    }

    public void floatMedium(int r, int c, double x) {
        String text = Format.floatMedium(x);
        if (text.indexOf('.') > -0) {
            set(r, c, text, 1, '.');
        } else {
            set(r, c, text, "");
        }
    }

    public void pValue(int r, int c, double p) {
        String text = Format.pValue(p);
        int index = text.indexOf('.');
        if (index >= 0) {
            set(r, c, text, 1, '.');
        } else {
            boolean number = true;
            for (int i = 0; i < text.length(); i++) {
                if (!Character.isDigit(text.charAt(i))) {
                    number = false;
                }
            }
            if (number) {
                set(r, c, text, null);
            } else {
                set(r, c, null, text);
            }
        }
    }

    public void textType(int row, int col, Var var, int pos) {
        if (var.type() == VType.DOUBLE) {
            floatFlex(row, col, var.getDouble(pos));
        } else {
            textRight(row, col, var.getLabel(pos));
        }
    }

    public void textType(int row, int col, Frame df, int pos, String varName) {
        if (df.type(varName) == VType.DOUBLE) {
            floatFlex(row, col, df.getDouble(pos, varName));
        } else {
            textRight(row, col, df.getLabel(pos, varName));
        }
    }

    /**
     * This method is required to implement custom things:
     * <p>
     * * if left is null, right is not - text from right will be rights aligned
     * * if right is null, left is not - text from left will be left aligned
     * * if left and right are non nulls - left will be right aligned, right will be left aligned
     */
    void set(int row, int col, String left, String right) {
        computedLayout = false;
        this.left[row][col] = left;
        this.right[row][col] = right;
    }

    void set(int row, int col, String value, int align) {
        set(row, col, value, align, NO_ANCHOR);
    }

    void set(int row, int col, String value, int align, char anchor) {
        computedLayout = false;
        if (anchor == NO_ANCHOR) {
            // no anchor
            if (align < 0) {
                center[row][col] = null;
                left[row][col] = value;
                right[row][col] = null;
                return;
            }
            if (align > 0) {
                center[row][col] = null;
                left[row][col] = null;
                right[row][col] = value;
                return;
            }
            center[row][col] = value;
            left[row][col] = null;
            right[row][col] = null;
        } else {
            // anchor
            VarInt indexes = VarInt.empty();
            for (int i = 0; i < value.length(); i++) {
                if (value.charAt(i) == anchor) {
                    indexes.addInt(i);
                }
            }
            // by default if no anchor is found we take mid
            int mid = (int) Math.ceil(value.length() / 2.);
            if (indexes.rowCount() != 0) {
                if (align < 0) {
                    mid = indexes.getInt(0);
                } else {
                    if (align > 0) {
                        mid = indexes.getInt(indexes.rowCount() - 1);
                    } else {
                        mid = indexes.getInt(indexes.rowCount() / 2);
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

            Arrays.fill(finalLen, 0);
            int[] len_left = Arrays.copyOf(finalLen, cols);
            int[] len_right = Arrays.copyOf(finalLen, cols);

            // fit global and to pieces lengths

            for (int i = 0; i < rows; i++) {
                for (int j = 0; j < cols; j++) {
                    if (left[i][j] == null && right[i][j] == null) {
                        finalLen[j] = Math.max(finalLen[j], center[i][j].length());
                        continue;
                    }
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
                    int delta = finalLen[i] - len_left[i] - len_right[i];
                    len_left[i] += delta / 2;
                    len_right[i] += delta - (delta / 2);
                }
            }

            // adjust text values

            for (int i = 0; i < rows; i++) {
                for (int j = 0; j < cols; j++) {
                    if (left[i][j] == null && right[i][j] == null) {
                        finalText[i][j] = fillCenter(center[i][j], finalLen[j]);
                        continue;
                    }
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

    /**
     * Fill the string with spaces to the right until the given length
     */
    private String fillCenter(String value, int len) {
        int sz = value.length();
        if (sz >= len) {
            return value;
        }
        int delta = len - sz;
        return spaces(delta / 2) + value + spaces(delta - (delta / 2));
    }

    private String spaces(int len) {
        char[] bytes = new char[len];
        Arrays.fill(bytes, ' ');
        return String.valueOf(bytes);
    }

    public String getRawText() {
        return getText(-1);
    }

    public String getDynamicText(Printer printer, POption... options) {
        return getText(printer.getOptions().bind(options).textWidth());
    }

    public String getText(int consoleWidth) {
        computeFinalTexts();
        int totalColLen = Arrays.stream(finalLen).sum();
        if (consoleWidth == -1 || consoleWidth == totalColLen) {
            return computeRawText();
        }
        return totalColLen > consoleWidth ? computeSplitText(consoleWidth) : computeMergeText(consoleWidth);
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
                    continue;
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

        int totalColLen = Arrays.stream(finalLen).sum();
        int nonHeaderRows = rows - headerRows;

        int sets = 1;
        int currentLen = totalColLen;

        while (true) {
            int newSets = sets + 1;
            int newCurrentLen = currentLen + totalColLen;
            int newNonHeaderRows = (int) Math.ceil(((double) (rows - headerRows)) / newSets);
            if (newNonHeaderRows > newSets && newCurrentLen <= consoleWidth) {
                sets = newSets;
                currentLen = newCurrentLen;
                nonHeaderRows = newNonHeaderRows;
                continue;
            }
            break;
        }

        String[] rows = new String[headerRows + nonHeaderRows];
        Arrays.fill(rows, "");

        for (int s = 0; s < sets; s++) {
            for (int i = 0; i < headerRows; i++) {
                for (int j = 0; j < cols; j++) {
                    rows[i] += finalText[i][j] + ' ';
                }
            }
            for (int i = 0; i < nonHeaderRows; i++) {
                for (int j = 0; j < cols; j++) {
                    int r = s * nonHeaderRows + i + headerRows;
                    if (r >= this.rows) {
                        break;
                    }
                    rows[i + headerRows] += finalText[r][j] + ' ';
                }
            }
        }
        for (String line : rows) {
            sb.append(line).append('\n');
        }
        return sb.toString();
    }
}
