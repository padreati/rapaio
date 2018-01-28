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

import java.io.Serializable;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.DoubleStream;

import rapaio.core.stat.Mean;
import rapaio.core.stat.Variance;
import rapaio.data.NumVar;
import rapaio.math.MTools;
import rapaio.math.linear.dense.MappedRM;
import rapaio.math.linear.dense.MatrixMultiplication;
import rapaio.math.linear.dense.SVDecomposition;
import rapaio.math.linear.dense.SolidRM;
import rapaio.math.linear.dense.SolidRV;
import rapaio.printer.Printable;
import rapaio.sys.WS;

/**
 * Dense matrix with double precision floating point values
 * <p>
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 2/3/16.
 */
public interface RM extends Serializable, Printable {

    /**
     * @return number of rows
     */
    int rowCount();

    /**
     * @return number of columns
     */
    int colCount();

    /**
     * @param row row index
     * @param col column index
     * @return value at given row index and column index
     */
    double get(int row, int col);

    /**
     * Sets value at the given row and column indexes
     *
     * @param row   row index
     * @param col   column index
     * @param value value to be set
     */
    void set(int row, int col, double value);

    /**
     * Increment value at the given row and column indexes
     *
     * @param row   row index
     * @param col   column index
     * @param value increment value
     */
    void increment(int row, int col, double value);

    RV mapCol(int col);

    RV mapRow(int row);

    default RM mapRows(int... indexes) {
        return new MappedRM(this, true, indexes);
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

    /**
     * @return new transposed matrix
     */
    RM t();

    default RM dot(RM B) {
        return MatrixMultiplication.ikjParallel(this, B);
    }

    default RV dot(RV b) {
        return MatrixMultiplication.ikjParallel(this, b);
    }

    default RM dot(double x) {
        for (int i = 0; i < rowCount(); i++) {
            for (int j = 0; j < colCount(); j++) {
                set(i, j, get(i, j) * x);
            }
        }
        return this;
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
     * Matrix rank
     *
     * @return effective numerical rank, obtained from SVD.
     */
    default int rank() {
        return SVDecomposition.from(this).rank();
    }

    default Mean mean() {
        NumVar values = NumVar.empty();
        for (int i = 0; i < rowCount(); i++) {
            for (int j = 0; j < colCount(); j++) {
                values.addValue(get(i, j));
            }
        }
        return Mean.from(values);
    }

    default Variance var() {
        NumVar values = NumVar.empty();
        for (int i = 0; i < rowCount(); i++) {
            for (int j = 0; j < colCount(); j++) {
                values.addValue(get(i, j));
            }
        }
        return Variance.from(values);
    }

    /**
     * Diagonal vector of values
     */
    default RV diag() {
        RV rv = SolidRV.empty(rowCount());
        for (int i = 0; i < rowCount(); i++) {
            rv.set(i, get(i, i));
        }
        return rv;
    }

    default RM scatter() {
        RM scatter = SolidRM.empty(colCount(), colCount());
        double[] mean = new double[colCount()];
        for (int i = 0; i < colCount(); i++) {
            mean[i] = mapCol(i).mean().value();
        }
        for (int k = 0; k < rowCount(); k++) {
            double[] row = new double[colCount()];
            for (int i = 0; i < colCount(); i++)
                row[i] = get(k, i) - mean[i];
            for (int i = 0; i < row.length; i++) {
                for (int j = 0; j < row.length; j++) {
                    scatter.increment(i, j, row[i] * row[j]);
                }
            }
        }
        return scatter;
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
        return isEqual(RM, 1e-20);
    }

    default boolean isEqual(RM RM, double tol) {
        if (rowCount() != RM.rowCount())
            return false;
        if (colCount() != RM.colCount())
            return false;
        for (int i = 0; i < rowCount(); i++) {
            for (int j = 0; j < colCount(); j++) {
                if (!MTools.eq(get(i, j), RM.get(i, j), tol))
                    return false;
            }
        }
        return true;
    }

    DoubleStream valueStream();

    RM solidCopy();

    default String summary() {

        StringBuilder sb = new StringBuilder();

        String[][] m = new String[rowCount()][colCount()];
        int max = 1;
        for (int i = 0; i < rowCount(); i++) {
            for (int j = 0; j < colCount(); j++) {
                m[i][j] = WS.formatShort(get(i, j));
                max = Math.max(max, m[i][j].length() + 1);
            }
        }
        max = Math.max(max, String.format("[,%d]", rowCount()).length());
        max = Math.max(max, String.format("[%d,]", colCount()).length());

        int hCount = (int) Math.floor(WS.getPrinter().textWidth() / (double) max);
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

    /**
     * @param row
     * @return
     */
    double[] getRow(int row);
}
