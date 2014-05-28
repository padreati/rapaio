/*
 * Apache License
 * Version 2.0, January 2004
 * http://www.apache.org/licenses/
 *
 *    Copyright 2013 Aurelian Tutuianu
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
 */

package rapaio.data;

import rapaio.data.mapping.Mapping;
import rapaio.data.stream.VSpots;

import java.io.Serializable;

/**
 * Random access list of observed values (observations) for a specific variable.
 *
 * @author Aurelian Tutuianu
 */
public interface Vector extends Serializable {

    VectorType type();

    boolean isMappedVector();

    Vector source();

    Mapping mapping();

    /**
     * Number of observations contained by the vector.
     *
     * @return size of vector
     */
    int rowCount();

    /**
     * Returns observation identifier which is an integer.
     * <p>
     * When a vector or frame is created from scratch as a solid vector/frame then
     * row identifiers are the row numbers. When the vector/frame wraps other
     * vector/frame then row identifier is the wrapped row identifier.
     * <p>
     * This is mostly used to keep track of the original row numbers even after a series
     * of transformations which use wrapped vectors/frames.
     *
     * @param row row for which row identifier is returned
     * @return row identifier
     */
    int rowId(int row);

    /**
     * Returns numeric value for the observation specified by row.
     * <p>
     * Returns valid values for numerical vector types, otherwise the method
     * returns unspecified value.
     *
     * @param row position of the observation
     * @return numerical setValue
     */
    double value(int row);

    /**
     * Set numeric setValue for the observation specified by {@param row} to {@param setValue}.
     * <p>
     * Returns valid values for numerical vector types, otherwise the method
     * returns unspeified values.
     *
     * @param row   position of the observation
     * @param value numeric setValue from position {@param row}
     */
    void setValue(int row, double value);


    void addValue(double value);

    /**
     * Returns index setValue for the observation specified by {@param row}
     *
     * @param row position of the observation
     * @return index setValue
     */
    int index(int row);

    /**
     * Set index setValue for the observation specified by {@param row}.
     *
     * @param row   position of the observation
     * @param value index setValue for the observation
     */
    void setIndex(int row, int value);

    void addIndex(int value);

    /**
     * Returns nominal label for the observation specified by {@param row}.
     *
     * @param row position of the observation
     * @return label setValue for the observation
     */
    String label(int row);

    /**
     * Set nominal label for the observation specified by {@param row}.
     *
     * @param row   position of the observation
     * @param value label setValue of the observation
     */
    void setLabel(int row, String value);

    void addLabel(String value);

    /**
     * Returns the term dictionary used by the nominal values.
     * <p>
     * Term dictionary contains all the nominal labels used by
     * observations and might contain also additional nominal labels.
     * Term dictionary defines the domain of the definition for the nominal vector.
     * <p>
     * The term dictionary contains nominal labels sorted in lexicografical order,
     * so binary search techniques may be used on this vector.
     * <p>
     * For other vector types like numerical ones this method returns nothing.
     *
     * @return term dictionary defined by the nominal vector.
     */
    String[] dictionary();

    void setDictionary(String[] dict);

    /**
     * Returns true if the setValue for the observation specified by {@param row} is missing, not available.
     * <p>
     * A missing setValue for the observation means taht the measurement
     * was not completed or the result of the measurement was not documented,
     * thus the setValue is not available for analysis.
     *
     * @param row position of the observation
     * @return true if the observation measurement is not specified
     */
    boolean missing(int row);

    /**
     * Set the setValue of the observation specified by {@param row} as missing, not available for analysis.
     *
     * @param row position of the observation.
     */
    void setMissing(int row);

    void addMissing();

    void remove(int row);

    void clear();

    Vector solidCopy();

    void ensureCapacity(int minCapacity);

    public VSpots stream();
}
