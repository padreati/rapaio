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

import rapaio.data.VarDouble;
import rapaio.math.linear.dense.DVectorDense;
import rapaio.printer.Printable;
import rapaio.util.function.Double2DoubleFunction;

import java.io.Serializable;
import java.util.function.BiFunction;
import java.util.stream.DoubleStream;

/**
 * Vector of values in double floating precision.
 * <p>
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 2/3/16.
 */
public interface DVector extends Serializable, Printable {

    enum Type {
        BASE,
        DENSE,
        VIEW
    }

    default boolean isDense() {
        return this instanceof DVectorDense;
    }

    default DVectorDense asDense() {
        return (DVectorDense) this;
    }

    /**
     * Implementation type of the vector class
     *
     * @return vector type
     */
    Type type();

    /**
     * @return number of elements from the vector
     */
    int size();

    /**
     * Creates a new vector copy retaining only the values from specified indexes
     *
     * @param indexes of the values to keep
     * @return reduced instance vector
     */
    DVector mapCopy(int... indexes);

    /**
     * Gets value from zero-based position index
     *
     * @param i given position
     * @return value stored at the given position
     */
    double get(int i);

    /**
     * Sets a value to the given position
     *
     * @param i     zero based index
     * @param value value to be stored
     */
    void set(int i, double value);

    /**
     * Increments the value at the given position
     */
    void inc(int i, double value);

    /**
     * Adds to all elements the value of x
     *
     * @param x value to be incremented with
     * @return same object
     */
    DVector add(double x);

    /**
     * Adds to to all positions values from the
     * corresponding positions of the vector B.
     * The resulted vectors will have values:
     * this[i] <- this[i] + B[i].
     * <p>
     * Vectors must be conformant for addition, which means
     * that they have to have the same size.
     *
     * @param B vector which contains values used for increment operation
     * @return same object
     */
    DVector add(DVector B);

    /**
     * Substracts from all elements the value of x, it is
     * similar wtth calling increment with -x for all positions
     * of the vector
     *
     * @param x value to be decremented with
     * @return same object
     */
    DVector sub(double x);

    /**
     * Subtracts from all positions values from the corresponding positions of the vector {@code b}.
     * The resulted vectors will have values:  {@code this[i] <- this[i] + B[i]}.
     * <p>
     * Vectors must be conformant for addition, which means that they have to have the same size.
     *
     * @param b vector which contains values used for increment operation
     * @return same object
     */
    DVector sub(DVector b);

    /**
     * Scalar multiplication. All the values from vector
     * will be multiplied with the given scalar
     *
     * @param scalar scaar value
     * @return the same object
     */
    DVector mult(double scalar);

    /**
     * Element wise multiplication between two vectors.
     *
     * @param b factor vector
     * @return element wise multiplication result vector
     */
    DVector mult(DVector b);

    /**
     * Scalar division. All values from vector will be divided by scalar value.
     *
     * @param scalar value
     * @return reference to original vector
     */
    DVector div(double scalar);

    /**
     * Element wise division between two vectors.
     *
     * @param b factor vector
     * @return element wise division result vector
     */
    DVector div(DVector b);

    /**
     * Creates a new {@link DVector} which contains the result of {@code a*this+y},
     * where {@code a} is a double scalar and {@code this} and {@code y} are conformant
     * double vectors. The {@code this} vector in expresion is the vector on which
     * the operation is called.
     *
     * @param a scalar
     * @param y vector added to the result
     * @return new vector which contains the result of {@code a*this+y}
     */
    DVector caxpy(double a, DVector y);

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
     * Computes self bilinear dot product through a matrix {@code x^t m x}.Matrix {@code m} must be squared and conformant
     * to multiplication.
     *
     * @param m bilinear matrix
     * @return bilinear dot product scalar value
     */
    double dotBilinear(DMatrix m);

    /**
     * Computes bilinear dot product through a diagonal matrix {@code x^t diag(m) y}.
     * Matrix {@code m} have to be conformat for multiplication. If the matrix is not diagonal, only the diagonal elements are used.
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
    DVector normalize(double p);

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
     * Product of all non missing values (Double.NaN is considered missing value).
     * Note that if all elements are missing, then
     * the computed prod equals 1.
     *
     * @return sum of all non missing elements.
     */
    double nanprod();

    /**
     * Compute the cumulative product of the vector elements
     *
     * @return original vector with updated values
     */
    DVector cumprod();

    /**
     * Computes count of non missing values
     *
     * @return count of non missing values
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
     * Computes non missing (non NaN) values from the vector
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

    DVector apply(Double2DoubleFunction f);

    DVector apply(BiFunction<Integer, Double, Double> f);

    /**
     * Creates a new copy of the vector.
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
    default DVector copy() {
        return copy(type());
    }

    DVector copy(Type type);

    default DMatrix asMatrix() {
        return asMatrix(SOrder.R);
    }

    /**
     * A vector is also a matrix, but for implementation
     * reasons the objects are not the same. This method
     * creates a new copy of the vector in the form of a matrix
     * with n rows and 1 column.
     *
     * @return a matrix corresponding with the current vector
     */
    DMatrix asMatrix(SOrder order);

    /**
     * Creates a stream of values to visit all the elements of the vector
     *
     * @return a stream of values
     */
    DoubleStream valueStream();

    VarDouble asVarDouble();

    default boolean deepEquals(DVector v) {
        return deepEquals(v, 1e-12);
    }

    boolean deepEquals(DVector v, double eps);
}
