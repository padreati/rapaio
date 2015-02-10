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

package rapaio.math.linear;

import rapaio.WS;
import rapaio.core.MathBase;
import rapaio.core.Printable;
import rapaio.math.linear.impl.*;
import rapaio.printer.Printer;

import java.io.Serializable;
import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> at 2/3/15.
 */
public interface M extends Serializable, Printable {

    /**
     * @return number of rows the matrix has
     */
    int rowCount();

    /**
     * @return number of columns the matrix has
     */
    int colCount();

    /**
     * Gets the value from a specified position
     *
     * @param i row number
     * @param j column number
     * @return value at given row and col
     */
    double get(int i, int j);

    /**
     * Sets the value from a given position in the matrix
     *
     * @param i     row number
     * @param j     column number
     * @param value new value
     */
    void set(int i, int j, double value);

    // methods

    /**
     * Builds a new matrix having only the specified rows
     *
     * @param indexes row indexes
     * @return new mapped matrix containing all columns and selected rows
     */
    default M mapRows(int... indexes) {
        return new MappedM(this, true, indexes);
    }

    default V mapRow(int index) {
        return new MappedRowV(this, index);
    }

    default M rangeRows(int start, int end) {
        int[] rows = new int[end - start];
        for (int i = start; i < end; i++) {
            rows[i - start] = i;
        }
        return new MappedM(this, true, rows);
    }

    /**
     * Builds a new matrix having all columns and all the rows not specified by given indexes
     *
     * @param indexes rows which will be removed
     * @return new mapped matrix containing all rows not specified by indexes
     */
    default M removeRows(int... indexes) {
        Set<Integer> rem = Arrays.stream(indexes).boxed().collect(Collectors.toSet());
        int[] rows = new int[rowCount() - rem.size()];
        int pos = 0;
        for (int i = 0; i < rowCount(); i++) {
            if (rem.contains(i))
                continue;
            rows[pos++] = i;
        }
        return new MappedM(this, true, rows);
    }

    default M mapCols(int... indexes) {
        return new MappedM(this, false, indexes);
    }

    default V mapCol(int index) {
        return new MappedColV(this, index);
    }

    default M rangeCols(int start, int end) {
        int[] cols = new int[end - start];
        for (int i = start; i < end; i++) {
            cols[i - start] = i;
        }
        return new MappedM(this, false, cols);
    }

    default M removeCols(int... indexes) {
        Set<Integer> rem = Arrays.stream(indexes).boxed().collect(Collectors.toSet());
        int[] cols = new int[colCount() - rem.size()];
        int pos = 0;
        for (int i = 0; i < colCount(); i++) {
            if (rem.contains(i))
                continue;
            cols[pos++] = i;
        }
        return new MappedM(this, false, cols);
    }

    default M solidCopy() {
        M m = new SolidM(rowCount(), colCount());
        for (int i = 0; i < rowCount(); i++) {
            for (int j = 0; j < colCount(); j++) {
                m.set(i, j, get(i, j));
            }
        }
        return m;
    }

    // operations

    default M t() {
        return new TransposeM(this);
    }

    default double det() {
        return new LUDecomposition(this, LUDecomposition.Method.GAUSSIAN_ELIMINATION).det();
    }

    default void increment(int i, int j, double value) {
        double old = get(i, j);
        set(i, j, old + value);
    }

    /**
     * Does not override equals since this is a costly
     * algorithm and can slow down processing as a side effect.
     *
     * @param m given matrix
     * @return true if dimension and elements are equal
     */
    default boolean isEqual(M m) {
        return isEqual(m, 1e-12);
    }

    default boolean isEqual(M m, double tol) {
        if (rowCount() != m.rowCount())
            return false;
        if (colCount() != m.colCount())
            return false;
        for (int i = 0; i < rowCount(); i++) {
            for (int j = 0; j < colCount(); j++) {
                if (!MathBase.eq(get(i, j), m.get(i, j), tol))
                    return false;
            }
        }
        return true;
    }

    // summary

    default void buildSummary(StringBuilder sb) {

        DecimalFormat f = Printer.formatDecShort;

        String[][] m = new String[rowCount()][colCount()];
        int max = 0;
        for (int i = 0; i < rowCount(); i++) {
            for (int j = 0; j < colCount(); j++) {
                m[i][j] = f.format(get(i, j));
                max = Math.max(max, m[i][j].length() + 1);
            }
        }
        max = Math.max(max, String.format("[,%d]", rowCount()).length());
        max = Math.max(max, String.format("[%d,]", colCount()).length());

        int hCount = (int) Math.floor(WS.getPrinter().getTextWidth() / (double) max);
        int vCount = Math.min(rowCount() + 1, 21);
        int hLast = 0;
        while (true) {

            // take vertical stripes
            if (hLast >= colCount())
                break;

            int hStart = hLast;
            int hEnd = Math.min(hLast + hCount, colCount());
            int vLast = 0;

            while (true) {

                // print rows
                if (vLast >= rowCount())
                    break;

                int vStart = vLast;
                int vEnd = Math.min(vLast + vCount, rowCount());

                for (int i = vStart; i <= vEnd; i++) {
                    for (int j = hStart; j <= hEnd; j++) {
                        if (i == vStart && j == hStart) {
                            sb.append(String.format("%" + (max + 1) + "s", ""));
                            continue;
                        }
                        if (i == vStart) {
                            sb.append(String.format("[ ,%" + (max - 4) + "d]", j - 1));
                            continue;
                        }
                        if (j == hStart) {
                            sb.append(String.format("[%" + (max - 4) + "d, ]", i - 1));
                            continue;
                        }
                        sb.append(String.format("%" + max + "s", m[i - 1][j - 1]));
                    }
                    sb.append("\n");
                }
                sb.append("\n");
                vLast = vEnd;
            }
            hLast = hEnd;
        }
    }

}
