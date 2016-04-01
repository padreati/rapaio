/*
 * Apache License
 * Version 2.0, January 2004
 * http://www.apache.org/licenses/
 *
 *    Copyright 2013 Aurelian Tutuianu
 *    Copyright 2014 Aurelian Tutuianu
 *    Copyright 2015 Aurelian Tutuianu
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

import rapaio.data.filter.FFilter;
import rapaio.data.stream.FSpot;
import rapaio.data.stream.FSpots;
import rapaio.printer.Printable;
import rapaio.sys.WS;
import rapaio.printer.Summary;

import java.io.Serializable;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;

/**
 * Random access list of observed values for multiple variables.
 * <p>
 * The observed values are represented in a tabular format.
 * Rows corresponds to observations and columns corresponds to observed variables.
 *
 * @author <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a>
 */
public interface Frame extends Serializable, Printable {

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
     * @return a var type reference
     */
    Var var(int pos);

    /**
     * Returns a var object with given name
     *
     * @param name name of the column inside the frame
     * @return a var type reference
     */
    Var var(String name);

    /**
     * Adds the given variables to the variables of the current frame to build a new frame.
     * New variables must have the same number of rows.
     *
     * @param vars variables added to the current frame variables
     * @return new frame with current frame variables and given variables added
     */
    Frame bindVars(Var... vars);

    /**
     * Adds the variables from the given frame to the variables of the current frame to build a new frame.
     * New variables from the given frame must have the same row count.
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
    Frame mapVars(VRange range);

    /**
     * Builds a new frame which has only the variables specified in the variable range string
     *
     * @param varRange variable range as string
     * @return new frame with only the given variables
     */
    default Frame mapVars(String... varRange) {
        return mapVars(VRange.of(varRange));
    }

    default Frame mapVars(List<String> varRange) {
        return mapVars(VRange.of(varRange.toArray(new String[varRange.size()])));
    }

    /**
     * Builds a new frame with all columns except the ones specified in variable range
     *
     * @param range given variable range which will be deleted
     * @return new frame with the non-deleted variables
     */
    default Frame removeVars(VRange range) {
        Set<String> remove = new HashSet<>(range.parseVarNames(this));
        if(remove.isEmpty())
            return this;
        if(remove.size() == this.varCount())
            return SolidFrame.newByVars();
        int[] retain = new int[varNames().length - remove.size()];
        int pos = 0;
        for (String varName : varNames()) {
            if (remove.contains(varName)) continue;
            retain[pos++] = varIndex(varName);
        }
        return mapVars(VRange.of(retain));
    }

    /**
     * Builds a new frame with all variables except ones specified in variable range string
     *
     * @param varRange variable range as string
     * @return new frame with the non-deleted variables
     */
    default Frame removeVars(String... varRange) {
        return removeVars(VRange.of(varRange));
    }

    /**
     * Builds a new frame with all the variables except ones in the given var indexes
     */
    default Frame removeVars(int... varIndexes) {
        return removeVars(VRange.of(varIndexes));
    }

    /**
     * Adds the following rowCount at the end of the frame.
     * The effect depends on the implementation, for solid frames
     * it increases rowCount, for other types it creates
     * a bounded frame.
     *
     * @param rowCount number of rowCount to be added
     * @return new frame with rowCount appended
     */
    Frame addRows(int rowCount);

    /**
     * Builds a new frame having rows of the current frame, followed by the rows of the bounded frame.
     * The new frame must has the same variable definitions as the current frame.
     *
     * @param df given frame with additional rows
     * @return new frame with additional rows
     */
    Frame bindRows(Frame df);

    /**
     * Builds a new mapped frame having the given rows.
     *
     * @param rows given rows to be selected
     * @return new mapped frame with given rows
     */
    default Frame mapRows(int... rows) {
        return mapRows(Mapping.copy(rows));
    }

    /**
     * Builds a new frame only with rows specified in mapping.
     *
     * @param mapping a list of rows from a frame
     * @return new frame with selected rows
     */
    Frame mapRows(Mapping mapping);

    /**
     * Builds a new frame only with rows not specified in mapping.
     */
    default Frame removeRows(int... rows) {
        return removeRows(Mapping.copy(rows));
    }

    /**
     * Builds a new frame only with rows not specified in mapping.
     */
    default Frame removeRows(Mapping mapping) {
        Set<Integer> remove = mapping.rowStream().mapToObj(i -> i).collect(Collectors.toSet());
        List<Integer> map = IntStream.range(0, rowCount()).filter(row -> !remove.contains(row)).mapToObj(i -> i).collect(toList());
        return mapRows(Mapping.wrap(map));
    }

    /**
     * Returns double value corresponding to given row and var index
     *
     * @param row      row number
     * @param varIndex variable index
     * @return numeric value
     */
    default double value(int row, int varIndex) {
        return var(varIndex).value(row);
    }

    /**
     * Returns double value from given row and varName
     *
     * @param row     row number
     * @param varName variable name
     * @return numeric value
     */
    default double value(int row, String varName) {
        return var(varName).value(row);
    }

    /**
     * Set double value for given row and var index
     *
     * @param row      row number
     * @param varIndex var index
     * @param value    numeric value
     */
    default void setValue(int row, int varIndex, double value) {
        var(varIndex).setValue(row, value);
    }

    /**
     * Convenient shortcut method to call {@link Var#setValue(int, double)} for a given variable.
     *
     * @param row     row number
     * @param varName var name
     * @param value   numeric value
     */
    default void setValue(int row, String varName, double value) {
        var(varName).setValue(row, value);
    }


    /**
     * Convenient shortcut method for calling {@link Var#index(int)} for a given variable.
     *
     * @param row      row number
     * @param varIndex column number
     * @return index value
     */
    default int index(int row, int varIndex) {
        if (varIndex >= varCount())
            throw new IllegalArgumentException("frame has " + varCount() + " variables, there is no var at index: " + varIndex);
        return var(varIndex).index(row);
    }

    /**
     * Convenient shortcut method for calling {@link Var#index(int)} for a given variable.
     *
     * @param row     row number
     * @param varName var name
     * @return index value
     */
    default int index(int row, String varName) {
        return var(varName).index(row);
    }

    /**
     * Convenient shortcut method for calling {@link Var#setIndex(int, int)} for given variable.
     *
     * @param row      row number
     * @param varIndex var index
     * @param value    setIndex value
     */
    default void setIndex(int row, int varIndex, int value) {
        var(varIndex).setIndex(row, value);
    }

    /**
     * Convenient shortcut method for calling {@link Var#setIndex(int, int)} for given variable.
     *
     * @param row     row number
     * @param varName var name
     * @param value   index value
     */
    default void setIndex(int row, String varName, int value) {
        var(varName).setIndex(row, value);
    }

    /**
     * Convenient shortcut method for calling {@link Var#label(int)} for given variable.
     *
     * @param row      row number
     * @param varIndex var index
     * @return nominal label value
     */
    default String label(int row, int varIndex) {
        return var(varIndex).label(row);
    }

    /**
     * Convenient shortcut method for calling {@link Var#label(int)} for given variable.
     *
     * @param row     row number
     * @param varName var name
     * @return nominal label value
     */
    default String label(int row, String varName) {
        return var(varName).label(row);
    }

    /**
     * Convenient shortcut method for calling {@link Var#setLabel(int, String)} for given variable.
     *
     * @param row      row number
     * @param varIndex var index
     * @param value    nominal label value
     */
    default void setLabel(int row, int varIndex, String value) {
        var(varIndex).setLabel(row, value);
    }

    /**
     * Convenient shortcut method for calling {@link Var#setLabel(int, String)} for given variable.
     *
     * @param row     row number
     * @param varName column name
     * @param value   nominal label value
     */
    default void setLabel(int row, String varName, String value) {
        var(varName).setLabel(row, value);
    }

    /**
     * Returns binary value from the given cell
     *
     * @param row      row number
     * @param varIndex variable index
     * @return binary value found
     */
    default boolean binary(int row, int varIndex) {
        return var(varIndex).binary(row);
    }

    /**
     * Returns binary value from given cell
     *
     * @param row     row number
     * @param varName var name
     * @return binary value found
     */
    default boolean binary(int row, String varName) {
        return var(varName).binary(row);
    }

    /**
     * Binary value setter for given cell
     *
     * @param row      row number
     * @param varIndex var index
     * @param value    value to be set
     */
    default void setBinary(int row, int varIndex, boolean value) {
        var(varIndex).setBinary(row, value);
    }

    /**
     * Binary value setter for given cell
     *
     * @param row     row number
     * @param varName var name
     * @param value   value to be set
     */
    default void setBinary(int row, String varName, boolean value) {
        var(varName).setBinary(row, value);
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

    default SolidFrame solidCopy() {
        final String[] names = varNames();
        final Var[] vars = new Var[names.length];
        for (int i = 0; i < names.length; i++) {
            vars[i] = var(names[i]).solidCopy().withName(names[i]);
        }
        return SolidFrame.newByVars(vars);
    }

    /**
     * @return a stream of FSpot
     */
    default FSpots stream() {
        return new FSpots(IntStream.range(0, rowCount()).mapToObj(row -> new FSpot(this, row)), this);
    }

    /**
     * Returns a list of FSpots, one spot for each frame row
     *
     * @return list of spots
     */
    default List<FSpot> spotList() {
        return IntStream.range(0, rowCount()).mapToObj(row -> new FSpot(this, row)).collect(toList());
    }

    /**
     * @return stream of all variables from frame
     */
    default Stream<Var> varStream() {
        return Arrays.stream(varNames()).map(this::var);
    }

    /**
     * @return a list with all variables from a frame
     */
    default List<Var> varList() {
        return varStream().collect(toList());
    }

    default Frame fitApply(FFilter... inputFilters) {
        Frame df = this;
        for (FFilter filter : inputFilters) {
            df = filter.fitApply(df);
        }
        return df;
    }

    @Override
    default String summary() {
        return Summary.summary(this);
    }

    default void printLines() {
        printLines(rowCount());
    }

    default String lines(int to) {
        Var[] vars = new Var[varCount()];
        String[] names = varNames();
        for (int i = 0; i < vars.length; i++) {
            vars[i] = var(i);
        }
        return Summary.headString(to, vars, names);
    }

    default void printLines(int to) {
        WS.code(lines(to));
    }

    default boolean deepEquals(Frame df) {
        if (rowCount() != df.rowCount())
            return false;
        if (varCount() != df.varCount())
            return false;
        String[] names = varNames();
        String[] dfNames = df.varNames();
        if (names.length != dfNames.length)
            return false;
        for (int i = 0; i < names.length; i++) {
            if (!(names[i].equals(dfNames[i]))) {
                return false;
            }
        }
        for (int i = 0; i < names.length; i++) {
            if (!(var(names[i]).deepEquals(df.var(dfNames[i])))) {
                return false;
            }
        }
        return true;
    }
}
