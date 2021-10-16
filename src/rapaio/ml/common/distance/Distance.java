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

package rapaio.ml.common.distance;

import rapaio.data.Frame;
import rapaio.math.linear.DVector;

/**
 * Interface which describes ways to compute metric distances. The API allows
 * computing distances on multiple types of objects like vectors, rows from data frames and
 * can be extended further. Various standard metric distances are implemented.
 * <p>
 * A metric distance is a function defined on a set S, {@code d:SxS->[0,inf)} and obey the following properties:
 * <p>
 * 1. distance(x,y) = 0  => x=y - identity of indiscernible
 * <p>
 * 2. distance(x,y) = distance(y,x) - symmetry
 * <p>
 * 3. distance(x,y) >= distance(x,z)+distance(y,z) - triangle inequality
 */
public interface Distance {

    String name();

    /**
     * Compute distance between two double vectors {@link DVector}
     *
     * @param x first parameter
     * @param y second parameter
     * @return computed distance
     */
    double compute(DVector x, DVector y);

    /**
     * Compute distance between two rows from two different data frames.
     *
     * @param df1  first data frame
     * @param row1 row from first data frame
     * @param df2  second data frame
     * @param row2 row from second data frame
     * @return computed distance
     */
    double compute(Frame df1, int row1, Frame df2, int row2);

    /**
     * Compute reduced distance between two double vectors {@link DVector}.
     * Reduced distance is a faster way to compute a similar distance which preserves ranks
     * and when the absolute value of the distance is not required, the reduced calculation can be used
     * instead.
     * <p>
     * For example the Euclidean distance requires a square root which can be costly, leaving the value
     * without taking square is faster and can be used instead.
     *
     * @param x first parameter
     * @param y second parameter
     * @return computed reduced distance
     */
    double reduced(DVector x, DVector y);

    /**
     * Compute reduced distance between two rows from different data frames.
     * <p>
     * Reduced distance is a faster way to compute a similar distance which preserves ranks
     * and when the absolute value of the distance is not required, the reduced calculation can be used
     * instead.
     * <p>
     * For example the Euclidean distance requires a square root which can be costly, leaving the value
     * without taking square is faster and can be used instead.
     *
     * @param df1  first data frame
     * @param row1 row from first data frame
     * @param df2  second data frame
     * @param row2 row from second data frame
     * @return computed reduced distance
     */
    double reduced(Frame df1, int row1, Frame df2, int row2);
}
