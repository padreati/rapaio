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
import rapaio.core.Printable;
import rapaio.data.Var;
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

    // builders

    /**
     * Builds a new 0 filled matrix with given rows and cols
     *
     * @param rows number of rows
     * @param cols number of columns
     * @return new matrix object
     */
    static M newEmpty(int rows, int cols) {
        return new SolidM(rows, cols);
    }

    /**
     * Builds a new matrix with given rows and cols, fillen with given value
     *
     * @param rows number of rows
     * @param cols number of columns
     * @param fill initial value for all matrix cells
     * @return new matrix object
     */
    static M newFill(int rows, int cols, double fill) {
        if (fill == 0) {
            return newEmpty(rows, cols);
        }
        M m = new SolidM(rows, cols);
        for (int i = 0; i < m.rows(); i++) {
            for (int j = 0; j < m.cols(); j++) {
                m.set(i, j, fill);
            }
        }
        return m;
    }

    static M newVectorCopyOf(Var var) {
        M m = new SolidM(var.rowCount(), 1);
        for (int i = 0; i < var.rowCount(); i++) {
            m.set(i, var.value(i));
        }
        return m;
    }

    static M newEmptyVector(int rows) {
        return new SolidM(rows, 1);
    }

    // data

    /**
     * @return number of rows the matrix has
     */
    int rows();

    /**
     * @return number of columns the matrix has
     */
    int cols();

    /**
     * Gets the value from a specified position
     *
     * @param i row number
     * @param j column number
     * @return value at given row and col
     */
    double get(int i, int j);

    default double get(int i) {
        if (rows() == 1)
            return get(0, i);
        if (cols() == 1)
            return get(i, 0);
        throw new IllegalArgumentException("This shortcut method can be called only for vectors or special matrices");
    }

    /**
     * Sets the value from a given position in the matrix
     *
     * @param i     row number
     * @param j     column number
     * @param value new value
     */
    void set(int i, int j, double value);

    default void set(int i, double value) {
        if (rows() == 1) {
            set(0, i, value);
            return;
        }
        if (cols() == 1) {
            set(i, 0, value);
            return;
        }
        throw new IllegalArgumentException("This shortcut method can be called only for vectors");
    }
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
        int[] rows = new int[rows() - rem.size()];
        int pos = 0;
        for (int i = 0; i < rows(); i++) {
            if (rem.contains(i))
                continue;
            rows[pos++] = i;
        }
        return new MappedM(this, true, rows);
    }

    default M mapCols(int... indexes) {
        return new MappedM(this, false, indexes);
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
        int[] cols = new int[cols() - rem.size()];
        int pos = 0;
        for (int i = 0; i < cols(); i++) {
            if (rem.contains(i))
                continue;
            cols[pos++] = i;
        }
        return new MappedM(this, false, cols);
    }

    default M solidCopy() {
        M m = new SolidM(rows(), cols());
        for (int i = 0; i < rows(); i++) {
            for (int j = 0; j < cols(); j++) {
                m.set(i, j, get(i, j));
            }
        }
        return m;
    }

    // operations

    default M t() {
        return new TransposeM(this);
    }

    default void increment(int i, int j, double value) {
        double old = get(i, j);
        set(i, j, old + value);
    }

    // summary

    default void buildSummary(StringBuilder sb) {

        DecimalFormat f = Printer.formatDecShort;

        String[][] m = new String[rows()][cols()];
        int max = 0;
        for (int i = 0; i < rows(); i++) {
            for (int j = 0; j < cols(); j++) {
                m[i][j] = f.format(get(i, j));
                max = Math.max(max, m[i][j].length() + 1);
            }
        }
        max = Math.max(max, String.format("[,%d]", rows()).length());
        max = Math.max(max, String.format("[%d,]", cols()).length());

        String fm = "%-" + max + "s";

        int hCount = (int) Math.floor(WS.getPrinter().getTextWidth() / (double) max);
        int vCount = Math.min(rows() + 1, 21);
        int hLast = 0;
        while (true) {

            // take vertical stripes
            if (hLast >= cols())
                break;

            int hStart = hLast;
            int hEnd = Math.min(hLast + hCount, cols());
            int vLast = 0;

            while (true) {

                // print rows
                if (vLast >= rows())
                    break;

                int vStart = vLast;
                int vEnd = Math.min(vLast + vCount, rows());

                for (int i = vStart; i <= vEnd; i++) {
                    for (int j = hStart; j <= hEnd; j++) {
                        if (i == vStart && j == hStart) {
                            WS.print(String.format("%" + (max + 1) + "s", ""));
                            continue;
                        }
                        if (i == vStart) {
                            WS.print(String.format("[ ,%" + (max - 4) + "d]", j - 1));
                            continue;
                        }
                        if (j == hStart) {
                            WS.print(String.format("[%" + (max - 4) + "d, ]", i - 1));
                            continue;
                        }
                        WS.print(String.format("%" + max + "s", m[i - 1][j - 1]));
                    }
                    WS.print("\n");
                }
                WS.print("\n");
                vLast = vEnd;
            }
            hLast = hEnd;
        }
    }

}
