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

package rapaio.data;

import it.unimi.dsi.fastutil.ints.IntComparator;
import rapaio.data.filter.FFilter;
import rapaio.data.filter.FRefSort;
import rapaio.data.stream.FSpot;
import rapaio.data.stream.FSpots;
import rapaio.printer.Printable;

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
     * Returns the name of a variable for a given index.
     */
    String varName(int i);

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
    Var rvar(int pos);

    /**
     * Returns a var object with given name
     *
     * @param name name of the column inside the frame
     * @return a var type reference
     */
    Var rvar(String name);

    /**
     * Returns the type of the given var
     *
     * @param name variable name
     * @return variable type
     */
    VType type(String name);

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
     * @return new mapped frame with only the given variables
     */
    default Frame mapVars(String... varRange) {
        return mapVars(VRange.of(varRange));
    }

    /**
     * Builds a new frame which has only the variables specified in the variable range list
     *
     * @param varNameList list of variable names
     * @return new mapped frame with only the given variables
     */
    default Frame mapVars(List<String> varNameList) {
        return mapVars(VRange.of(varNameList));
    }

    /**
     * Builds a new frame with all columns except the ones specified in variable range
     *
     * @param range given variable range which will be deleted
     * @return new frame with the non-deleted variables
     */
    default Frame removeVars(VRange range) {
        Set<String> remove = new HashSet<>(range.parseVarNames(this));
        if (remove.isEmpty())
            return this;
        if (remove.size() == this.varCount())
            return SolidFrame.byVars();
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
     * Builds a new frame only with rows not specified in mapping.
     */
    default Frame removeRows(int... rows) {
        return removeRows(Mapping.wrap(rows));
    }

    /**
     * Builds a new frame only with rows not specified in mapping.
     */
    default Frame removeRows(Mapping mapping) {
        Set<Integer> remove = mapping.stream().boxed().collect(Collectors.toSet());
        int[] map = new int[Math.max(0, rowCount() - remove.size())];
        int pos = 0;
        for (int i = 0; i < rowCount(); i++) {
            if (!remove.contains(i)) {
                map[pos++] = i;
            }
        }
        return mapRows(Mapping.wrap(map));
    }

    /**
     * Clear all rows. Obtain a data frame which has all variable definitions
     * with same metadata but without any observation.
     *
     * @return frame with no rows
     */
    Frame clearRows();

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
        return mapRows(Mapping.wrap(rows));
    }

    /**
     * Builds a new frame only with rows specified in mapping.
     *
     * @param mapping a list of rows from a frame
     * @return new frame with selected rows
     */
    Frame mapRows(Mapping mapping);

    /**
     * Returns double value corresponding to given row and var index
     *
     * @param row      row number
     * @param varIndex variable index
     * @return numeric value
     */
    double getDouble(int row, int varIndex);

    /**
     * Returns double value from given row and varName
     *
     * @param row     row number
     * @param varName variable name
     * @return numeric value
     */
    double getDouble(int row, String varName);

    /**
     * Set double value for given row and var index
     *
     * @param row   row number
     * @param col   var index
     * @param value numeric value
     */
    void setDouble(int row, int col, double value);

    /**
     * Convenient shortcut method to call {@link Var#setDouble(int, double)} for a given variable.
     *
     * @param row     row number
     * @param varName var name
     * @param value   numeric value
     */
    void setDouble(int row, String varName, double value);

    /**
     * Convenient shortcut method for calling {@link Var#getInt(int)} for a given variable.
     *
     * @param row      row number
     * @param varIndex variable index
     * @return index value
     */
    int getInt(int row, int varIndex);

    /**
     * Convenient shortcut method for calling {@link Var#getInt(int)} for a given variable.
     *
     * @param row     row number
     * @param varName var name
     * @return index value
     */
    int getInt(int row, String varName);

    /**
     * Convenient shortcut method for calling {@link Var#setInt(int, int)} for given variable.
     *
     * @param row   row number
     * @param col   var index
     * @param value int value
     */
    void setInt(int row, int col, int value);

    /**
     * Convenient shortcut method for calling {@link Var#setInt(int, int)} for given variable.
     *
     * @param row     row number
     * @param varName var name
     * @param value   index value
     */
    void setInt(int row, String varName, int value);

    /**
     * Convenient shortcut method for calling {@link Var#getLong(int)} for a given variable.
     *
     * @param row      row number
     * @param varIndex variable index
     * @return long value
     */
    long getLong(int row, int varIndex);

    /**
     * Convenient shortcut method for calling {@link Var#getLong(int)} for a given variable.
     *
     * @param row     row number
     * @param varName var name
     * @return long value
     */
    long getLong(int row, String varName);

    /**
     * Convenient shortcut method for calling {@link Var#setLong(int, long)} for given variable.
     *
     * @param row   row number
     * @param col   var index
     * @param value long value
     */
    void setLong(int row, int col, long value);

    /**
     * Convenient shortcut method for calling {@link Var#setLong(int, long)} for given variable.
     *
     * @param row     row number
     * @param varName var name
     * @param value   long value
     */
    void setLong(int row, String varName, long value);

    /**
     * Convenient shortcut method for calling {@link Var#getLabel(int)} for given variable.
     *
     * @param row row number
     * @param col var index
     * @return nominal label value
     */
    String getLabel(int row, int col);

    /**
     * Convenient shortcut method for calling {@link Var#getLabel(int)} for given variable.
     *
     * @param row     row number
     * @param varName var name
     * @return nominal label value
     */
    String getLabel(int row, String varName);

    /**
     * Convenient shortcut method for calling {@link Var#setLabel(int, String)} for given variable.
     *
     * @param row   row number
     * @param col   var index
     * @param value nominal label value
     */
    void setLabel(int row, int col, String value);

    /**
     * Convenient shortcut method for calling {@link Var#setLabel(int, String)} for given variable.
     *
     * @param row     row number
     * @param varName column name
     * @param value   nominal label value
     */
    void setLabel(int row, String varName, String value);

    List<String> levels(String varName);

    /**
     * Convenient shortcut method for calling {@link Var#isMissing(int)} for given column
     *
     * @param row row number
     * @param col column number
     * @return true if missing, false otherwise
     */
    boolean isMissing(int row, int col);

    /**
     * Convenient shortcut method for calling {@link Var#isMissing(int)} for given column
     *
     * @param row     row number
     * @param varName var name
     * @return true if missing, false otherwise
     */
    boolean isMissing(int row, String varName);

    /**
     * Returns true if there is at least one missing value for the given row, in any column.
     *
     * @param row row number
     * @return true if there is a missing value for any variable at the given row
     */
    default boolean isMissing(int row) {
        for (String colName : varNames()) {
            if (rvar(colName).isMissing(row)) return true;
        }
        return false;
    }

    /**
     * Convenient shortcut method for calling {@link Var#setMissing(int)} for given column
     *
     * @param row row number
     * @param col column number
     */
    void setMissing(int row, int col);

    /**
     * Convenient shortcut method for calling {@link Var#setMissing(int)} for given column
     *
     * @param row     row number
     * @param varName var name
     */
    void setMissing(int row, String varName);

    default SolidFrame copy() {
        final String[] names = varNames();
        final Var[] vars = new Var[names.length];
        for (int i = 0; i < names.length; i++) {
            vars[i] = rvar(names[i]).copy().withName(names[i]);
        }
        return SolidFrame.byVars(vars);
    }

    /**
     * @return a stream of FSpot
     */
    default FSpots stream() {
        return new FSpots(this);
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
        return Arrays.stream(varNames()).map(this::rvar);
    }

    /**
     * @return a list with all variables from a frame
     */
    default List<Var> varList() {
        return varStream().collect(toList());
    }

    /**
     * Fit and apply frame filters to the data frame.
     * The array of filters are applied successively, the first filter
     * transform the frame, second filter transform the transformed frame and so on,
     * until in the end the last transformed filter in the chain is returned.
     *
     * @param filters list of frame filters
     * @return transformed frame
     */
    default Frame fapply(FFilter... filters) {
        Frame df = this;
        for (FFilter filter : filters) {
            df = filter.fapply(df);
        }
        return df;
    }

    /**
     * Apply already fitted frame filters to the data frame.
     * The array of filters are applied successively, the first filter
     * transform the frame, second filter transform the transformed frame and so on,
     * until in the end the last transformed filter in the chain is returned.
     *
     * @param filters list of frame filters
     * @return transformed frame
     */
    default Frame apply(FFilter... filters) {
        Frame df = this;
        for (FFilter filter : filters) {
            df = filter.apply(df);
        }
        return df;
    }

    /**
     * Sorts data frame ordered by given columns in ascending orders
     *
     * @param names list of columns
     * @return sorted frame
     */
    default Frame refSort(String... names) {
        IntComparator[] comparators = new IntComparator[names.length];
        for (int i = 0; i < names.length; i++) {
            comparators[i] = this.rvar(names[i]).refComparator();
        }
        return this.fapply(FRefSort.by(comparators));
    }

    String head();

    String head(int lines);

    void printHead();

    void printHead(int lines);

    default boolean deepEquals(Frame df) {
        if (rowCount() != df.rowCount())
            return false;
        if (varCount() != df.varCount())
            return false;
        String[] names = varNames();
        String[] dfNames = df.varNames();
        for (int i = 0; i < names.length; i++) {
            if (!(names[i].equals(dfNames[i]))) {
                return false;
            }
        }
        for (int i = 0; i < names.length; i++) {
            if (!(rvar(names[i]).deepEquals(df.rvar(dfNames[i])))) {
                return false;
            }
        }
        return true;
    }
}
