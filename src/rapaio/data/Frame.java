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

import rapaio.core.VarRange;
import rapaio.data.stream.FSpots;

import java.io.Serializable;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Random access list of observed values for multiple variables.
 * <p>
 * The observed values are represented in a tabular format.
 * Rows corresponds to observations and columns corresponds to
 * observed (measured) statistical variables.
 *
 * @author Aurelian Tutuianu
 */
public interface Frame extends Serializable {

    /**
     * Number of observations contained in frame. Observations are accessed by position.
     *
     * @return number of observations
     */
    int rowCount();

    /**
     * Number of variables contained in frame. Variable references could be obtained by name or by position.
     * <p>
     * Each variable corresponds to a column in tabular format, thus in the frame terminology
     * this is denoted as var (short form of column).
     *
     * @return number of variables
     */
    int varCount();

    /**
     * Returns an array of variable names. The names are ordered by the position of the variables.
     * <p>
     * Each variable has it's own name. Inside a frame a specific variable could be named differently.
     * However, the default name for a variable inside a frame is own variable name.
     *
     * @return array of var names
     */
    String[] varNames();

    /**
     * Returns the index (position) of the var inside the frame given the var name as parameter.
     *
     * @param name var name
     * @return column position inside the frame corresponding to the var with the specified name
     */
    int varIndex(String name);

    /**
     * Returns a var object from the given position
     *
     * @param pos position of the column inside the frame
     * @return a var getType reference
     */
    Var var(int pos);

    /**
     * Returns a var object with given name
     *
     * @param name name of the column inside the frame
     * @return a var getType reference
     */
    Var var(String name);

    /**
     * Adds the given variables to the variables of the current frame to build a new frame.
     *
     * @param vars variables added to the current frame variables
     * @return new frame with current frame variables and given variables added
     */
    Frame bindVars(Var... vars);

    /**
     * Adds the variables from the given frame to the variables of the current frame to build a new frame.
     *
     * @param df given frame with variables which will be added
     * @return new frame with the current frame variables and given frame variables
     */
    Frame bindVars(Frame df);

    /**
     * Builds a new frame which has only the variables specified in variable range
     *
     * @param range given variable range
     * @return new frame with only given variables
     */
    Frame mapVars(VarRange range);

    /**
     * Builds a new frame which has only the variables specified in the variable range string
     *
     * @param varRange variable range as string
     * @return new frame with only the given variables
     */
    default Frame mapVars(String varRange) {
        return mapVars(new VarRange(varRange));
    }

    /**
     * Builds a new frame with all columns except the ones specified in variable range
     *
     * @param range given variable range which will be deleted
     * @return new frame with the non-deleted variables
     */
    default Frame removeVars(VarRange range) {
        Set<String> remove = new HashSet<>(range.parseVarNames(this));
        int[] retain = new int[varNames().length - remove.size()];
        int pos = 0;
        for (String varName : varNames()) {
            if (remove.contains(varName)) continue;
            retain[pos++] = varIndex(varName);
        }
        return mapVars(new VarRange(retain));
    }

    /**
     * Builds a new frame with all variables except ones specified in variable range string
     *
     * @param varRange variable range as string
     * @return new frame with the non-deleted variables
     */
    default Frame removeVars(String varRange) {
        return removeVars(new VarRange(varRange));
    }

    /**
     * Builds a new frame having rows of the current frame, followed by the rows of the binded frame.
     *
     * @param df given frame with additional rows
     * @return new frame with additional rows
     */
    public Frame bindRows(Frame df);

    /**
     * Builds a new frame only with rows specified in mapping.
     *
     * @param mapping a list of rows from a frame
     * @return new frame with selected rows
     */
    public Frame mapRows(Mapping mapping);

    /**
     * Builds a new frame only with rows not specified in mapping.
     */
    default Frame removeRows(int... rows) {
        return removeRows(Mapping.newCopyOf(rows));
    }

    /**
     * Builds a new frame only with rows not specified in mapping.
     */
    default Frame removeRows(Mapping mapping) {
        Set<Integer> remove = mapping.rowStream().mapToObj(i -> i).collect(Collectors.toSet());
        List<Integer> map = IntStream.range(0, rowCount()).filter(row -> !remove.contains(row)).mapToObj(i -> i).collect(Collectors.toList());
        return mapRows(Mapping.newWrapOf(map));
    }

    /**
     * Returns double value corresponding to given row and var number
     *
     * @param row row number
     * @param col column number
     * @return numeric setValue
     */
    default double value(int row, int col) {
        return var(col).value(row);
    }

    /**
     * Returns double value from given row and varName
     *
     * @param row     row number
     * @param varName column name
     * @return numeric value
     */
    default double value(int row, String varName) {
        return var(varName).value(row);
    }

    /**
     * Set double value for given row and column
     *
     * @param row   row number
     * @param col   column number
     * @param value numeric value
     */
    default void setValue(int row, int col, double value) {
        var(col).setValue(row, value);
    }

    /**
     * Convenient shortcut method to call {@link Var#setValue(int, double)} for a given column.
     *
     * @param row     row number
     * @param varName var name
     * @param value   numeric value
     */
    default void setValue(int row, String varName, double value) {
        var(varName).setValue(row, value);
    }


    /**
     * Convenient shortcut method for calling {@link Var#index(int)} for a given column.
     *
     * @param row row number
     * @param col column number
     * @return setIndex value
     */
    default int index(int row, int col) {
        return var(col).index(row);
    }

    /**
     * Convenient shortcut method for calling {@link Var#index(int)} for a given column.
     *
     * @param row     row number
     * @param varName var name
     * @return setIndex value
     */
    default int index(int row, String varName) {
        return var(varName).index(row);
    }

    /**
     * Convenient shortcut method for calling {@link Var#setIndex(int, int)} for given column.
     *
     * @param row   row number
     * @param col   column number
     * @param value setIndex value
     */
    default void setIndex(int row, int col, int value) {
        var(col).setIndex(row, value);
    }

    /**
     * Convenient shortcut method for calling {@link Var#setIndex(int, int)} for given column.
     *
     * @param row     row number
     * @param varName var name
     * @param value   setIndex value
     */
    default void setIndex(int row, String varName, int value) {
        var(varName).setIndex(row, value);
    }

    /**
     * Convenient shortcut method for calling {@link Var#label(int)} for given column.
     *
     * @param row row number
     * @param col column number
     * @return nominal label value
     */
    default String label(int row, int col) {
        return var(col).label(row);
    }

    /**
     * Convenient shortcut method for calling {@link Var#label(int)} for given column.
     *
     * @param row     row number
     * @param varName column name
     * @return nominal label value
     */
    default String label(int row, String varName) {
        return var(varName).label(row);
    }

    /**
     * Convenient shortcut method for calling {@link Var#setLabel(int, String)} for given column.
     *
     * @param row   row number
     * @param col   column number
     * @param value nominal label value
     */
    default void setLabel(int row, int col, String value) {
        var(col).setLabel(row, value);
    }

    /**
     * Convenient shortcut method for calling {@link Var#setLabel(int, String)} for given column.
     *
     * @param row     row number
     * @param varName column name
     * @param value   nominal label value
     */
    default void setLabel(int row, String varName, String value) {
        var(varName).setLabel(row, value);
    }

    /**
     * Convenient shortcut method for calling {@link Var#missing(int)} for given column
     *
     * @param row row number
     * @param col column number
     * @return true if missing, false otherwise
     */
    default boolean missing(int row, int col) {
        return var(col).missing(row);
    }

    /**
     * Convenient shortcut method for calling {@link Var#missing(int)} for given column
     *
     * @param row     row number
     * @param varName var name
     * @return true if missing, false otherwise
     */
    default boolean missing(int row, String varName) {
        return var(varName).missing(row);
    }

    /**
     * Returns true if there is at least one missing value for the given row, in any column.
     *
     * @param row row number
     * @return true if there is a missing value for any variable at the given row
     */
    default boolean missing(int row) {
        for (String colName : varNames()) {
            if (var(colName).missing(row)) return true;
        }
        return false;
    }

    /**
     * Convenient shortcut method for calling {@link Var#setMissing(int)} for given column
     *
     * @param row row number
     * @param col column number
     */
    default void setMissing(int row, int col) {
        var(col).setMissing(row);
    }

    /**
     * Convenient shortcut method for calling {@link Var#setMissing(int)} for given column
     *
     * @param row     row number
     * @param varName var name
     */
    default void setMissing(int row, String varName) {
        var(varName).setMissing(row);
    }

    SolidFrame solidCopy();

    /**
     * Builds a stream of FSpots
     *
     * @return a stream of FSpot
     */
    public FSpots stream();
}
