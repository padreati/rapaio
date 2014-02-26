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

import rapaio.data.mapping.{MappedFrame, Mapping}
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
   * @param colIndex position of the column inside the frame
   * @return a vector getType reference
   */
  def col(colIndex: Int): Feature

  /**
   * Returns a vector reference for column with given name
   *
   * @param name name of the column inside the frame
   * @return a vector getType reference
   */
  def col(name: String): Feature

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

  def missing: Missing = new Missing

  def values: Values = new Values

  def indexes: Indexes = new Indexes

  def labels: Labels = new Labels

  class Missing {
    def apply(row: Int, colIndex: Int): Boolean = col(colIndex).missing(row)

    def apply(row: Int, colName: String): Boolean = col(colName).missing(row)

    def apply(row: Int): Boolean = colNames.exists((colName) => col(colName).missing(row))

    def update(row: Int, colIndex: Int, value: Boolean): Unit = col(colIndex).missing(row) = true

    def update(row: Int, colName: String, value: Boolean): Unit = col(colName).missing(row) = true
  }

  class Values {
    def apply(row: Int, colIndex: Int): Double = col(colIndex).values(row)

    def apply(row: Int, colName: String): Double = col(colName).values(row)

    def update(row: Int, colIndex: Int, x: Double): Unit = col(colIndex).values(row) = x

    def update(row: Int, colName: String, x: Double): Unit = col(colName).values(row) = x
  }

  class Indexes {
    def apply(row: Int, colIndex: Int): Int = col(colIndex).indexes(row)

    def apply(row: Int, colName: String): Int = col(colName).indexes(row)

    def update(row: Int, colIndex: Int, x: Int): Unit = col(colIndex).indexes(row) = x

    def update(row: Int, colName: String, x: Int): Unit = col(colName).indexes(row) = x
  }

  class Labels {
    def apply(row: Int, colIndex: Int): String = col(colIndex).labels(row)

    def apply(row: Int, colName: String): String = col(colName).labels(row)

    def update(row: Int, colIndex: Int, x: String): Unit = col(colIndex).labels(row) = x

    def update(row: Int, colName: String, x: String): Unit = col(colName).labels(row) = x
  }


  def filter(f: (Frame, Int) => Boolean): MappedFrame = {
    val mapping = new Mapping
    for (i <- 0 until rowCount) {
      if (f(this, i)) mapping.add(rowId(i))
    }
    new MappedFrame(sourceFrame, mapping)
  }

  def binarySplit(f: (Frame, Int) => Boolean): (MappedFrame, MappedFrame) = {
    val leftMapping = new Mapping
    val rightMapping = new Mapping
    for (i <- 0 until rowCount) {
      if (f(this, i)) leftMapping.add(rowId(i))
      else rightMapping.add(rowId(i))
    }
    (new MappedFrame(this, leftMapping), new MappedFrame(this, rightMapping))
  }
}

object Frame {

  /**
   * Builds a frame only with Value features, with given column names.
   *
   * @param rows number of rows of the resulted frame
   * @param names column names
   * @return new solid frame only with Value columns
   */
  def matrix(rows: Int, names: Array[String]): Frame = {
    val features = new Array[Feature](names.length)
    for (i <- 0 until names.length) features(i) = new Value()
    new SolidFrame(rows, features, names)
  }
}