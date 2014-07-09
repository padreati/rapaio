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
import rapaio.data.stream.FSpots;

import java.io.Serializable;
import java.util.List;

/**
 * Random access list of observed values for multiple variables.
 * <p>
 * The observed values are represented in a tabular format.
 * Rows corresponds to observations and columns corresponds to
 * observed (measured) statstical variables.
 *
 * @author Aurelian Tutuianu
 */
@Deprecated
public interface Frame extends Serializable {

    /**
     * Number of observations contained in frame. Observations are accessed by position.
     *
     * @return number of observations
     */
    int rowCount();

    /**
     * Number of vectors contained in frame. Vector references could be obtained by name or by position.
     * <p>
     * Each var corresponds to a column in tabular format, thus in the frame terminology
     * this is denoted as getCol (short form of column).
     *
     * @return number of vectors
     */
    int colCount();

    /**
     * Returns an array of var names, the names are ordered by the position of the vectors.
     * <p>
     * Each var has it's own name. Inside a frame a specific var could be named differently.
     * However, the default name for a var inside a frame is the var's name.
     *
     * @return array of var names
     */
    String[] colNames();

    /**
     * Returns the index (position) of the var inside the frame given the var's name as parameter.
     *
     * @param name var's name
     * @return column position inside the frame corresponding to the var with the specified name
     */
    int colIndex(String name);

    /**
     * Returns a var reference for column at given position
     *
     * @param col position of the column inside the frame
     * @return a var getType reference
     */
    Var col(int col);

    /**
     * Returns a var reference for column with given name
     *
     * @param name name of the column inside the frame
     * @return a var getType reference
     */
    Var col(String name);

    public boolean isMappedFrame();

    /**
     * Returns the solid frame which contains the data. Solid frames return themselves,
     * mapped frames returns the solid frame which contains the data.
     *
     * @return source frame when mapped, itself for solid frame
     */
    public Frame sourceFrame();

    public Mapping mapping();

    /**
     * Convenient shortcut to call {@link Var#value(int)} for a given column.
     *
     * @param row row number
     * @param col column number
     * @return numeric setValue
     */
    default double value(int row, int col) {
        return col(col).value(row);
    }

    /**
     * Convenient shortcut to call {@link Var#value(int)} for a given column.
     *
     * @param row     row number
     * @param colName column name
     * @return numeric setValue
     */
    default double value(int row, String colName) {
        return col(colName).value(row);
    }

    /**
     * Convenient shortcut method to call {@link Var#setValue(int, double)} for a given column.
     *
     * @param row   row number
     * @param col   column number
     * @param value numeric value
     */
    default void setValue(int row, int col, double value) {
        col(col).setValue(row, value);
    }

    /**
     * Convenient shortcut method to call {@link Var#setValue(int, double)} for a given column.
     *
     * @param row     row number
     * @param colName column name
     * @param value   numeric value
     */
    default void setValue(int row, String colName, double value) {
        col(colName).setValue(row, value);
    }


    /**
     * Convenient shortcut method for calling {@link Var#index(int)} for a given column.
     *
     * @param row row number
     * @param col column number
     * @return setIndex value
     */
    default int index(int row, int col) {
        return col(col).index(row);
    }

    /**
     * Convenient shortcut method for calling {@link Var#index(int)} for a given column.
     *
     * @param row     row number
     * @param colName column name
     * @return setIndex value
     */
    default int index(int row, String colName) {
        return col(colName).index(row);
    }

    /**
     * Convenient shortcut method for calling {@link Var#setIndex(int, int)} for given column.
     *
     * @param row   row number
     * @param col   column number
     * @param value setIndex value
     */
    default void setIndex(int row, int col, int value) {
        col(col).setIndex(row, value);
    }

    /**
     * Convenient shortcut method for calling {@link Var#setIndex(int, int)} for given column.
     *
     * @param row     row number
     * @param colName column name
     * @param value   setIndex value
     */
    default void setIndex(int row, String colName, int value) {
        col(colName).setIndex(row, value);
    }

    /**
     * Convenient shortcut method for calling {@link Var#label(int)} for given column.
     *
     * @param row row number
     * @param col column number
     * @return nominal label value
     */
    default String label(int row, int col) {
        return col(col).label(row);
    }

    /**
     * Convenient shortcut method for calling {@link Var#label(int)} for given column.
     *
     * @param row     row number
     * @param colName column name
     * @return nominal label value
     */
    default String label(int row, String colName) {
        return col(colName).label(row);
    }

    /**
     * Convenient shortcut method for calling {@link Var#setLabel(int, String)} for given column.
     *
     * @param row   row number
     * @param col   column number
     * @param value nominal label value
     */
    default void setLabel(int row, int col, String value) {
        col(col).setLabel(row, value);
    }

    /**
     * Convenient shortcut method for calling {@link Var#setLabel(int, String)} for given column.
     *
     * @param row     row number
     * @param colName column name
     * @param value   nominal label value
     */
    default void setLabel(int row, String colName, String value) {
        col(colName).setLabel(row, value);
    }

    default boolean missing(int row, int col) {
        return col(col).missing(row);
    }

    default boolean missing(int row, String colName) {
        return col(colName).missing(row);
    }

    /**
     * Returns true if there is at least one missing value for the given row, in any column.
     *
     * @param row row number
     * @return
     */
    default boolean missing(int row) {
        for (String colName : colNames()) {
            if (col(colName).missing(row)) return true;
        }
        return false;
    }

    default void setMissing(int row, int col) {
        col(col).setMissing(row);
    }

    default void setMissing(int row, String colName) {
        col(colName).setMissing(row);
    }

    /**
     * Builds a stream of FSpots
     *
     * @return
     */
    public FSpots stream();

    public Numeric weights();

    public void setWeights(Numeric weights);

    public double weight(int row);

    public void setWeight(int row, double weight);
}
