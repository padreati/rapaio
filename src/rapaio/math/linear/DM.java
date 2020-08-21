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

package rapaio.math.linear;

import rapaio.math.linear.interfaces.DMAggegateOps;
import rapaio.math.linear.interfaces.DMAlgebraOps;
import rapaio.math.linear.interfaces.DMMathOps;
import rapaio.math.linear.interfaces.DMOrderingOps;
import rapaio.math.linear.interfaces.DMTransformDataOps;
import rapaio.printer.Printable;

import java.io.Serializable;
import java.util.stream.DoubleStream;

/**
 * Dense matrix with double precision floating point values
 * <p>
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 2/3/16.
 */
public interface DM extends DMMathOps, DMAlgebraOps, DMAggegateOps, DMOrderingOps, DMTransformDataOps,
        Serializable, Printable {

    enum Type {
        /**
         * Base implementation using only the set/get API used as reference for
         * performance benchmarks and as a starting point for new implementations
         */
        BASE,
        /**
         * Array of arrays implementation of a matrix. I can use row major or column
         * major ordering.
         */
        STRIPE,
        /**
         * Mapped view over dense array
         */
        MAP
    }

    Type type();

    /**
     * @return number of rows of the matrix
     */
    int rowCount();

    /**
     * @return number of columns of the matrix
     */
    int colCount();

    /**
     * Getter for value found at given row and column index.
     *
     * @param row row index
     * @param col column index
     * @return value at given row index and column index
     */
    double get(final int row, final int col);

    /**
     * Sets value at the given row and column indexes
     *
     * @param row   row index
     * @param col   column index
     * @param value value to be set
     */
    void set(final int row, int col, final double value);

    /**
     * Increment the value at given position.
     *
     * @param row   row index
     * @param col   column index
     * @param value value to be added
     */
    void increment(final int row, final int col, final double value);

    /**
     * Returns a vector build from values of a row in
     * the original matrix. Depending on implementation,
     * the vector can be a view over the original data.
     * If one wants a copy of a row as a vector than
     * she must call {@link #mapRowCopy(int)}.
     *
     * @param row row index
     * @return result vector reference
     */
    DV mapRow(final int row);

    /**
     * Returns a vector build from values of a row
     * in original matrix. The vector contains a copy of
     * the original values.
     *
     * @param row row index
     * @return result vector reference
     */
    DV mapRowCopy(final int row);

    /**
     * Creates a new matrix which contains only the rows
     * specified by given indexes. Depending on the implementation
     * the new matrix can be a view on the original data. If one
     * wants a copied matrix, she must call {@link #mapRowsCopy(int...)}
     *
     * @param rows row indexes
     * @return result matrix reference
     */
    DM mapRows(int... rows);

    /**
     * Creates a new matrix which contains only the rows
     * specified by given indexes. The new matrix is a copy
     * over the original data.
     *
     * @param rows row indexes
     * @return result matrix reference
     */
    DM mapRowsCopy(int... rows);

    /**
     * Creates a new matrix which contains only rows with
     * indices in the given range starting from {@param start} inclusive
     * and ending at {@param end} exclusive. Depending on the implementation
     * the new matrix can be a view. To obtain a copy method
     * {@link #rangeRowsCopy(int, int)} must be called.
     *
     * @param start start row index (inclusive)
     * @param end   end row index (exclusive)
     * @return result matrix reference
     */
    DM rangeRows(int start, int end);

    /**
     * Creates a new matrix which contains only rows with
     * indices in the given range starting from {@param start} inclusive
     * and ending at {@param end} exclusive. The new matrix contains a
     * copy of the data.
     *
     * @param start start row index (inclusive)
     * @param end   end row index (exclusive)
     * @return result matrix reference
     */
    DM rangeRowsCopy(int start, int end);

    /**
     * Builds a new matrix having all columns and all the rows not specified by given indexes.
     * Depending on the implementation this can be a view over the original matrix.
     * To obtain a new copy of the data method {@link #removeRowsCopy(int...)} must be called.
     *
     * @param rows rows which will be removed
     * @return new mapped matrix containing all rows not specified by indexes
     */
    DM removeRows(int... rows);

    /**
     * Builds a new matrix having all columns and all the rows not specified by given indexes.
     * containg a copy of the original data.
     *
     * @param indexes rows which will be removed
     * @return new mapped matrix containing all rows not specified by indexes
     */
    DM removeRowsCopy(int... indexes);

    /**
     * Returns a vector build from values of a column in
     * the original matrix. Depending on implementation,
     * the vector can be a view over the original array.
     * If one wants a copy of a column as a vector than
     * she must call {@link #mapColCopy(int)}.
     *
     * @param col column index
     * @return result vector reference
     */
    DV mapCol(final int col);

    /**
     * Returns a vector build from values of a column
     * in original matrix. The vector contains a copy of
     * the original values.
     *
     * @param col column index
     * @return result vector reference
     */
    DV mapColCopy(final int col);

    /**
     * Creates a new matrix which contains only the cols
     * specified by given indexes. Depending on the implementation
     * the new matrix can be a view on the original data. If one
     * wants a copied matrix, she must call {@link #mapColsCopy(int...)}
     *
     * @param indexes column indexes
     * @return result matrix reference
     */
    DM mapCols(int... indexes);

    /**
     * Creates a new matrix which contains only the cols
     * specified by given indexes. The new matrix is a copy
     * over the original data.
     *
     * @param cols column indexes
     * @return result matrix reference
     */
    DM mapColsCopy(int... cols);

    /**
     * Creates a new matrix which contains only cols with
     * indices in the given range starting from {@param start} inclusive
     * and ending at {@param end} exclusive. Depending on the implementation
     * the new matrix can be a view. To obtain a copy method
     * {@link #rangeColsCopy(int, int)} must be called.
     *
     * @param start start column index (inclusive)
     * @param end   end column index (exclusive)
     * @return result matrix reference
     */
    DM rangeCols(int start, int end);

    /**
     * Creates a new matrix which contains only columns with
     * indices in the given range starting from {@param start} inclusive
     * and ending at {@param end} exclusive. The new matrix contains a
     * copy of the data.
     *
     * @param start start column index (inclusive)
     * @param end   end column index (exclusive)
     * @return result matrix reference
     */
    DM rangeColsCopy(int start, int end);

    /**
     * Builds a new matrix having all columns not specified by given indexes.
     * Depending on the implementation this can be a view over the original matrix.
     * To obtain a new copy of the data method {@link #removeColsCopy(int...)} must be called.
     *
     * @param cols columns which will be removed
     * @return new mapped matrix containing all rows not specified by indexes
     */
    DM removeCols(int... cols);

    /**
     * Builds a new matrix having all columns not specified by given indexes.
     * containg a copy of the original data.
     *
     * @param cols columns which will be removed
     * @return new mapped matrix containing all rows not specified by indexes
     */
    DM removeColsCopy(int... cols);

    /**
     * Stream of double values, the element order is not guaranteed,
     * it depends on the implementation.
     *
     * @return double value stream
     */
    DoubleStream valueStream();

    /**
     * Creates a copy of a matrix.
     *
     * @return copy matrix reference
     */
    DM copy();

    /**
     * Compares matrices using a tolerance of 1e-12 for values.
     * If the absolute difference between two values is less
     * than the specified tolerance, than the values are
     * considered equal.
     *
     * @param m matrix to compare with
     * @return true if dimensions and elements are equal
     */
    default boolean deepEquals(DM m) {
        return deepEquals(m, 1e-12);
    }

    /**
     * Compares matrices using a tolerance for values.
     * If the absolute difference between two values is less
     * than the specified tolerance, than the values are
     * considered equal.
     *
     * @param m   matrix to compare with
     * @param eps tolerance
     * @return true if dimensions and elements are equal
     */
    boolean deepEquals(DM m, double eps);
}
