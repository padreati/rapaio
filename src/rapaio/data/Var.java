/*
 * Apache License
 * Version 2.0, January 2004
 * http://www.apache.org/licenses/
 *
 *    Copyright 2013 Aurelian Tutuianu
 *    Copyright 2014 Aurelian Tutuianu
 *    Copyright 2015 Aurelian Tutuianu
 *    Copyright 2016 Aurelian Tutuianu
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

package rapaio.data;

import rapaio.data.filter.VFilter;
import rapaio.data.stream.VSpot;
import rapaio.data.stream.VSpots;
import rapaio.printer.Printable;
import rapaio.printer.Summary;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 * Random access list of observed values (observations) of a random variable (a vector with sample values).
 *
 * @author Aurelian Tutuianu
 */
public interface Var extends Serializable, Printable {

    /**
     * @return name of the variable
     */
    String getName();

    /**
     * Sets the variable name
     *
     * @param name future name of the variable
     */
    Var withName(String name);

    /**
     * @return variable type
     */
    VarType getType();

    /**
     * Number of observations contained by the variable.
     *
     * @return size of var
     */
    int getRowCount();

    /**
     * Builds a new variable having rows of the current variable,
     * followed by the rows of the bounded frame.
     *
     * @param var given var with additional rows
     * @return new var with all union of rows
     */
    default Var bindRows(Var var) {
        return BoundVar.from(this, var);
    }

    /**
     * Builds a new frame only with rows specified in mapping.
     *
     * @param mapping a list of rows from a frame
     * @return new frame with selected rows
     */
    default Var mapRows(Mapping mapping) {
        return MappedVar.byRows(this, mapping);
    }

    /**
     * Builds a new variable only with rows specified in mapping.
     *
     * @param rows a list of rows
     * @return new frame with selected rows
     */
    default Var mapRows(int... rows) {
        return mapRows(Mapping.copy(rows));
    }

    void addRows(int rowCount);

    /**
     * Returns numeric value for the observation specified by row.
     * <p>
     * Returns valid values for numerical var types, otherwise the method
     * returns unspecified value.
     *
     * @param row position of the observation
     * @return numerical setValue
     */
    double getValue(int row);

    /**
     * Set numeric value for the observation specified by {@param row} to {@param value}.
     * <p>
     * Returns valid values for numerical var types, otherwise the method
     * returns unspecified values.
     *
     * @param row   position of the observation
     * @param value numeric value from position {@param row}
     */
    void setValue(int row, double value);

    /**
     * Adds a new value to the last position of the variable.
     *
     * @param value value to be added variable
     */
    void addValue(double value);

    /**
     * Returns index value for the observation specified by {@param row}
     *
     * @param row position of the observation
     * @return index value
     */
    int getIndex(int row);

    /**
     * Set index value for the observation specified by {@param row}.
     *
     * @param row   position of the observation
     * @param value index value for the observation
     */
    void setIndex(int row, int value);

    /**
     * Adds an index value to the last position of the variable
     *
     * @param value value to be added at the end of the variable
     */
    void addIndex(int value);

    /**
     * Returns nominal label for the observation specified by {@param row}.
     *
     * @param row position of the observation
     * @return label value for the observation
     */
    String getLabel(int row);

    /**
     * Set nominal label for the observation specified by {@param row}.
     *
     * @param row   position of the observation
     * @param value label value of the observation
     */
    void setLabel(int row, String value);

    /**
     * Adds an index value to the last position of the variable, updates levels
     * if is necessary.
     *
     * @param value text label to be added at the end of the variable
     */
    void addLabel(String value);

    /**
     * Returns the term levels used by the nominal values.
     * <p>
     * Term levels contains all the nominal labels used by
     * observations and might contain also additional nominal labels.
     * Term levels defines the domain of the definition for the nominal var.
     * <p>
     * The term levels contains nominal labels sorted in lexicografical order,
     * so binary search techniques may be used on this var.
     * <p>
     * For other var types like numerical ones this method returns nothing.
     *
     * @return term levels defined by the nominal var.
     */
    String[] getLevels();

    default Stream<String> streamLevels() {
        return Arrays.stream(getLevels());
    }

    /**
     * Replace the used levels with a new one. A mapping between the
     * old values of the levels with the new values is done. The mapping
     * is done based on position.
     * <p>
     * The new levels can have repeated levels. This feature can be used
     * to unite multiple old labels with new ones. However the actual new
     * levels used will have only unique levels and indexed accordingly.
     * Thus a nominal with labels a,b,a,c,a,c which will have levels a,b,c,
     * when replaced with levels x,y,x will have as a result the following
     * labels: x,y,x,x,x,x and indexes 1,2,1,1,1,1
     *
     * @param dict array of levels which comprises the new levels
     */
    void setLevels(String... dict);

    /**
     * Replace the used levels with a new one. A mapping between the
     * old values of the levels with the new values is done. The mapping
     * is done based on position.
     * <p>
     * The new levels can have repeated levels. This feature can be used
     * to unite multiple old labels with new ones. However the actual new
     * levels used will have only unique levels and indexed accordingly.
     * Thus a nominal with labels a,b,a,c,a,c which will have levels a,b,c,
     * when replaced with levels x,y,x will have as a result the following
     * labels: x,y,x,x,x,x and indexes 1,2,1,1,1,1
     *
     * @param dict list of levels which comprises the new levels
     */
    default void setLevels(List<String> dict) {
        setLevels(dict.toArray(new String[0]));
    }

    /**
     * @param row position of the observation
     * @return boolean binary value
     */
    boolean getBinary(int row);

    /**
     * Set a binary/boolean value
     *
     * @param row   position of the observation
     * @param value boolean binary value
     */
    void setBinary(int row, boolean value);

    /**
     * Adds a binary/boolean value
     *
     * @param value boolean binary value to be added
     */
    void addBinary(boolean value);

    /**
     * Gets long integer (stamp) value.
     *
     * @param row position of the observation
     * @return long integer value
     */
    long getStamp(int row);

    /**
     * Set long integer (stamp) value
     *
     * @param row   position of the observation
     * @param value long integer value to be set
     */
    void setStamp(int row, long value);

    /**
     * Adds a long integer (stump) value
     *
     * @param value long integer value to be added
     */
    void addStamp(long value);

    /**
     * Returns true if the value for the observation specified by {@param row} is missing, not available.
     * <p>
     * A missing value for the observation means that the measurement
     * was not completed or the result of the measurement was not documented,
     * thus the value is not available for analysis.
     *
     * @param row position of the observation
     * @return true if the observation measurement is not specified or not assigned
     */
    boolean isMissing(int row);

    /**
     * Set the value of the observation specified by {@param row} as missing, not available for analysis.
     *
     * @param row position of the observation.
     */
    void setMissing(int row);

    /**
     * Adds a new observation unspecified observation value
     */
    void addMissing();

    /**
     * Removes the observation value at a given position.
     * The new size of the variable is the old size decremented by 1
     *
     * @param row position of the observation to be removed
     */
    void remove(int row);

    /**
     * Removes all the observation values specified by the variable.
     * The new size of the variable is 0.
     */
    void clear();

    /**
     * Creates a solid copy of the variable, even if the variable is mapped or not.
     *
     * @return a solid copy of the current variable
     */
    Var solidCopy();

    /**
     * Builds a new empty instance of the given type
     *
     * @return new empty instance
     */
    default Var newInstance() {
        return newInstance(0);
    }

    /**
     * Builds a new empty instance of given size
     *
     * @param rows size of the new variable
     * @return new empty instance of given size
     */
    Var newInstance(int rows);

    /**
     * @return a stream of variables spots
     */
    default VSpots stream() {
        return new VSpots(IntStream.range(0, getRowCount()).mapToObj(row -> new VSpot(row, this)), this);
    }

    /**
     * @return a stream of variables spots
     */
    default List<VSpot> spotList() {
        return IntStream.range(0, getRowCount()).mapToObj(row -> new VSpot(row, this)).collect(Collectors.toList());
    }

    default Var fitApply(VFilter... inputFilters) {
        Var var = this;
        for (VFilter filter : inputFilters) {
            var = filter.fitApply(var);
        }
        return var;
    }


    default Comparator<Integer> refComparator() {
        return refComparator(true);
    }

    default Comparator<Integer> refComparator(boolean asc) {
        switch (this.getType()) {
            case TEXT:
            case NOMINAL:
                return RowComparators.nominal(this, asc);
            case STAMP:
                return RowComparators.stamp(this, asc);
            case ORDINAL:
            case INDEX:
            case BINARY:
                return RowComparators.index(this, asc);
            default:
                return RowComparators.numeric(this, asc);
        }
    }

    /**
     * Tests if two variables has identical content, it does not matter the implementation.
     *
     * @param var variable on which the deep equals applied
     * @return true if type, size and content is identical
     */
    default boolean deepEquals(Var var) {
        if (!getName().equals(var.getName()))
            return false;
        if (getRowCount() != var.getRowCount())
            return false;
        if (getType() != var.getType())
            return false;
        for (int i = 0; i < getRowCount(); i++) {
            if (var.getType().isNumeric()) {
                if (Math.abs(getValue(i) - var.getValue(i)) > 1e-12)
                    return false;
            } else {
                if (!getLabel(i).equals(var.getLabel(i)))
                    return false;
            }
        }
        return true;
    }

    @Override
    default String getSummary() {
        return Summary.getSummary(this);
    }

    default void printLines() {
        printLines(true);
    }

    default void printLines(boolean merge) {
        Summary.lines(merge, this);
    }

    default void printLines(int n) {
        printLines(true, n);
    }

    default void printLines(boolean merge, int n) {
        Summary.lines(merge, this.mapRows(Mapping.range(0, n)));
    }
}
