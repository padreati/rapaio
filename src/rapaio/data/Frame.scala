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
import rapaio.printer.Printable

/**
 * Random access list of observed values for multiple variables.
 * <p>
 * The observed values are represented in a tabular format.
 * Rows corresponds to observations and columns corresponds to
 * observed (measured) statistical variables.
 *
 * @author Aurelian Tutuianu
 */
trait Frame extends Serializable with Printable {
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

  def split(f: (Frame, Int) => Boolean): (MappedFrame, MappedFrame) = {
    val left = new Mapping
    val right = new Mapping
    for (i <- 0 until rowCount) {
      if (f(this, i)) left.add(rowId(i))
      else right.add(rowId(i))
    }
    (new MappedFrame(sourceFrame, left), new MappedFrame(sourceFrame, right))
  }

  def weightedSplit(weights: Value, f: (Frame, Int) => Boolean): ((MappedFrame, Value), (MappedFrame, Value)) = {
    val left = new Mapping
    val right = new Mapping
    val leftWeights = new Value()
    val rightWeights = new Value()
    for (i <- 0 until rowCount) {
      if (f(this, i)) {
        left.add(rowId(i))
        leftWeights.values ++ weights.values(i)
      } else {
        right.add(rowId(i))
        rightWeights.values ++ weights.values(i)
      }
    }
    ((new MappedFrame(sourceFrame, left), leftWeights), (new MappedFrame(sourceFrame, right), rightWeights))
  }

  override def buildSummary(sb: StringBuilder): Unit = {
    sb.append(">> frame summary\n")
    sb.append("rowCount: %d, colCount: %d\n".format(rowCount, colCount))

    sb.append("cols:\n\t")
    val slide = colNames.map(colName => "%s[%s]".format(colName, col(colName).typeName)).sliding(5, 2)
    slide.foreach(s => sb.append(s mkString ",").append("\n\t"))

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
    for (i <- 0 until names.length) features(i) = new Value(rows, rows, 0)
    new SolidFrame(rows, features, names)
  }

  def solid(rows: Int, pair: (String, Feature)*): SolidFrame = {
    solid(rows, pair.toList)
  }

  def solid(rows: Int, pair: List[(String, Feature)]): SolidFrame = {
    require(rows >= 0, "rows must have a positive value")
    require(pair.forall(p => p._2.rowCount >= rows), "all features must have at least the same row count as rows")
    require(pair.forall(p => !p._2.isMappedFeature), "features should not be mapped")

    val cols = pair.unzip
    new SolidFrame(rows, cols._2.toArray, cols._1.toArray)
  }

  def bind(rows: Int, left: Frame, right: Frame): SolidFrame = {

    require(!left.isMappedFrame || !right.isMappedFrame, "operation not available on mapped frames")
    require(rows <= left.rowCount, "rows must be no greater then rowCount of the left frame")
    require(rows <= right.rowCount, "rows must be no greate then rowCount of the right frame")

    var pairs = List[(String, Feature)]()
    for (i <- 0 until left.colCount) pairs = List((left.colNames(i), left.col(i))) ::: pairs
    for (i <- 0 until right.colCount) pairs = List((right.colNames(i), right.col(i))) ::: pairs
    solid(rows, pairs.reverse)
  }
}