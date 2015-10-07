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

package rapaio.math.linear;

import rapaio.core.MathTools;
import rapaio.core.stat.Mean;
import rapaio.core.stat.Variance;
import rapaio.data.Numeric;
import rapaio.sys.WS;
import rapaio.printer.Printable;
import rapaio.math.linear.impl.*;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import static rapaio.sys.WS.formatFlex;

/**
 * Real value matrix interface
 * <p>
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> at 2/3/15.
 */
@Deprecated
public interface RM extends Serializable, Printable {

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

    /**
     * Sets value from a given position in the matrix using a function
     *
     * @param i      row number
     * @param j      col number
     * @param update update function, takes cell value and outputs transformed value
     */
    default void set(int i, int j, Function<Double, Double> update) {
        set(i, j, update.apply(get(i, j)));
    }

    /**
     * Increment value from a given position with the increment value
     *
     * @param i         row number
     * @param j         col number
     * @param increment value to be added to the cell value
     */
    default void increment(int i, int j, double increment) {
        double old = get(i, j);
        set(i, j, old + increment);
    }

    // transforming methods

    /**
     * Builds a new matrix having only the specified rows
     *
     * @param indexes row indexes
     * @return new mapped matrix containing all columns and selected rows
     */
    default RM mapRows(int... indexes) {
        return new MappedRM(this, true, indexes);
    }

    default RV mapRow(int index) {
        return new MappedRowRV(this, index);
    }

    default RM rangeRows(int start, int end) {
        int[] rows = new int[end - start];
        for (int i = start; i < end; i++) {
            rows[i - start] = i;
        }
        return new MappedRM(this, true, rows);
    }

    /**
     * Builds a new matrix having all columns and all the rows not specified by given indexes
     *
     * @param indexes rows which will be removed
     * @return new mapped matrix containing all rows not specified by indexes
     */
    default RM removeRows(int... indexes) {
        Set<Integer> rem = Arrays.stream(indexes).boxed().collect(Collectors.toSet());
        int[] rows = new int[rowCount() - rem.size()];
        int pos = 0;
        for (int i = 0; i < rowCount(); i++) {
            if (rem.contains(i))
                continue;
            rows[pos++] = i;
        }
        return new MappedRM(this, true, rows);
    }

    default RM mapCols(int... indexes) {
        return new MappedRM(this, false, indexes);
    }

    default RV mapCol(int index) {
        return new MappedColRV(this, index);
    }

    default RM rangeCols(int start, int end) {
        int[] cols = new int[end - start];
        for (int i = start; i < end; i++) {
            cols[i - start] = i;
        }
        return new MappedRM(this, false, cols);
    }

    default RM removeCols(int... indexes) {
        Set<Integer> rem = Arrays.stream(indexes).boxed().collect(Collectors.toSet());
        int[] cols = new int[colCount() - rem.size()];
        int pos = 0;
        for (int i = 0; i < colCount(); i++) {
            if (rem.contains(i))
                continue;
            cols[pos++] = i;
        }
        return new MappedRM(this, false, cols);
    }

    ///////////////////////
    // matrix operations
    ///////////////////////

    default RM t() {
        return new TransposeRM(this);
    }

    default double det() {
        return new LUDecomposition(this, LUDecomposition.Method.GAUSSIAN_ELIMINATION).det();
    }

    /**
     * Matrix multiplication of this matrix with the one given as parameter.
     * The implementation uses a naive algorithm, it can be done much better
     * al least by implementing Strassen algorithm.
     *
     * @param B matrix with which it will be multiplied.
     * @return new matrix as a result of multiplication
     */
    default RM dot(RM B) {
        if (colCount() != B.rowCount()) {
            throw new IllegalArgumentException(String.format("Matrices are not conform for multiplication ([n,m]x[m,p] = [%d,%d]=[%d,%d])",
                    rowCount(), colCount(), B.rowCount(), B.colCount()));
        }
        RM C = Linear.newRMEmpty(rowCount(), B.colCount());
        for (int i = 0; i < rowCount(); i++) {
            for (int j = 0; j < B.colCount(); j++) {
                double s = 0;
                for (int k = 0; k < colCount(); k++) {
                    s += get(i, k) * B.get(k, j);
                }
                C.set(i, j, s);
            }
        }
        return C;
    }

    default RM plus(double x) {
        for (int i = 0; i < rowCount(); i++) {
            for (int j = 0; j < colCount(); j++) {
                increment(i, j, x);
            }
        }
        return this;
    }

    default RM plus(RM B) {
        if ((rowCount() != B.rowCount()) || (colCount() != B.colCount()))
            throw new IllegalArgumentException(String.format(
                    "Matrices are not conform for addition: [%d x %d] + [%d x %d]", rowCount(), colCount(), B.rowCount(), B.colCount()));
        for (int i = 0; i < rowCount(); i++) {
            for (int j = 0; j < colCount(); j++) {
                increment(i, j, B.get(i, j));
            }
        }
        return this;
    }

    default RM minus(double x) {
        return plus(-x);
    }

    default RM minus(RM B) {
        if ((rowCount() != B.rowCount()) || (colCount() != B.colCount()))
            throw new IllegalArgumentException(String.format(
                    "Matrices are not conform for substraction: [%d x %d] + [%d x %d]", rowCount(), colCount(), B.rowCount(), B.colCount()));
        for (int i = 0; i < rowCount(); i++) {
            for (int j = 0; j < colCount(); j++) {
                increment(i, j, -B.get(i, j));
            }
        }
        return this;
    }

    /**
     * Scalar matrix multiplication.
     * It updates the current matrix so make a solid copy to not alter actual data.
     *
     * @param b scalar value used for multiplication
     * @return current instance multiplied with a scalar
     */
    default RM dot(double b) {
        for (int i = 0; i < rowCount(); i++) {
            for (int j = 0; j < colCount(); j++) {
                set(i, j, get(i, j) * b);
            }
        }
        return this;
    }

    default RM cov() {
        RM scatter = Linear.newRMEmpty(colCount(), colCount());
        RV mean = Linear.newRVEmpty(colCount());
        for (int i = 0; i < colCount(); i++) {
            mean.set(i, mapCol(i).mean().value());
        }
        for (int i = 0; i < rowCount(); i++) {

            RM row = mapRow(i).copy();
            row.minus(mean.t());

            scatter.plus(row.t().dot(row));
        }
        return scatter.dot(1.0 / (rowCount() - 1));
    }

    default RM scaleUnit(boolean cols) {
        RM src = cols ? this : this.t();

        RV mean = Linear.newRVEmpty(src.colCount());
        RV sd = Linear.newRVEmpty(src.colCount());
        for (int i = 0; i < src.colCount(); i++) {
            mean.set(i, src.mapCol(i).mean().value());
            sd.set(i, src.mapCol(i).var().sdValue());
        }
        for (int i = 0; i < src.rowCount(); i++) {
            for (int j = 0; j < src.colCount(); j++) {
                src.set(i, j, (src.get(i, j) - mean.get(j)) / sd.get(j));
            }
        }
        return this;
    }

    default RM scatter() {
        RM scatter = Linear.newRMEmpty(colCount(), colCount());
        RV mean = Linear.newRVEmpty(colCount());
        for (int i = 0; i < colCount(); i++) {
            mean.set(i, mapCol(i).mean().value());
        }
        for (int i = 0; i < rowCount(); i++) {

            RM row = mapRow(i).copy();
            row.minus(mean);

            scatter.plus(row.dot(row.t()));
        }
        return scatter;
    }

    /**
     * Diagonal vector of values
     */
    default RV diag() {
        RV rv = Linear.newRVEmpty(rowCount());
        for (int i = 0; i < rowCount(); i++) {
            rv.set(i, get(i, i));
        }
        return rv;
    }

    /**
     * Matrix rank
     *
     * @return effective numerical rank, obtained from SVD.
     */
    default int rank() {
        return new SVDecomposition(this).rank();
    }

    default Mean mean() {
        Numeric values = Numeric.newEmpty();
        for (int i = 0; i < rowCount(); i++) {
            for (int j = 0; j < colCount(); j++) {
                values.addValue(get(i, j));
            }
        }
        return new Mean(values);
    }

    default Variance var() {
        Numeric values = Numeric.newEmpty();
        for (int i = 0; i < rowCount(); i++) {
            for (int j = 0; j < colCount(); j++) {
                values.addValue(get(i, j));
            }
        }
        return new Variance(values);
    }

    default QR qr() {
        return new QR(this);
    }

    ///////////////////////
    // other tools
    ///////////////////////

    /**
     * Does not override equals since this is a costly
     * algorithm and can slow down processing as a side effect.
     *
     * @param RM given matrix
     * @return true if dimension and elements are equal
     */
    default boolean isEqual(RM RM) {
        return isEqual(RM, 1e-12);
    }

    default boolean isEqual(RM RM, double tol) {
        if (rowCount() != RM.rowCount())
            return false;
        if (colCount() != RM.colCount())
            return false;
        for (int i = 0; i < rowCount(); i++) {
            for (int j = 0; j < colCount(); j++) {
                if (!MathTools.eq(get(i, j), RM.get(i, j), tol))
                    return false;
            }
        }
        return true;
    }

    /**
     * Makes a solid copy of the matrix
     *
     * @return new solid copy of the matrix
     */
    default RM copy() {
        RM RM = new SolidRM(rowCount(), colCount());
        for (int i = 0; i < rowCount(); i++) {
            for (int j = 0; j < colCount(); j++) {
                RM.set(i, j, get(i, j));
            }
        }
        return RM;
    }

    //////////////////
    // printSummary
    //////////////////

    default String summary() {

        StringBuilder sb = new StringBuilder();

        String[][] m = new String[rowCount()][colCount()];
        int max = 1;
        for (int i = 0; i < rowCount(); i++) {
            for (int j = 0; j < colCount(); j++) {
                m[i][j] = formatFlex(get(i, j));
                max = Math.max(max, m[i][j].length() + 1);
            }
        }
        max = Math.max(max, String.format("[,%d]", rowCount()).length());
        max = Math.max(max, String.format("[%d,]", colCount()).length());

        int hCount = (int) Math.floor(WS.getPrinter().getTextWidth() / (double) max);
        int vCount = Math.min(rowCount() + 1, 101);
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
                            sb.append(String.format("%" + (max) + "s| ", ""));
                            continue;
                        }
                        if (i == vStart) {
                            sb.append(String.format("%" + Math.max(1, max - 1) + "d|", j - 1));
                            continue;
                        }
                        if (j == hStart) {
                            sb.append(String.format("%" + Math.max(1, max - 1) + "d |", i - 1));
                            continue;
                        }
                        sb.append(String.format("%" + max + "s", m[i - 1][j - 1]));
                    }
                    sb.append("\n");
                }
                vLast = vEnd;
            }
            hLast = hEnd;
        }
        return sb.toString();
    }
}
