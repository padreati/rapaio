/*
 * Apache License
 * Version 2.0, January 2004
 * http://www.apache.org/licenses/
 *
 * Copyright 2013 - 2022 Aurelian Tutuianu
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package rapaio.math.linear;

import java.io.Serializable;
import java.util.Iterator;
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.stream.DoubleStream;

import rapaio.core.distributions.Distribution;
import rapaio.core.distributions.Normal;
import rapaio.data.VarDouble;
import rapaio.math.linear.dense.DMatrixDenseC;
import rapaio.math.linear.dense.DVectorDense;
import rapaio.printer.Printable;
import rapaio.util.DoubleComparator;
import rapaio.util.DoubleComparators;
import rapaio.util.collection.DoubleArrays;
import rapaio.util.collection.IntArrays;
import rapaio.util.function.Double2DoubleFunction;
import rapaio.util.function.Int2DoubleFunction;

/**
 * Vector of values in double floating precision.
 * <p>
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 2/3/16.
 */
public interface DVector extends Serializable, Printable, Iterable<Double> {

    /**
     * Builds a new real dense vector of size {@param n} filled with 0.
     *
     * @param n the size of the vector
     * @return dense vector instance
     */
    static DVectorDense zeros(int n) {
        return fill(n, 0);
    }

    /**
     * Builds a new double vector of size {@param n} filled with 1.
     *
     * @param n the size of the vector
     * @return vector instance
     */
    static DVectorDense ones(int n) {
        return fill(n, 1);
    }

    /**
     * Builds a standard basis vector of dimension {@code n} with {@code 1}
     * on the given position.
     *
     * @param n   dimension of the vector
     * @param pos position from the vector set to value {@code 1}
     * @return basis vector instance
     */
    static DVectorDense one(int n, int pos) {
        DVectorDense v = zeros(n);
        v.set(pos, 1);
        return v;
    }

    /**
     * Builds a new real vector of {@code len} size, filled with {@code fill} value given as parameter.
     *
     * @param n    size of the vector
     * @param fill fill value
     * @return new dense vector of given type
     */
    static DVectorDense fill(int n, double fill) {
        return new DVectorDense(0, n, DoubleArrays.newFill(n, fill));
    }

    /**
     * Build a dense vector with random values drawn from a standard normal
     * distribution.
     *
     * @param size size of the vector
     * @return dense vector with random values
     */
    static DVectorDense random(int size) {
        return random(size, Normal.std());
    }

    /**
     * Builds a random vector with random values drawn from the distribution
     * given as parameter.
     *
     * @param size         size of the vector
     * @param distribution distribution which generates the values
     * @return dense vector with random values
     */
    static DVectorDense random(int size, Distribution distribution) {
        return DVectorDense.random(size, distribution);
    }

    static DVectorDense wrap(double... values) {
        return wrapAt(0, values.length, values);
    }

    /**
     * Builds a new random vector which wraps a double array.
     * It uses the same reference.
     *
     * @param values referenced array of values
     * @return new real dense vector
     */
    static DVectorDense wrapAt(int offset, int size, double... values) {
        Objects.requireNonNull(values);
        return new DVectorDense(offset, size, values);
    }

    /**
     * Builds a vector with values computed by a function given as parameter,
     * where the input values of the function starts at {@code 0} and ends at {@code len - 1}.
     *
     * @param len length of the vector
     * @param fun generating function
     * @return dense vector with computed values
     */
    static DVector from(int len, Int2DoubleFunction fun) {
        return wrapAt(0, len, DoubleArrays.newFrom(0, len, fun));
    }

    /**
     * Number of elements in vector.
     *
     * @return number of elements from the vector
     */
    int size();

    /**
     * Creates a new vector map which map values from specified indexes.
     *
     * @param indexes of the values to keep
     * @return map instance vector
     */
    DVector map(int... indexes);

    /**
     * Creates a new vector map which map values from specified indexes
     * and stores the values into the {@param to} vector.
     *
     * @param indexes of the values to keep
     * @return map instance vector
     */
    DVector mapTo(DVector to, int[] indexes);

    /**
     * Creates a new vector map which map values from specified indexes
     * and store the values into a new vector.
     *
     * @param indexes of the values to keep
     * @return map instance vector
     */
    default DVector mapNew(int... indexes) {
        DVectorDense result = new DVectorDense(indexes.length);
        return mapTo(result, indexes);
    }

    default DVector range(int start, int end) {
        return map(IntArrays.newSeq(start, end));
    }

    default DVector rangeTo(DVector to, int start, int end) {
        return mapTo(to, IntArrays.newSeq(start, end));
    }

    default DVector rangeNew(int start, int end) {
        return mapNew(IntArrays.newSeq(start, end));
    }

    /**
     * Creates a new copy of the vector. The type of the copy vector is the same as original one,
     * with the exception when this is a view vector and a copy using the original type is impossible.
     * In that case a dense copy will be created.
     * <p>
     * There are two common reasons why we would need such an operations:
     *
     * <ul>
     * <li>the current vector could be the result of multiple
     * mapping or binding operations and we would like to have a solid
     * copy of all those values</li>
     * <li>most of the operations work on the current instance, if we want
     * to avoid altering this instance than we need a new copy</li>
     * </ul>
     *
     * @return a new solid copy of the vector
     */
    DVector copy();

    /**
     * Creates a dense copy of the same size.
     *
     * @return new dense vector copy
     */
    default DVectorDense denseCopy() {
        return denseCopy(size());
    }

    /**
     * Creates a dense copy with a new length. If the length is less than the actual size of the vector a new dense copy is created
     * having only the first {@code len} values. If the length is greater or equal with the actual size, a dense vector copy is created
     * having the first {@code len} values as in the original vector, while the remaining values are zero padded.
     *
     * @param len length of new vector
     * @return new dense vector copy
     */
    DVectorDense denseCopy(int len);

    /**
     * Gets value from zero-based position index.
     *
     * @param i given position
     * @return value stored at the given position
     */
    double get(int i);

    /**
     * Sets a value to the given position.
     *
     * @param i     zero based index
     * @param value value to be stored
     */
    void set(int i, double value);

    /**
     * Increments the value at the given position.
     */
    void inc(int i, double value);

    /**
     * Set all vector values to the given value.
     *
     * @param value value to be set
     */
    DVector fill(double value);

    /**
     * Swap values from positions {@code i} and {@code j}.
     *
     * @param i position of the first value.
     * @param j position of the second value.
     */
    void swap(int i, int j);

    /**
     * Swap value from two intervals. First interval starts at position {@code i}, second
     * interval starts at position {@code j}, both intervals having length {@code len}.
     *
     * @param i   start position of the first interval
     * @param j   start postion of the second interval
     * @param len length of the intervals
     */
    void swap(int i, int j, int len);

    DVector log();

    DVector logTo(DVector to);

    default DVector logNew() {
        return logTo(new DVectorDense(size()));
    }

    DVector log1p();

    DVector log1pTo(DVector to);

    default DVector log1pNew() {
        return log1pTo(new DVectorDense(size()));
    }

    DVector log10();

    DVector log10To(DVector to);

    default DVector log10New() {
        return log10To(new DVectorDense(size()));
    }

    DVector abs();

    DVector absTo(DVector to);

    default DVector absNew() {
        return absTo(new DVectorDense(size()));
    }

    DVector neg();

    DVector negTo(DVector to);

    default DVector negNew() {
        return negTo(new DVectorDense(size()));
    }

    DVector cos();

    DVector cosTo(DVector to);

    default DVector cosNew() {
        return cosTo(new DVectorDense(size()));
    }

    DVector cosh();

    DVector coshTo(DVector to);

    default DVector coshNew() {
        return coshTo(new DVectorDense(size()));
    }

    DVector acos();

    DVector acosTo(DVector to);

    default DVector acosNew() {
        return acosTo(new DVectorDense(size()));
    }

    DVector sin();

    DVector sinTo(DVector to);

    default DVector sinNew() {
        return sinTo(new DVectorDense(size()));
    }

    DVector sinh();

    DVector sinhTo(DVector to);

    default DVector sinhNew() {
        return sinhTo(new DVectorDense(size()));
    }

    DVector asin();

    DVector asinTo(DVector to);

    default DVector asinNew() {
        return asinTo(new DVectorDense(size()));
    }

    DVector tan();

    DVector tanTo(DVector to);

    default DVector tanNew() {
        return tanTo(new DVectorDense(size()));
    }

    DVector tanh();

    DVector tanhTo(DVector to);

    default DVector tanhNew() {
        return tanhTo(new DVectorDense(size()));
    }

    DVector atan();

    DVector atanTo(DVector to);

    default DVector atanNew() {
        return atanTo(new DVectorDense(size()));
    }

    DVector exp();

    DVector expTo(DVector to);

    default DVector expNew() {
        return expTo(new DVectorDense(size()));
    }

    DVector expm1();

    DVector expm1To(DVector to);

    default DVector expm1New() {
        return expm1To(new DVectorDense(size()));
    }

    DVector sqrt();

    DVector sqrtTo(DVector to);

    default DVector sqrtNew() {
        return sqrtTo(new DVectorDense(size()));
    }

    DVector cbrt();

    DVector cbrtTo(DVector to);

    default DVector cbrtNew() {
        return cbrtTo(new DVectorDense(size()));
    }

    /**
     * Adds value {@param x} to all vector elements.
     *
     * @param x value to be added
     * @return same vector instance
     */
    DVector add(double x);

    /**
     * Computes the sum between this vector and scalar value {@param x} and store into {@param to} vector.
     *
     * @param x  increment value
     * @param to destination vector
     * @return destination vector
     */
    DVector addTo(DVector to, double x);

    /**
     * Computes the sum between this vector and scalar value {@param x} and store into a new vector.
     *
     * @param x scalar increment value
     * @return new vector which contains the result
     */
    default DVector addNew(double x) {
        return addTo(new DVectorDense(size()), x);
    }

    /**
     * Computes in place the sum between this vector and given vector {@param y}.
     *
     * @param y vector to be added
     * @return same vector instance
     */
    DVector add(DVector y);

    /**
     * Computes the sum between this vector and given vector {@param y} and store the result into vector {@param to}.
     *
     * @param y  vector to be added
     * @param to vector where to store the result
     * @return result vector
     */
    DVector addTo(DVector to, DVector y);

    /**
     * Computes sum between this vector and given vector {@param y} and store the result into a new vector.
     *
     * @param y vector to be added
     * @return new vector which contains the result
     */
    default DVector addNew(DVector y) {
        DVectorDense copy = new DVectorDense(size());
        return addTo(copy, y);
    }

    /**
     * Subtracts value {@param x} from all vector elements.
     *
     * @param x value to be subtracted
     * @return same vector instance
     */
    DVector sub(double x);

    /**
     * Computes the difference between this vector and scalar value {@param x} and store into {@param to} vector.
     *
     * @param x  decrement value
     * @param to destination vector
     * @return destination vector
     */
    DVector subTo(DVector to, double x);

    /**
     * Computes the difference between this vector and scalar value {@param x} and store into a new vector.
     *
     * @param x scalar decrement value
     * @return new vector which contains the result
     */
    default DVector subNew(double x) {
        DVectorDense copy = new DVectorDense(0, size(), new double[size()]);
        return subTo(copy, x);
    }

    /**
     * Computes in place the difference between this vector and given vector {@param y}.
     *
     * @param y vector to be subtracted
     * @return same vector instance
     */
    DVector sub(DVector y);

    /**
     * Computes the difference between this vector and given vector {@param y} and store the result into vector {@param to}.
     *
     * @param y  vector to be subtracted
     * @param to vector where to store the result
     * @return result vector
     */
    DVector subTo(DVector to, DVector y);

    /**
     * Computes the difference between this vector and given vector {@param y} and store result into a new vector.
     *
     * @param y vector to be subtracted
     * @return new vector which contains the result
     */
    default DVector subNew(DVector y) {
        DVectorDense copy = new DVectorDense(size());
        return subTo(copy, y);
    }

    /**
     * Multiply with value {@param x} all vector elements.
     *
     * @param x value to be multiplied with
     * @return same vector instance
     */
    DVector mul(double x);

    /**
     * Computes the product between this vector and scalar value {@param x} and store into {@param to} vector.
     *
     * @param x  multiplier value
     * @param to destination vector
     * @return destination vector
     */
    DVector mulTo(DVector to, double x);

    /**
     * Computes the product between this vector and scalar value {@param x} and store into a new vector.
     *
     * @param x scalar multiplier value
     * @return new vector which contains the result
     */
    default DVector mulNew(double x) {
        DVectorDense copy = new DVectorDense(0, size(), new double[size()]);
        return mulTo(copy, x);
    }

    /**
     * Computes in place the product between this vector and given vector {@param y}.
     *
     * @param y vector to be multiplied with
     * @return same vector instance
     */
    DVector mul(DVector y);

    /**
     * Computes the product between this vector and given vector {@param y} and store the result into vector {@param to}.
     *
     * @param y  vector to be multiplied with
     * @param to vector where to store the result
     * @return result vector
     */
    DVector mulTo(DVector to, DVector y);

    /**
     * Computes the product between this vector and given vector {@param y} and store the result into a new vector.
     *
     * @param y vector to be multiplied with
     * @return new vector which contains the result
     */
    default DVector mulNew(DVector y) {
        DVectorDense copy = new DVectorDense(size());
        return mulTo(copy, y);
    }

    /**
     * Divides with value {@param x} all vector elements.
     *
     * @param x value to be divided with
     * @return same vector instance
     */
    DVector div(double x);

    /**
     * Computes the ratio between this vector and scalar value {@param x} and store into {@param to} vector.
     *
     * @param x  divisor value
     * @param to destination vector
     * @return destination vector
     */
    DVector divTo(DVector to, double x);

    /**
     * Computes the ratio between this vector and scalar value {@param x} and store into a new vector.
     *
     * @param x scalar divisor value
     * @return new vector which contains the result
     */
    default DVector divNew(double x) {
        DVectorDense copy = new DVectorDense(0, size(), new double[size()]);
        return divTo(copy, x);
    }

    /**
     * Computes in place the ratio between this vector and given vector {@param y}.
     *
     * @param y vector to be divided with
     * @return same vector instance
     */
    DVector div(DVector y);

    /**
     * Computes the ratio between this vector and given vector {@param y} and store the result into vector {@param to}.
     *
     * @param y  vector to be divided with
     * @param to vector where to store the result
     * @return result vector
     */
    DVector divTo(DVector to, DVector y);

    /**
     * Computes the ratio between this vector and given vector {@param y} and stores the result into a new vector.
     *
     * @param y vector to be divided with
     * @return new vector which contains the result
     */
    default DVector divNew(DVector y) {
        DVectorDense copy = new DVectorDense(size());
        return divTo(copy, y);
    }

    /**
     * Adds to the current vector multiple of the given vector {@code this = this + a * y},
     * where {@code a} is a double scalar and {@code this} and {@code y} are conformant
     * double vectors. The {@code this} vector in expression is the vector on which
     * the operation is called.
     *
     * @param a scalar
     * @param y vector
     * @return new vector which contains the result of {@code this[i] <- this[i] + a * y[i]}
     */
    DVector fma(double a, DVector y);

    /**
     * Creates a new {@link DVector} which contains the result of {@code this + a * y},
     * where {@code a} is a double scalar and {@code this} and {@code y} are conformant
     * double vectors. The {@code this} vector in expression is the vector on which
     * the operation is called.
     *
     * @param a scalar
     * @param y vector
     * @return new vector which contains the result of {@code this[i] <- this[i] + a * y[i]}
     */
    DVector fmaNew(double a, DVector y);

    /**
     * Cuts the vector values to be in interval [low,high]. In other words a value
     * {@code v} is transformed into {@code max(low, min(high, v))}.
     * <p>
     * If {@code low} is {@link Double#NaN} than values are not cut downside.
     * <p>
     * If {@code high} is {@link Double#NaN} than values are not cut upside.
     *
     * @param low  minimum value after cut operation, if {@link Double#NaN} minimum is not applied.
     * @param high maximum value after cut operation, if {@link Double#NaN} maximum is not applied.
     * @return initial vector with modified values
     */
    DVector cut(double low, double high);

    /**
     * Store into {@code to} vector the cut values in interval [low,high]. In other words a value
     * {@code v} is transformed into {@code max(low, min(high, v))}.
     * <p>
     * If {@code low} is {@link Double#NaN} than values are not cut downside.
     * <p>
     * If {@code high} is {@link Double#NaN} than values are not cut upside.
     *
     * @param to   vector which will store the values
     * @param low  minimum value after cut operation, if {@link Double#NaN} minimum is not applied.
     * @param high maximum value after cut operation, if {@link Double#NaN} maximum is not applied.
     * @return {@code to} vector with modified values
     */
    DVector cutTo(DVector to, double low, double high);

    /**
     * Store into new vector the cut values in interval [low,high]. In other words a value
     * {@code v} is transformed into {@code max(low, min(high, v))}.
     * <p>
     * If {@code low} is {@link Double#NaN} than values are not cut downside.
     * <p>
     * If {@code high} is {@link Double#NaN} than values are not cut upside.
     *
     * @param low  minimum value after cut operation, if {@link Double#NaN} minimum is not applied.
     * @param high maximum value after cut operation, if {@link Double#NaN} maximum is not applied.
     * @return {@code to} vector with modified values
     */
    default DVector cutNew(double low, double high) {
        return cutTo(denseCopy(), low, high);
    }

    /**
     * Dot product between two vectors is equal to the sum of the
     * product of elements from each given position.
     * <p>
     * sum_{i=1}^{n}a_i*b_i
     *
     * @param y the vector used to compute dot product
     * @return same vector object
     */
    double dot(DVector y);

    /**
     * Computes bilinear dot product through a matrix {@code x^t m y}. Matrix {@code m} have to be conformat for multiplication.
     *
     * @param m bilinear matrix
     * @param y bilinear vector
     * @return bilinear dot product scalar value
     */
    double dotBilinear(DMatrix m, DVector y);

    /**
     * Computes self bilinear dot product through a matrix {@code x^t m x}.Matrix {@code m} must be squared and conform
     * to multiplication.
     *
     * @param m bilinear matrix
     * @return bilinear dot product scalar value
     */
    double dotBilinear(DMatrix m);

    /**
     * Computes bilinear dot product through a diagonal matrix {@code x^t diag(m) y}.
     * Matrix {@code m} has to be conform for multiplication.
     * If the matrix is not diagonal, only the diagonal elements are used.
     *
     * @param m bilinear matrix
     * @param y bilinear vector
     * @return bilinear diagonal dot product
     */
    double dotBilinearDiag(DMatrix m, DVector y);

    /**
     * Computes self bilinear dot product through a diagonal matrix {@code x^t diag(m) x}.
     * Matrix {@code m} have to be conformat for multiplication. If the matrix is not diagonal, only the diagonal elements are used.
     *
     * @param m bilinear matrix
     * @return bilinear diagonal dot product
     */
    double dotBilinearDiag(DMatrix m);

    /**
     * Computes bilinear dot product through a diagonal matrix {@code x^t diag(m) y}.
     * Vector {@code m} have to be conformat for multiplication. If the matrix {@code diag(m)} is the matrix with elements on the main
     * diagonal set to elements of vector {@code m} and other elements equals {@code 0}.
     *
     * @param m bilinear matrix
     * @param y bilinear vector
     * @return bilinear diagonal dot product
     */
    double dotBilinearDiag(DVector m, DVector y);

    /**
     * Computes self bilinear dot product through a diagonal matrix {@code x^t diag(m) x}.
     * Matrix {@code m} have to be conformat for multiplication. If the matrix is not diagonal, only the diagonal elements are used.
     *
     * @param m bilinear matrix
     * @return bilinear diagonal dot product
     */
    double dotBilinearDiag(DVector m);

    DMatrix outer(DVector b);

    /**
     * Computes the p norm of the vector.
     * <ul>
     * <li>if <b>p == 1</b> it returns the absolute value norm (L1 norm)</li>
     * <li>if <b>p == 2</b> it returns the euclidean norm (L2 norm)</li>
     * <li>if <b>p == Inf</b> it returns the value of the biggest element</li>
     * <li>in general it returns p-norm
     * </ul>
     *
     * @param p the order of the norm
     * @return computed p norm value
     */
    double norm(double p);

    /**
     * Divides all the values by the given p norm. Thus, after normalization
     * the specific p norm is equal with 1.
     * <p>
     * An example of usage is to make a unit vector from a given vector.
     * Thus the normalized vector keeps the same direction with a different size.
     * <p>
     * If the p-norm equals 0, than the vector is kept the same.
     *
     * @param p order of the p norm used at normalization.
     * @return normalized vector
     */
    default DVector normalize(double p) {
        return div(norm(p));
    }

    /**
     * Divides all the values by the given p norm. Thus, after normalization
     * the specific p norm is equal with 1 and stores the result into a new vector.
     * <p>
     * An example of usage is to make a unit vector from a given vector.
     * Thus the normalized vector keeps the same direction with a different size.
     * <p>
     * If the p-norm equals 0, than the vector is kept the same.
     *
     * @param p order of the p norm used at normalization.
     * @return normalized vector
     */
    default DVector normalizeNew(double p) {
        return divNew(norm(p));
    }

    /**
     * Computes the sum of all elements in vector. If there is
     * at least one NaN value, the computed sum is NaN.
     *
     * @return sum of all elements in the vector
     */
    double sum();

    /**
     * Sum of all non missing values (Double.NaN is considered missing value). Note that if all elements are missing, then
     * the computed sum equals 0.
     *
     * @return sum of all non missing elements.
     */
    double nansum();

    /**
     * Compute cumulative sum of the elements from beginning to end.
     *
     * @return original vector with values computed from cumulative sum
     */
    DVector cumsum();

    /**
     * Computes the product of all elements in vector. If there is
     * at least one NaN value, the computed sum is NaN.
     *
     * @return multiply of all elements in the vector
     */
    double prod();

    /**
     * Product of all non-missing values (Double.NaN is considered missing value).
     * Note that if all elements are missing, then
     * the computed prod equals 1.
     *
     * @return sum of all non missing elements.
     */
    double nanprod();

    /**
     * Compute the cumulative product of the vector elements.
     *
     * @return original vector with updated values
     */
    DVector cumprod();

    /**
     * Computes count of non-missing values.
     *
     * @return count of non-missing values
     */
    int nancount();

    /**
     * Computes a sample mean object where the sample values
     * consists of the elements of the vector.
     *
     * @return mean result
     */
    double mean();

    /**
     * Computes non-missing (non NaN) values from the vector.
     *
     * @return mean of non missing values
     */
    double nanmean();

    /**
     * Computes a sample variance object where the
     * sample values consists of the elements of the vector.
     *
     * @return the sample variance
     */
    double variance();

    /**
     * Computes sample variance ignoring NaN missing values.
     *
     * @return sample variance value
     */
    double nanvariance();

    /**
     * Returns the index of the element with minimum value.
     * If there are multiple elements with the same minimum value, the first index is returned.
     *
     * @return index of the minimum element
     */
    int argmin();

    /**
     * Returns the minimum value from vector.
     *
     * @return minimum value from vector
     */
    double min();

    /**
     * Returns the index of the element with maximum value.
     * If there are multiple elements with the same maximum value, the first index is returned.
     *
     * @return index of the maximum element
     */
    int argmax();

    /**
     * Returns the maximum value from vector.
     *
     * @return maximum value from vector
     */
    double max();

    /**
     * Apply a double to double function on all the values from the vector.
     *
     * @param f double to double function
     * @return result vector
     */
    DVector apply(Double2DoubleFunction f);

    /**
     * Apply a double to double function on all values from the vector and stores results into {@param to} vector.
     *
     * @param f  function to be applied
     * @param to result vector
     * @return result vector
     */
    DVector applyTo(DVector to, Double2DoubleFunction f);

    /**
     * Apply a double to double function on all the values from the vector and stores results into a new vector.
     *
     * @param f double to double function
     * @return result vector
     */
    default DVectorDense applyNew(Double2DoubleFunction f) {
        DVectorDense copy = new DVectorDense(size());
        return (DVectorDense) applyTo(copy, f);
    }

    /**
     * Apply an (integer,double) to double function on all the values from the vector.
     * The integer value is the position of the value in the vector.
     *
     * @param f (int,double) to double function
     * @return result vector
     */
    DVector apply(BiFunction<Integer, Double, Double> f);

    /**
     * Apply an (integer,double) to double function on all the values from the vector
     * and stores results into {@param to} vector.
     * <p>
     * The integer value is the position of the value in the vector.
     *
     * @param f (int,double) to double function
     * @return result vector
     */
    DVector applyTo(DVector to, BiFunction<Integer, Double, Double> f);

    /**
     * Apply an (integer,double) to double function on all the values from the vector
     * and stores results into a new vector.
     * <p>
     * The integer value is the position of the value in the vector.
     *
     * @param f (int,double) to double function
     * @return result vector
     */
    default DVectorDense applyNew(BiFunction<Integer, Double, Double> f) {
        DVectorDense copy = new DVectorDense(size());
        return (DVectorDense) applyTo(copy, f);
    }

    /**
     * Sort values from vector.
     *
     * @return same vector or a new vector with sorted values
     */
    default DVector sortValues() {
        return sortValues(true);
    }

    /**
     * Sort values from vector and stores result into a new vector
     *
     * @return same vector or a new vector with sorted values
     */
    default DVector sortValuesNew() {
        return sortValuesNew(true);
    }

    /**
     * Sort values from vector in place.
     *
     * @param asc ascending sort if {@code true}, descending otherwise
     * @return same vector or a new vector with sorted values
     */
    default DVector sortValues(boolean asc) {
        return sortValues(asc ? DoubleComparators.NATURAL_COMPARATOR : DoubleComparators.OPPOSITE_COMPARATOR);
    }

    /**
     * Sort values from vector and store result into a new vector.
     *
     * @param asc ascending sort if {@code true}, descending otherwise
     * @return same vector or a new vector with sorted values
     */
    default DVector sortValuesNew(boolean asc) {
        return sortValuesNew(asc ? DoubleComparators.NATURAL_COMPARATOR : DoubleComparators.OPPOSITE_COMPARATOR);
    }

    /**
     * Sort values from vector in place.
     *
     * @param comp double value comparator
     * @return same vector or a new vector with sorted values
     */
    DVector sortValues(DoubleComparator comp);

    /**
     * Sort values from vector and stores result into a new vector.
     *
     * @param comp double value comparator
     * @return same vector or a new vector with sorted values
     */
    DVector sortValuesNew(DoubleComparator comp);

    /**
     * Sort in place the integer indexes in ascending order of the double values.
     *
     * @param indexes indexes to be sorted
     */
    default void sortIndexes(int[] indexes) {
        sortIndexes(true, indexes);
    }

    /**
     * Sort in place the integer indexes in ascending or descending order of the double values.
     *
     * @param asc     ascending order if true, descending otherwise
     * @param indexes indexes to be sorted
     */
    default void sortIndexes(boolean asc, int[] indexes) {
        sortIndexes(asc ? DoubleComparators.NATURAL_COMPARATOR : DoubleComparators.OPPOSITE_COMPARATOR, indexes);
    }

    /**
     * Sort in place the integer indexes in the order defined by given comparator over the double values
     *
     * @param comp    double value comparator
     * @param indexes indexes to be sorted
     */
    void sortIndexes(DoubleComparator comp, int[] indexes);

    /**
     * A vector is also a matrix, but for implementation reasons the objects are not the same. This method
     * creates a new copy of the vector in the form of a matrix with {@code n} rows and {@code 1} column.
     *
     * @return a matrix corresponding with the current vector
     */
    DMatrixDenseC asMatrix();

    /**
     * Creates a stream of values to visit all the elements of the vector.
     *
     * @return a stream of values
     */
    DoubleStream valueStream();

    @Override
    default void forEach(Consumer<? super Double> action) {
        for (int i = 0; i < size(); i++) {
            action.accept(get(i));
        }
    }

    @Override
    Iterator<Double> iterator();

    /**
     * Creates a VarDouble by wrapping the values if possible (if the vector storage type is
     * a direct one). Otherwise, a new copy of the data is created and a VarDouble wraps the data array.
     *
     * @return new double variable instance
     */
    VarDouble dv();

    /**
     * Compares the values between the vector given as parameter and the current one.
     * Since we work with doubles, the comparison of two values returns true if their
     * absolute difference is less than a default tolerance threshold value of {@code 1e-12}.
     *
     * @param v comparison vector
     * @return true if the size and values are equal, false otherwise
     */
    default boolean deepEquals(DVector v) {
        return deepEquals(v, 1e-12);
    }

    /**
     * Compares the values between the vector given as parameter and the current one.
     * Since we work with doubles, the comparison of two values returns true if their
     * absolute difference is less than a tolerance value {@code eps}.
     *
     * @param v   comparison vector
     * @param eps tolerance threshold for the absolute difference between values
     * @return true if the size and values are equal, false otherwise
     */
    boolean deepEquals(DVector v, double eps);
}
