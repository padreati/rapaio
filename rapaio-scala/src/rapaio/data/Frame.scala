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

package rapaio.data

import rapaio.data.mapping.Mapping
import java.io.Serializable

/**
 * Random access list of observed values for multiple variables.
 * <p>
 * The observed values are represented in a tabular format.
 * Rows corresponds to observations and columns corresponds to
 * observed (measured) statistical variables.
 *
 * @author Aurelian Tutuianu
 */
abstract trait Frame extends Serializable {
  /**
   * Number of observations contained in frame
   * @return number of observations
   */
  def rowCount: Int

  /**
   * Number of vectors contained in frame. Vector references could be obtained by name or by position.
   * <p>
   * Each vector corresponds to a column in tabular format, thus in the frame terminology
   * this is denoted as getCol (short form of column).
   *
   * @return number of vectors
   */
  def colCount: Int

  /**
   * Returns an array of vector names, the names are ordered by the position of the vectors.
   * <p>
   * Each vector has it's own name. Inside a frame a specific vector could be named differently.
   * However, the default name for a vector inside a frame is the vector's name.
   *
   * @return array of vector names
   */
  def colNames: Array[String]

  /**
   * Returns the getIndex (position) of the vector inside the frame given the vector's name as parameter.
   *
   * @param name vector's name
   * @return column position inside the frame corresponding to the vector with the specified name
   */
  def colIndex(name: String): Int

  /**
   * Returns a vector reference for column at given position
   *
   * @param col position of the column inside the frame
   * @return a vector getType reference
   */
  def col(colIndex: Int): Vector

  /**
   * Returns a vector reference for column with given name
   *
   * @param name name of the column inside the frame
   * @return a vector getType reference
   */
  def col(name: String): Vector

  /**
   * Returns row identifier for a specific column
   *
   * @param row row for which row identifier is returned
   * @return row identifier
   */
  def rowId(row: Int): Int

  def isMappedFrame: Boolean

  /**
   * Returns the solid frame which contains the data. Solid frames return themselves,
   * mapped frames returns the solid frame which contains the data.
   *
   * @return
   */
  def sourceFrame: Frame

  def mapping: Mapping

  /**
   * Convenient shortcut to call
   *
   * @param row row number
   * @param col column number
   * @return numeric setValue
   */
  def getValue(row: Int, col: Int): Double

  /**
   * Convenient shortcut to call {@link rapaio.data.Vector#getValue(int)} for a given column.
   *
   * @param row     row number
   * @param colName column name
   * @return numeric setValue
   */
  def getValue(row: Int, colName: String): Double

  /**
   * Convenient shortcut method to call {@link rapaio.data.Vector#setValue(int, double)} for a given column.
   *
   * @param row   row number
   * @param col   column number
   * @param value numeric getValue
   */
  def setValue(row: Int, col: Int, value: Double)

  /**
   * Convenient shortcut method to call {@link rapaio.data.Vector#setValue(int, double)} for a given column.
   *
   * @param row     row number
   * @param colName column name
   * @param value   numeric getValue
   */
  def setValue(row: Int, colName: String, value: Double)

  /**
   * Convenient shortcut method for calling {@link rapaio.data.Vector#getIndex(int)} for a given column.
   *
   * @param row row number
   * @param col column number
   * @return setIndex getValue
   */
  def getIndex(row: Int, col: Int): Int

  /**
   * Convenient shortcut method for calling {@link rapaio.data.Vector#getIndex(int)} for a given column.
   *
   * @param row     row number
   * @param colName column name
   * @return setIndex getValue
   */
  def getIndex(row: Int, colName: String): Int

  /**
   * Convenient shortcut method for calling {@link rapaio.data.Vector#setIndex(int, int)} for given column.
   *
   * @param row   row number
   * @param col   column number
   * @param value setIndex getValue
   */
  def setIndex(row: Int, col: Int, value: Int)

  /**
   * Convenient shortcut method for calling {@link rapaio.data.Vector#setIndex(int, int)} for given column.
   *
   * @param row     row number
   * @param colName column name
   * @param value   setIndex getValue
   */
  def setIndex(row: Int, colName: String, value: Int)

  /**
   * Convenient shortcut method for calling {@link rapaio.data.Vector#getLabel(int)} for given column.
   *
   * @param row row number
   * @param col column number
   * @return nominal getLabel getValue
   */
  def getLabel(row: Int, col: Int): String

  /**
   * Convenient shortcut method for calling {@link rapaio.data.Vector#getLabel(int)} for given column.
   *
   * @param row     row number
   * @param colName column name
   * @return nominal getLabel getValue
   */
  def getLabel(row: Int, colName: String): String

  /**
   * Convenient shortcut method for calling {@link rapaio.data.Vector#setLabel(int, String)} for given column.
   *
   * @param row   row number
   * @param col   column number
   * @param value nominal getLabel getValue
   */
  def setLabel(row: Int, col: Int, value: String)

  /**
   * Convenient shortcut method for calling {@link rapaio.data.Vector#setLabel(int, String)} for given column.
   *
   * @param row     row number
   * @param colName column name
   * @param value   nominal getLabel getValue
   */
  def setLabel(row: Int, colName: String, value: String)

  def isMissing(row: Int, col: Int): Boolean

  def isMissing(row: Int, colName: String): Boolean

  def isMissing(row: Int): Boolean

  def setMissing(row: Int, col: Int)

  def setMissing(row: Int, colName: String)
}