/*
 * Copyright 2013 Aurelian Tutuianu <padreati@yahoo.com>
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

/**
 * Random access list of observed values for multiple variables.
 * <p/>
 * The observed values are represented in a tabular format.
 * Rows corresponds to observations and columns corresponds to
 * observed (measured) statstical variables.
 *
 * @author Aurelian Tutuianu
 */
public interface Frame extends Serializable {

    /**
     * Returns the name of the frame, used mostly by exploring facilities.
     */
    String getName();

    /**
     * Number of observations contained in frame. Observations are accessed by position.
     *
     * @return number of observations
     */
    int getRowCount();

    /**
     * Number of vectors contained in frame. Vector references could be obtained by name or by position.
     * <p/>
     * Each vector corresponds to a column in tabular format, thus in the frame terminology
     * this is denoted as col (short form of column).
     *
     * @return number of vectors
     */
    int getColCount();

    /**
     * Returns an array of vector names, the names are ordered by the position of the vectors.
     * <p/>
     * Each vector has it's own name. Inside a frame a specific vector could be named differently.
     * However, the default name for a vector inside a frame is the vector's name.
     *
     * @return array of vector names
     */
    String[] getColNames();

    /**
     * Returns the getIndex (position) of the vector inside the frame given the vector's name as parameter.
     *
     * @param name vector's name
     * @return column position inside the frame corresponding to the vector with the specified name
     */
    int getColIndex(String name);

    /**
     * Returns a vector reference for column at given position
     *
     * @param col position of the column inside the frame
     * @return a vector type reference
     */
    Vector getCol(int col);

    /**
     * Returns a vectir reference for column with given name
     *
     * @param name name of the column inside the frame
     * @return a vector type reference
     */
    Vector getCol(String name);

    /**
     * Returns row identifier. See {@link Vector#getRowId(int)} for further reference.
     *
     * @param row row for which row identifier is returned
     * @return row identifier
     */
    int rowId(int row);

    /**
     * Convenient shortcut to call {@link Vector#getValue(int)} for a given column.
     *
     * @param row row number
     * @param col column number
     * @return numeric setValue
     */
    double getValue(int row, int col);

    /**
     * Convenient shortcut method to call {@link Vector#setValue(int, double)} for a given column.
     *
     * @param row   row number
     * @param col   column number
     * @param value numeric value
     */
    void setValue(int row, int col, double value);


    /**
     * Convenient shortcut method for calling {@link Vector#getIndex(int)} for a given column.
     *
     * @param row row number
     * @param col column number
     * @return setIndex value
     */
    int getIndex(int row, int col);

    /**
     * Convenient shortcut method for calling {@link Vector#setIndex(int, int)} for given column.
     *
     * @param row   row number
     * @param col   column number
     * @param value setIndex value
     */
    void setIndex(int row, int col, int value);

    /**
     * Conveninet shortcut method for calling {@link Vector#getLabel(int)} for given column.
     *
     * @param row row number
     * @param col column number
     * @return nominal label value
     */
    String getLabel(int row, int col);

    /**
     * Convenient shortcut method for calling {@link Vector#setLabel(int, String)} for given column.
     *
     * @param row   row number
     * @param col   column number
     * @param value nominal label value
     */
    void setLabel(int row, int col, String value);
}
