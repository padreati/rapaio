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

package rapaio.data.ops;

import rapaio.data.Var;
import rapaio.data.VarDouble;
import rapaio.util.collection.IntComparator;
import rapaio.util.function.DoubleDoubleFunction;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 7/12/19.
 */
public interface DVarOp<T extends Var> {

    /**
     * Update values through a double to double function.
     * The update is realized in place.
     *
     * @param fun transformation function
     * @return reference to the original variable
     */
    T apply(DoubleDoubleFunction fun);

    /**
     * Creates a VarDouble copy with transformed values.
     *
     * @param fun transformation function
     * @return new variable with transformed values
     */
    VarDouble capply(DoubleDoubleFunction fun);

    /**
     * Computes the sum of non missing values.
     *
     * @return sum of non missing values
     */
    double nansum();

    /**
     * Computes the average of non missing values.
     * The counted values are only the non missing ones.
     *
     * @return average of non missing values
     */
    double nanmean();

    /**
     * Adds a constant to all values from the variable.
     *
     * @param a constant values to be added
     * @return reference to the original variable
     */
    T plus(double a);

    /**
     * Adds elementwise a variable to the original variable in place
     *
     * @param x variable with values to be added to original
     * @return reference to the original variable
     */
    T plus(Var x);

    /**
     * Substract a constant value from all values from the variable.
     *
     * @param a constant value to be substracted
     * @return reference to the original variable
     */
    T minus(double a);

    /**
     * Substract elementwise a variable from the original variable.
     * Operation is realized in place.
     *
     * @param x variable to be substracted from original
     * @return reference to the original variable
     */
    T minus(Var x);

    /**
     * Multiply all values of the original variables with a constant value.
     *
     * @param a constant multiplier
     * @return reference to the original variable
     */
    T mult(double a);

    /**
     * Multiply elementwise a variable with the original variable.
     * Operation is realized in place.
     *
     * @param x variable to be multiplied with original
     * @return reference to the original variable
     */
    T mult(Var x);

    /**
     * Divide all values from the original variable with a constant value.
     *
     * @param a a constant value divider
     * @return reference to the original variable.
     */
    T divide(double a);

    /**
     * Divide elementwise the original variable with values from given variable.
     *
     * @param x divider variable
     * @return reference to the original variable
     */
    T divide(Var x);

    /**
     * Sort in place values of the original variable using a row comparator.
     *
     * @param comparator row comparator
     * @return reference to the original variable
     */
    T sort(IntComparator comparator);

    /**
     * Sort in place values of the original variable using a natural value comparator.
     * The ordering is ascending.
     *
     * @return reference to the original variable
     */
    default T sort() {
        return sort(true);
    }

    /**
     * Sort in place values of the original variable using a natural value comparator.
     * The oridering is given as parameter.
     *
     * @param asc if true the ordering is ascending, descending otherwise
     * @return reference to the original variable
     */
    T sort(boolean asc);

    /**
     * Computes the row array of the sorted non missing values in ascending order.
     *
     * @return an array with row numbers
     */
    default int[] sortedCompleteRows() {
        return sortedCompleteRows(true);
    }

    /**
     * Computes the row array of the sorted non missing values with ordering
     * given as parameter.
     *
     * @param asc if true the ordering is ascending, descending otherwise
     * @return an array with row numbers
     */
    int[] sortedCompleteRows(boolean asc);

    /**
     * Computes the row array of the sorted values with ascending ordering
     *
     * @return an array with row numbers
     */
    default int[] sortedRows() {
        return sortedRows(true);
    }

    /**
     * Computes the row array of the sorted values with ordering given
     * as parameter.
     *
     * @param asc if true the ordering is ascending, descending otherwise
     * @return an array with row numbers
     */
    int[] sortedRows(boolean asc);
}
