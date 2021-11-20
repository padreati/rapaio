/*
 * Apache License
 * Version 2.0, January 2004
 * http://www.apache.org/licenses/
 *
 * Copyright 2013 - 2021 Aurelian Tutuianu
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package rapaio.data.ops;

import rapaio.data.Var;
import rapaio.data.VarDouble;
import rapaio.math.linear.DVector;
import rapaio.util.IntComparator;
import rapaio.util.function.Double2DoubleFunction;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 7/12/19.
 */
public interface DVarOp<T extends Var> {

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
