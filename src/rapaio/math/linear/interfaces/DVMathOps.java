package rapaio.math.linear.interfaces;

import it.unimi.dsi.fastutil.doubles.Double2DoubleFunction;
import rapaio.math.linear.DV;

import java.util.function.BiFunction;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 8/19/20.
 */
public interface DVMathOps {

    /**
     * Adds to all elements the value of x
     *
     * @param x value to be incremented with
     * @return same object
     */
    DV plus(double x);

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
    DV plus(DV B);

    /**
     * Substracts from all elements the value of x, it is
     * similar wtth calling increment with -x for all positions
     * of the vector
     *
     * @param x value to be decremented with
     * @return same object
     */
    DV minus(double x);

    /**
     * Substracts from all positions values from the
     * corresponding positions of the vector B.
     * The resulted vectors will have values:
     * this[i] <- this[i] + B[i].
     * <p>
     * Vectors must be conformant for addition, which means
     * that they have to have the same size.
     *
     * @param b vector which contains values used for increment operation
     * @return same object
     */
    DV minus(DV b);

    /**
     * Scalar multiplication. All the values from vector
     * will be multiplied with the given scalar
     *
     * @param scalar scaar value
     * @return the same object
     */
    DV times(double scalar);

    /**
     * Element wise multiplication between two vectors.
     *
     * @param b factor vector
     * @return element wise multiplication result vector
     */
    DV times(DV b);

    /**
     * Scalar division. All values from vector will be divided by scalar value.
     *
     * @param scalar value
     * @return reference to original vector
     */
    DV div(double scalar);

    /**
     * Element wise division between two vectors.
     *
     * @param b factor vector
     * @return element wise division result vector
     */
    DV div(DV b);

    /**
     * Dot product between two vectors is equal to the sum of the
     * product of elements from each given position.
     * <p>
     * sum_{i=1}^{n}a_i*b_i
     *
     * @param b the vector used to compute dot product
     * @return same vector object
     */
    double dot(DV b);

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
    DV normalize(double p);

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

    DV apply(Double2DoubleFunction f);

    DV apply(BiFunction<Integer, Double, Double> f);
}
