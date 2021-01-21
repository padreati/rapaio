/*
 * Apache License
 * Version 2.0, January 2004
 * http://www.apache.org/licenses/
 *
 *    Copyright 2013 - 2021 Aurelian Tutuianu
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

import rapaio.printer.Printable;
import rapaio.util.function.Double2DoubleFunction;

import java.io.Serializable;
import java.util.stream.DoubleStream;

/**
 * Dense matrix with double precision floating point values
 * <p>
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 2/3/16.
 */
public interface DMatrix extends Serializable, Printable {

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
    void inc(final int row, final int col, final double value);

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
    DVector mapRow(final int row);

    /**
     * Returns a vector build from values of a row
     * in original matrix. The vector contains a copy of
     * the original values.
     *
     * @param row row index
     * @return result vector reference
     */
    DVector mapRowCopy(final int row);

    /**
     * Creates a new matrix which contains only the rows
     * specified by given indexes. Depending on the implementation
     * the new matrix can be a view on the original data. If one
     * wants a copied matrix, she must call {@link #mapRowsCopy(int...)}
     *
     * @param rows row indexes
     * @return result matrix reference
     */
    DMatrix mapRows(int... rows);

    /**
     * Creates a new matrix which contains only the rows
     * specified by given indexes. The new matrix is a copy
     * over the original data.
     *
     * @param rows row indexes
     * @return result matrix reference
     */
    DMatrix mapRowsCopy(int... rows);

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
    DMatrix rangeRows(int start, int end);

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
    DMatrix rangeRowsCopy(int start, int end);

    /**
     * Builds a new matrix having all columns and all the rows not specified by given indexes.
     * Depending on the implementation this can be a view over the original matrix.
     * To obtain a new copy of the data method {@link #removeRowsCopy(int...)} must be called.
     *
     * @param rows rows which will be removed
     * @return new mapped matrix containing all rows not specified by indexes
     */
    DMatrix removeRows(int... rows);

    /**
     * Builds a new matrix having all columns and all the rows not specified by given indexes.
     * containg a copy of the original data.
     *
     * @param indexes rows which will be removed
     * @return new mapped matrix containing all rows not specified by indexes
     */
    DMatrix removeRowsCopy(int... indexes);

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
    DVector mapCol(final int col);

    /**
     * Returns a vector build from values of a column
     * in original matrix. The vector contains a copy of
     * the original values.
     *
     * @param col column index
     * @return result vector reference
     */
    DVector mapColCopy(final int col);

    /**
     * Creates a new matrix which contains only the cols
     * specified by given indexes. Depending on the implementation
     * the new matrix can be a view on the original data. If one
     * wants a copied matrix, she must call {@link #mapColsCopy(int...)}
     *
     * @param indexes column indexes
     * @return result matrix reference
     */
    DMatrix mapCols(int... indexes);

    /**
     * Creates a new matrix which contains only the cols
     * specified by given indexes. The new matrix is a copy
     * over the original data.
     *
     * @param cols column indexes
     * @return result matrix reference
     */
    DMatrix mapColsCopy(int... cols);

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
    DMatrix rangeCols(int start, int end);

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
    DMatrix rangeColsCopy(int start, int end);

    /**
     * Builds a new matrix having all columns not specified by given indexes.
     * Depending on the implementation this can be a view over the original matrix.
     * To obtain a new copy of the data method {@link #removeColsCopy(int...)} must be called.
     *
     * @param cols columns which will be removed
     * @return new mapped matrix containing all rows not specified by indexes
     */
    DMatrix removeCols(int... cols);

    /**
     * Builds a new matrix having all columns not specified by given indexes.
     * containg a copy of the original data.
     *
     * @param cols columns which will be removed
     * @return new mapped matrix containing all rows not specified by indexes
     */
    DMatrix removeColsCopy(int... cols);

    /**
     * Adds a scalar value to all elements of a matrix. If possible,
     * the operation is realized in place.
     *
     * @param x value to be added
     * @return instance of the result matrix
     */
    DMatrix add(double x);

    /**
     * Add vector values to all rows (axis 0) or vectors (axis 1).
     *
     * @param x    vector to be added
     * @param axis axis addition
     * @return same matrix with added values
     */
    DMatrix add(DVector x, int axis);

    /**
     * Adds element wise values from given matrix. If possible,
     * the operation is realized in place.
     *
     * @param b matrix with elements to be added
     * @return instance of the result matrix
     */
    DMatrix add(DMatrix b);

    /**
     * Subtract a scalar value to all elements of a matrix. If possible,
     * the operation is realized in place.
     *
     * @param x value to be substracted
     * @return instance of the result matrix
     */
    DMatrix sub(double x);

    /**
     * Subtract vector values to all rows (axis 0) or vectors (axis 1).
     *
     * @param x    vector to be added
     * @param axis axis addition
     * @return same matrix with added values
     */
    DMatrix sub(DVector x, int axis);

    /**
     * Subtracts element wise values from given matrix. If possible,
     * the operation is realized in place.
     *
     * @param b matrix with elements to be substracted
     * @return instance of the result matrix
     */
    DMatrix sub(DMatrix b);

    /**
     * Multiply a scalar value to all elements of a matrix. If possible,
     * the operation is realized in place.
     *
     * @param x value to be multiplied with
     * @return instance of the result matrix
     */
    DMatrix mult(double x);

    /**
     * Multiply vector values to all rows (axis 0) or columns (axis 1).
     *
     * @param x    vector to be added
     * @param axis axis addition
     * @return same matrix with added values
     */
    DMatrix mult(DVector x, int axis);

    /**
     * Multiplies element wise values from given matrix. If possible,
     * the operation is realized in place.
     *
     * @param b matrix with elements to be multiplied with
     * @return instance of the result matrix
     */
    DMatrix mult(DMatrix b);

    /**
     * Divide a scalar value from all elements of a matrix. If possible,
     * the operation is realized in place.
     *
     * @param x divisor value
     * @return instance of the result matrix
     */
    DMatrix div(double x);

    /**
     * Divide all rows (axis 0) or columns (axis 1) by elements of the given vector
     *
     * @param x    vector to be added
     * @param axis axis addition
     * @return same matrix with added values
     */
    DMatrix div(DVector x, int axis);

    /**
     * Divides element wise values from given matrix. If possible,
     * the operation is realized in place.
     *
     * @param b matrix with division elements
     * @return instance of the result matrix
     */
    DMatrix div(DMatrix b);

    /**
     * Apply the given function to all elements of the matrix.
     *
     * @param fun function to be applied
     * @return same instance matrix
     */
    DMatrix apply(Double2DoubleFunction fun);

    /**
     * Computes matrix vector multiplication.
     *
     * @param b vector to be multiplied with
     * @return result vector
     */
    DVector dot(DVector b);

    /**
     * Compute matrix multiplication between the current
     * matrix and the diagonal matrix obtained from the given vector.
     * <p>
     * A * I * v
     *
     * @param v diagonal vector
     * @return result matrix
     */
    DMatrix dotDiag(DVector v);

    /**
     * Compute matrix multiplication between the current
     * matrix and the diagonal matrix obtained from the given vector.
     * <p>
     * v^T * I * A
     *
     * @param v diagonal vector
     * @return result matrix
     */
    DMatrix dotDiagT(DVector v);

    /**
     * Computes matrix - matrix multiplication.
     *
     * @param b matrix to be multiplied with
     * @return matrix result
     */
    DMatrix dot(DMatrix b);

    /**
     * Trace of the matrix, if the matrix is square. The trace of a squared
     * matrix is the sum of the elements from the main diagonal.
     * Otherwise returns an exception.
     *
     * @return value of the matrix trace
     */
    double trace();

    /**
     * Matrix rank obtained using singular value decomposition.
     *
     * @return effective numerical rank, obtained from SVD.
     */
    int rank();

    /**
     * Creates an instance of a transposed matrix. Depending on implementation
     * this can be a view of the original data.
     *
     * @return new transposed matrix
     */
    DMatrix t();

    /**
     * Vector with values from main diagonal
     */
    DVector diag();

    /**
     * Computes scatter matrix.
     *
     * @return scatter matrix instance
     */
    DMatrix scatter();

    /**
     * Builds a vector with maximum values from rows/cols.
     * If axis = 0 and matrix has m rows and n columns, the resulted vector
     * will have size m and will contain in each position the maximum
     * value from the row with that position.
     *
     * @param axis axis for which to compute maximal values
     * @return vector with result values
     */
    DVector amax(int axis);

    /**
     * Builds a vector with indexes of the maximum values from rows/columns.
     * Thus if a matrix has m rows and n columns, the resulted vector
     * will have size m and will contain in each position the maximum
     * value from the row with that position.
     *
     * @return vector with indexes of max value values
     */
    int[] argmax(int axis);

    /**
     * Builds a vector with minimum values from rows/cols.
     * If axis = 0 and matrix has m rows and n columns, the resulted vector
     * will have size m and will contain in each position the minimum
     * value from the row with that position.
     *
     * @param axis axis for which to compute maximal values
     * @return vector with result values
     */
    DVector amin(int axis);

    /**
     * Builds a vector with indexes of the minimum value index from rows/columns.
     * Thus if a matrix has m rows and n columns, the resulted vector
     * will have size m and will contain in each position the minimum
     * value index from the row with that position.
     *
     * @return vector with indexes of max value values
     */
    int[] argmin(int axis);

    /**
     * Computes the sum of all elements from the matrix
     *
     * @return scalar value with sum
     */
    double sum();

    /**
     * Computes the sum of all elements on the given axis. If axis
     * is 0 it will compute sum on rows, the resulting vector having size
     * as the number of rows and on each position the sum of elements from
     * that row. If the axis is 1 it will compute sums on columns.
     *
     * @param axis specifies the dimension used for summing
     * @return vector of sums on the given axis
     */
    DVector sum(int axis);

    /**
     * Computes the mean of all elements of the matrix.
     *
     * @return mean of all matrix elements
     */
    default double mean() {
        return sum() / (rowCount() * colCount());
    }

    /**
     * Computes vector of means along the specified axis.
     *
     * @param axis 0 for rows,  for columns
     * @return vector of means along axis
     */
    default DVector mean(int axis) {
        return sum(axis).div(axis == 0 ? rowCount() : colCount());
    }

    /**
     * Compute the variance of all elements of the matrix
     *
     * @return variance of all elements of the matrix
     */
    double variance();

    /**
     * Computes vector of variances along the given axis of the matrix
     *
     * @param axis 0 for rows, 1 for columns
     * @return vector of variances computed along given axis
     */
    DVector variance(int axis);

    /**
     * Compute the standard deviation of all elements of the matrix
     *
     * @return standard deviation of all elements of the matrix
     */
    default double sd() {
        return Math.sqrt(variance());
    }

    /**
     * Computes vector of standard deviations along the given axis of the matrix
     *
     * @param axis 0 for rows, 1 for columns
     * @return vector of standard deviations computed along given axis
     */
    default DVector sd(int axis) {
        return variance(axis).apply(Math::sqrt);
    }

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
    DMatrix copy();

    /**
     * Compares matrices using a tolerance of 1e-12 for values.
     * If the absolute difference between two values is less
     * than the specified tolerance, than the values are
     * considered equal.
     *
     * @param m matrix to compare with
     * @return true if dimensions and elements are equal
     */
    default boolean deepEquals(DMatrix m) {
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
    boolean deepEquals(DMatrix m, double eps);
}
