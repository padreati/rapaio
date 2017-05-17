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

package rapaio.printer.format;

import rapaio.printer.Printable;
import rapaio.sys.WS;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> at 12/16/14.
 */
public class TextTable implements Printable {

    private final int rows;
    private final int cols;
    private final String[][] values;
    private final int[][] mergeSizes;
    private final int[][] alignValues;
    private int headerRows = 0;
    private int headerCols = 0;
    private int hSplitSize = -1;
    private int hMergeSize = -1;

    private TextTable(int rows, int cols) {
        this.rows = rows;
        this.cols = cols;
        values = new String[rows][cols];
        mergeSizes = new int[rows][cols];
        alignValues = new int[rows][cols];
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                values[i][j] = "";
                mergeSizes[i][j] = 1;
                alignValues[i][j] = -1;
            }
        }
    }

    public static TextTable newEmpty(int rows, int cols) {
        return new TextTable(rows, cols);
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
        this.hSplitSize = (width == 0) ? WS.getPrinter().textWidth() : width;
        return this;
    }

    public TextTable withMerge() {
        return withMerge(0);
    }

    public TextTable withMerge(int width) {
        this.hMergeSize = width == 0 ? WS.getPrinter().textWidth() : width;
        return this;
    }

    public TextTable withHeaderRows(int headerRows) {
        if (isLeftAlign(headerRows))
            headerRows = 0;
        if (headerRows > rows) {
            throw new IllegalArgumentException("cannot set header rows greater than the number of rows");
        }
        this.headerRows = headerRows;
        return this;
    }

    public TextTable withHeaderCols(int headerCols) {
        if (isLeftAlign(headerCols))
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
        alignValues[row][col] = align;
    }

    public void mergeCols(int row, int col, int size) {
        if (size < 1) {
            throw new IllegalArgumentException("cannot merge with size less than 1");
        }
        if (size + col - 1 >= cols) {
            throw new IllegalArgumentException("merge size goes outside boundaries");
        }
        mergeSizes[row][col] = size;
    }

    @Override
    public String getSummary() {
        StringBuilder stringBuilder = new StringBuilder();
        if (hSplitSize != -1 && hMergeSize != -1) {
            throw new IllegalArgumentException("Cannot set hSplitSize >= 0 and hMergeSize >= 0 in the same time");
        }
        if (hSplitSize >= 0) {
            summaryHSplit(stringBuilder);
        } else if (hMergeSize >= 0) {
            summaryHMerge(stringBuilder);
        } else {
            writeToStirngBuilder(stringBuilder);
        }
        return stringBuilder.toString();
    }

    private void summaryHSplit(StringBuilder stringBuilder) {
        int[] maxLengths = computeLayout();
        boolean[] cannotSplit = new boolean[cols];

        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < cols; col++) {
                for (int count = 1; count < mergeSizes[row][col]; count++) {
                    cannotSplit[col + count - 1] = true;
                }
            }
        }

        List<List<Integer>> splitColsList = getSplitColsList(maxLengths, cannotSplit);

        for (List<Integer> indexes : splitColsList) {
            TextTable textTable = createSplitTextTable(indexes);
            
            textTable.writeToStirngBuilder(stringBuilder);
            
            stringBuilder.append("\n");
        }
    }

    private List<List<Integer>> getSplitColsList(int[] maxLengths, boolean[] cannotSplit) {
        List<List<Integer>> splitColsList = new ArrayList<>();

        int splitCount = 0;
        int splitCurrentCol = headerCols;
        int sumOfSplitLength = 0;
        while (splitCurrentCol < cols) {
            if (splitColsList.size() < splitCount + 1) {
                splitColsList.add(new ArrayList<>());

                if (headerCols > 0) {
                    for (int i = 0; i < headerCols; i++) {
                        sumOfSplitLength += maxLengths[i];
                        splitColsList.get(splitCount).add(i);
                    }
                }
            }

            int splitSize = maxLengths[splitCurrentCol];
            int splitEndCol = splitCurrentCol+1;
            while (splitEndCol < cols && cannotSplit[splitEndCol]) {
                splitSize += maxLengths[splitEndCol];
                splitEndCol++;
            }

            if (splitColsList.get(splitCount).isEmpty() || sumOfSplitLength + splitSize <= hSplitSize) {
                for (int i = splitCurrentCol; i < splitEndCol; i++) {
                    splitColsList.get(splitCount).add(i);
                    sumOfSplitLength += splitSize;
                }
                splitCurrentCol = splitEndCol;
            } else {
                sumOfSplitLength = 0;
                splitCount++;
            }
        }
        return splitColsList;
    }

    private TextTable createSplitTextTable(List<Integer> indexes) {
        TextTable textTable = new TextTable(rows, indexes.size());
        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < indexes.size(); col++) {
                textTable.set(row, col, get(row, indexes.get(col)), alignValues[row][indexes.get(col)]);
                textTable.mergeCols(row, col, mergeSizes[row][indexes.get(col)]);
            }
        }
        return textTable;
    }

    private void summaryHMerge(StringBuilder stringBuilder) {
        int count = calculateCount();

        if (count == 1) {
            writeToStirngBuilder(stringBuilder);
        } else {
            int maxContent = getMaxContent(count);

            TextTable textTable = createMergeTextTable(count, maxContent);

            textTable.writeToStirngBuilder(stringBuilder);
        }
    }

    private int getMaxContent(int count) {
        int contentRows = rows - headerRows;
        count = Math.min(contentRows, count);
        return (int) Math.ceil(1.0 * contentRows / count);
    }

    private int calculateCount() {
        int time = 1;
        int[] maxLengths = computeLayout();
        int sumOfLength = Arrays.stream(maxLengths).sum();
        int remainSize = hMergeSize - sumOfLength;
        while (remainSize > sumOfLength) {
            time++;
            remainSize -= sumOfLength;
        }
        return time;
    }

    private TextTable createMergeTextTable(int count, int maxContent) {
        TextTable textTable = TextTable.newEmpty(headerRows + maxContent, cols * count);
        textTable.withHeaderRows(headerRows);

        for (int currentCount = 0, nextRow = headerRows; currentCount < count; currentCount++) {
            copyHeader(textTable, currentCount);

            for (int content = 0; content < maxContent; content++) {
                for (int col = 0; col < cols; col++) {
                    if (nextRow < rows) {
                        textTable.set(content + headerRows, currentCount * cols + col, get(nextRow, col),
                                alignValues[nextRow][col]);
                        textTable.mergeCols(content + headerRows, currentCount * cols + col, mergeSizes[nextRow][col]);
                    } else {
                        break;
                    }
                }
                nextRow++;
            }
        }
        return textTable;
    }

    private void copyHeader(TextTable textTable, int time) {
        for (int j = 0; j < headerRows; j++) {
            for (int k = 0; k < cols; k++) {
                textTable.set(j, time * cols + k, get(j, k), alignValues[j][k]);
                textTable.mergeCols(j, time * cols + k, mergeSizes[j][k]);
            }
        }
    }

    private void writeToStirngBuilder(StringBuilder stringBuilder) {
        int[] maxLengths = computeLayout();
        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < cols; col++) {
                if (mergeSizes[row][col] > 1) {
                    int totalLength = 0;
                    for (int count = 0; count < mergeSizes[row][col]; count++) {
                        totalLength += maxLengths[col + count];
                    }
                    stringBuilder.append(align(alignValues[row][col], totalLength, values[row][col]));
                    col += mergeSizes[row][col] - 1;
                } else {
                    stringBuilder.append(" ")
                            .append(align(alignValues[row][col], maxLengths[col] - 1, values[row][col]));
                }
            }
            stringBuilder.append("\n");
        }
    }

    private String align(int align, int width, String text) {
        if (text.length() < width) {
            if (isLeftAlign(align)) {
                return text + addSpaces(width - text.length());
            } else if (isRightAlign(align)) {
                return addSpaces(width - text.length()) + text;
            } else {
                int halfSpace = (width - text.length()) / 2;
                return addSpaces(width - text.length() - halfSpace) + text + addSpaces(halfSpace);
            }
        }
        return text;
    }

    private boolean isRightAlign(int align) {
        return align > 0;
    }

    private boolean isLeftAlign(int align) {
        return align < 0;
    }

    private String addSpaces(int numberOfSpace) {
        StringBuilder outputBuffer = new StringBuilder(numberOfSpace);
        for (int i = 0; i < numberOfSpace; i++) {
            outputBuffer.append(" ");
        }
        return outputBuffer.toString();
    }

    private int[] computeLayout() {
        int[] mergeMaxValueLength = new int[cols];
        int[] maxValueLength = new int[cols];
        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < cols; col++) {
                int valueLength = values[row][col].length();
                if (mergeSizes[row][col] > 1) {
                    maxValueLength[col] = Math.max(valueLength, maxValueLength[col]);
                    col += (mergeSizes[row][col] - 1);
                } else {
                    mergeMaxValueLength[col] = Math.max(valueLength + 1, mergeMaxValueLength[col]);
                }
            }
        }
        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < cols; col++) {
                if (mergeSizes[row][col] > 1) {
                    int spaceCount = 0;
                    for (int count = 1; count < mergeSizes[row][col]; count++) {
                        spaceCount += mergeMaxValueLength[col + count];
                    }
                    mergeMaxValueLength[col] = Math.max(maxValueLength[col] - spaceCount, mergeMaxValueLength[col]);
                }
            }
        }
        return mergeMaxValueLength;
    }
}
