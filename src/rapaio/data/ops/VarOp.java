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

import it.unimi.dsi.fastutil.doubles.Double2DoubleFunction;
import it.unimi.dsi.fastutil.ints.IntComparator;
import rapaio.data.Var;
import rapaio.data.VarDouble;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 7/12/19.
 */
public interface VarOp<T extends Var> {

    T apply(Double2DoubleFunction fun);

    VarDouble capply(Double2DoubleFunction fun);

    double sum();

    double avg();

    T plus(double a);

    T plus(Var x);

    T minus(double a);

    T minus(Var x);

    T mult(double a);

    T mult(Var x);

    T divide(double a);

    T divide(Var x);

    T sort(IntComparator comparator);

    default T sort() {
        return sort(true);
    }

    T sort(boolean asc);

    default int[] sortedCompleteRows() {
        return sortedCompleteRows(true);
    }

    int[] sortedCompleteRows(boolean asc);

    default int[] sortedRows() {
        return sortedRows(true);
    }

    int[] sortedRows(boolean asc);
}
