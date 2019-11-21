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

import rapaio.core.stat.Variance;
import rapaio.data.VarDouble;
import rapaio.math.linear.dense.SolidRM;
import rapaio.printer.Printable;

import java.io.Serializable;
import java.util.function.Function;
import java.util.stream.DoubleStream;

/**
 * Vector of values in double floating precision.
 * <p>
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 2/3/16.
 */
public interface RV extends Serializable, Printable {

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
     * Increment with the given value the value from the given position.
     *
     * @param i     zero-based index position
     * @param value value used for increment
     */
    void increment(int i, double value);

    /**
     * @return length of vector
     */
    int count();

    /**
     * Scalar multiplication. All the values from vector
     * will be multiplied with the given scalar
     *
     * @param scalar scaar value
     * @return the same object
     */
    RV dot(double scalar);

    /**
     * Adds to all elements the value of x, it is
     * similar wtth calling increment with x for all positions
     * of the vector
     *
     * @param x value to be incremented with
     * @return same object
     */
    default RV plus(double x) {
        for (int i = 0; i < count(); i++) {
            increment(i, x);
        }
        return this;
    }

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
    default RV plus(RV B) {
        if (count() != B.count())
            throw new IllegalArgumentException(String.format(
                    "Vectors are not conform for addition: [%d] + [%d]", count(), B.count()));
        for (int i = 0; i < count(); i++) {
            increment(i, B.get(i));
        }
        return this;
    }

    /**
     * Substract from all elements the value of x, it is
     * similar wtth calling increment with -x for all positions
     * of the vector
     *
     * @param x value to be decremented with
     * @return same object
     */
    default RV minus(double x) {
        return plus(-x);
    }

    /**
     * Substract from all positions values from the
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
    default RV minus(RV B) {
        if (count() != B.count())
            throw new IllegalArgumentException(String.format(
                    "Matrices are not conform for substraction: [%d] + [%d]", count(), B.count()));
        for (int i = 0; i < count(); i++) {
            increment(i, -B.get(i));
        }
        return this;
    }

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
    RV normalize(double p);

    /**
     * Dot product between two vectors is equal to the sum of the
     * product of elements from each given position.
     * <p>
     * sum_{i=1}^{n}a_i*b_i
     *
     * @param b the vector used to compute dot product
     * @return same vector object
     */
    default double dotProd(RV b) {
        int max = Math.max(count(), b.count());
        double s = 0;
        for (int i = 0; i < max; i++) {
            s += get(i) * b.get(i);
        }
        return s;
    }

    /**
     * Computes a sample mean object where the sample values
     * consists of the elements of the vector.
     *
     * @return mean result
     */
    default double mean() {
        return asNumericVar().op().avg();
    }

    default double sum() {
        return asNumericVar().op().sum();
    }

    default RV apply(Function<Double, Double> f) {
        for (int i = 0; i < count(); i++) {
            set(i, f.apply(get(i)));
        }
        return this;
    }

    /**
     * Computes a sample variance object where the
     * sample values consists of the elements of the vector.
     *
     * @return the sample variance object
     */
    default Variance variance() {
        VarDouble values = VarDouble.empty();
        for (int i = 0; i < count(); i++) {
            values.addDouble(get(i));
        }
        return Variance.of(values);
    }

    /**
     * Creates a new solid copy of the vector.
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
    RV copy();

    /**
     * A vector is also a matrix, but for implementation
     * reasons the objects are not the same. This method
     * creates a new copy of the vector in the form of a matrix
     * with n rows and 1 column.
     *
     * @return a matrix corresponding with the current vector
     */
    default RM asMatrix() {
        SolidRM res = SolidRM.empty(count(), 1);
        for (int i = 0; i < count(); i++) {
            res.set(i, 0, get(i));
        }
        return res;
    }

    /**
     * A vector is also a matrix, but for implementation
     * reasons the objects are not the same. This method
     * creates a new copy of the vector in the form of a transposed matrix
     * with 1 rows and n columns.
     *
     * @return a matrix corresponding with the current transposed vector
     */
    default RM asMatrixT() {
        SolidRM res = SolidRM.empty(1, count());
        for (int i = 0; i < count(); i++) {
            res.set(0, i, get(i));
        }
        return res;
    }

    /**
     * Creates a stream of values to visit all the elements of the vector
     *
     * @return a stream of values
     */
    DoubleStream valueStream();


    VarDouble asNumericVar();
}
