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

import rapaio.data.collect.FIterator;
import rapaio.data.mapping.Mapping;

import java.io.Serializable;

/**
 * Random access list of observed values for multiple variables.
 * <p>
 * The observed values are represented in a tabular format.
 * Rows corresponds to observations and columns corresponds to
 * observed (measured) statstical variables.
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
     * Number of vectors contained in frame. Vector references could be obtained by name or by position.
     * <p>
     * Each vector corresponds to a column in tabular format, thus in the frame terminology
     * this is denoted as getCol (short form of column).
     *
     * @return number of vectors
     */
    int colCount();

    /**
     * Returns an array of vector names, the names are ordered by the position of the vectors.
     * <p>
     * Each vector has it's own name. Inside a frame a specific vector could be named differently.
     * However, the default name for a vector inside a frame is the vector's name.
     *
     * @return array of vector names
     */
    String[] colNames();

    /**
     * Returns the getIndex (position) of the vector inside the frame given the vector's name as parameter.
     *
     * @param name vector's name
     * @return column position inside the frame corresponding to the vector with the specified name
     */
    int colIndex(String name);

    /**
     * Returns a vector reference for column at given position
     *
     * @param col position of the column inside the frame
     * @return a vector getType reference
     */
    Vector col(int col);

    /**
     * Returns a vector reference for column with given name
     *
     * @param name name of the column inside the frame
     * @return a vector getType reference
     */
    Vector col(String name);

    /**
     * Returns row identifier for a specific column. See {@link Vector#rowId(int)} for further reference.
     *
     * @param row row for which row identifier is returned
     * @return row identifier
     */
    int rowId(int row);

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
     * Convenient shortcut to call {@link Vector#value(int)} for a given column.
     *
     * @param row row number
     * @param col column number
     * @return numeric setValue
     */
    double value(int row, int col);

    /**
     * Convenient shortcut to call {@link Vector#value(int)} for a given column.
     *
     * @param row     row number
     * @param colName column name
     * @return numeric setValue
     */
    double value(int row, String colName);

    /**
     * Convenient shortcut method to call {@link Vector#setValue(int, double)} for a given column.
     *
     * @param row   row number
     * @param col   column number
     * @param value numeric getValue
     */
    void setValue(int row, int col, double value);

    /**
     * Convenient shortcut method to call {@link Vector#setValue(int, double)} for a given column.
     *
     * @param row     row number
     * @param colName column name
     * @param value   numeric getValue
     */
    void setValue(int row, String colName, double value);


    /**
     * Convenient shortcut method for calling {@link Vector#index(int)} for a given column.
     *
     * @param row row number
     * @param col column number
     * @return setIndex getValue
     */
    int index(int row, int col);

    /**
     * Convenient shortcut method for calling {@link Vector#index(int)} for a given column.
     *
     * @param row     row number
     * @param colName column name
     * @return setIndex getValue
     */
    int index(int row, String colName);

    /**
     * Convenient shortcut method for calling {@link Vector#setIndex(int, int)} for given column.
     *
     * @param row   row number
     * @param col   column number
     * @param value setIndex getValue
     */
    void setIndex(int row, int col, int value);

    /**
     * Convenient shortcut method for calling {@link Vector#setIndex(int, int)} for given column.
     *
     * @param row     row number
     * @param colName column name
     * @param value   setIndex getValue
     */
    void setIndex(int row, String colName, int value);

    /**
     * Convenient shortcut method for calling {@link Vector#label(int)} for given column.
     *
     * @param row row number
     * @param col column number
     * @return nominal getLabel getValue
     */
    String label(int row, int col);

    /**
     * Convenient shortcut method for calling {@link Vector#label(int)} for given column.
     *
     * @param row     row number
     * @param colName column name
     * @return nominal getLabel getValue
     */
    String label(int row, String colName);

    /**
     * Convenient shortcut method for calling {@link Vector#setLabel(int, String)} for given column.
     *
     * @param row   row number
     * @param col   column number
     * @param value nominal getLabel getValue
     */
    void setLabel(int row, int col, String value);

    /**
     * Convenient shortcut method for calling {@link Vector#setLabel(int, String)} for given column.
     *
     * @param row     row number
     * @param colName column name
     * @param value   nominal getLabel getValue
     */
    void setLabel(int row, String colName, String value);

    boolean missing(int row, int col);

    boolean missing(int row, String colName);

    boolean missing(int row);

    void setMissing(int row, int col);

    void setMissing(int row, String colName);

    public FIterator iterator();

    public FIterator iterator(boolean complete);
}
