/*
 * Copyright 2013 Aurelian Tutuianu
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

import java.io.Serializable;
import java.util.Comparator;

/**
 * Random access list of observed values (observations) for a specific variable.
 *
 * @author Aurelian Tutuianu
 */
public interface Vector extends Serializable {

    /**
     * Specifies if a vector contains numeric values.
     * <p/>
     * This is used by continuous or discrete random variable observations.
     *
     * @return true if the vector is numeric
     */
    boolean isNumeric();

    /**
     * Specifies if the vector contains categorical/nominal values.
     *
     * @return true is the vector is nominal, false otherwise
     */
    boolean isNominal();

    /**
     * The name of the vector. Name is mostly used by various user-interaction facilities.
     *
     * @return name of the vector
     */
    String getName();

    /**
     * Number of observations contained by the vector.
     *
     * @return size of vector
     */
    int getRowCount();

    /**
     * Returns observation identifier which is an integer.
     * <p/>
     * When a vector or frame is created from scratch as a solid vector/frame then
     * row identifiers are the row numbers. When the vector/frame wraps other
     * vector/frame then row identifier is the wrapped row identifier.
     * <p/>
     * This is mostly used to keep track of the original row numbers even after a series
     * of transformations which use wrapped vectors/frames.
     *
     * @param row row for which row identifier is returned
     * @return row identifier
     */
    int getRowId(int row);

    /**
     * Returns numeric setValue for the observation specified by {@code row}.
     * <p/>
     * Returns valid values for numerical vector types, otherwise the method
     * returns unspeified values.
     *
     * @param row
     * @return numerical setValue
     */
    double getValue(int row);

    /**
     * Set numeric setValue for the observation specified by {@param row} to {@param setValue}.
     * <p/>
     * Returns valid values for numerical vector types, otherwise the method
     * returns unspeified values.
     *
     * @param row   position of the observation
     * @param value numeric setValue from position {@param row}
     */
    void setValue(int row, double value);

    /**
     * Returns getIndex setValue for the observation specified by {@param row}
     *
     * @param row position of the observation
     * @return getIndex setValue
     */
    int getIndex(int row);

    /**
     * Set getIndex setValue for the observation specified by {@param row}.
     *
     * @param row   position of the observation
     * @param value getIndex setValue for the observation
     */
    void setIndex(int row, int value);

    /**
     * Returns nominal getLabel for the observation specified by {@param row}.
     *
     * @param row position of the observation
     * @return getLabel setValue for the observation
     */
    String getLabel(int row);

    /**
     * Set nominal getLabel for the observation specified by {@param row}.
     *
     * @param row   position of the observation
     * @param value getLabel setValue of the observation
     */
    void setLabel(int row, String value);

    /**
     * Returns the term dictionary used by the nominal values.
     * <p/>
     * Term dictionary contains all the nominal labels used by
     * observations and might contain also additional nominal labels.
     * Term dictionary defines the domain of the definition for the nominal vector.
     * <p/>
     * The term dictionary contains nominal labels sorted in lexicografical order,
     * so binary search techniques may be used on this vector.
     * <p/>
     * For other vector types like numerical ones this method returns nothing.
     *
     * @return term dictionary defined by the nominal vector.
     */
    String[] dictionary();

    /**
     * Returns true if the setValue for the observation specified by {@param row} is missing, not available.
     * <p/>
     * A missing setValue for the observation means taht the measurement
     * was not completed or the result of the measurement was not documented,
     * thus the setValue is not available for analysis.
     *
     * @param row position of teh observation
     * @return true if the observation measurement is not specified
     */
    boolean isMissing(int row);

    /**
     * Set the setValue of the observation specified by {@param row} as missing, not available for analysis.
     *
     * @param row position of the observation.
     */
    void setMissing(int row);

    /**
     * Returns the default comparator for the values of the observation
     * in ascending or descending order, specified by {@param asc}.
     * <p/>
     * The defaul comparator depends on the implementation of the vector.
     * For numerical vectors the ordering is the natural numeric ordering,
     * for nominal values the ordering is the case-sensitive lexicographical ordering.
     *
     * @param asc
     * @return default observation comparator
     */
    Comparator<Integer> getComparator(boolean asc);
}
