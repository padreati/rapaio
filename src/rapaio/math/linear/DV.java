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

import rapaio.data.VarDouble;
import rapaio.math.linear.interfaces.DVMathOps;
import rapaio.printer.Printable;

import java.io.Serializable;
import java.util.stream.DoubleStream;

/**
 * Vector of values in double floating precision.
 * <p>
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 2/3/16.
 */
public interface DV extends DVMathOps, Serializable, Printable {

    enum Type {

        BASE,
        DENSE,
        VIEW
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
    void increment(int i, double value);

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
    default DV copy() {
        return copy(type());
    }

    DV copy(Type type);

    /**
     * A vector is also a matrix, but for implementation
     * reasons the objects are not the same. This method
     * creates a new copy of the vector in the form of a matrix
     * with n rows and 1 column.
     *
     * @return a matrix corresponding with the current vector
     */
    DM asMatrix();

    /**
     * Creates a stream of values to visit all the elements of the vector
     *
     * @return a stream of values
     */
    DoubleStream valueStream();

    VarDouble asVarDouble();

    boolean deepEquals(DV v);
}
