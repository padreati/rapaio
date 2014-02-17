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
 * Random access list of observed values for a specific variable.
 *
 * @author Aurelian Tutuianu
 */
abstract trait Vector extends Serializable {

  /**
   * @return true is the vector can be used as a nominal variable, false otherwise
   */
  def isNominal: Boolean

  /**
   * @return true if the vector can be treated as a numeric variable, false otherwise
   */
  def isNumeric: Boolean

  /**
   * @return true if the vector is mapped (is a mapping over an original vector),
   *         false otherwise
   */
  def isMappedVector: Boolean

  /**
   * @return the source vector if is a mapping vector, otherwise
   *         the same instance is returned
   */
  def source: Vector

  /**
   * @return mapping which consists of all rowId for all the available rows, null if
   *         the vector is not mapped
   */
  def mapping: Mapping

  /**
   * @return number of observations contained by the vector
   */
  def rowCount: Int

  /**
   * Returns observation identifier which is an integer.
   * <p>
   * When a vector or frame is created from scratch as a solid vector/frame then
   * row identifiers are the row numbers. When the vector/frame wraps other
   * vector/frame then row identifier is the wrapped row identifier.
   * <p>
   * This is mostly used to keep track of the original row numbers even after a series
   * of transformations which use wrapped vectors/frames.
   *
   * @param row row for which row identifier is returned
   * @return row identifier
   */
  def rowId(row: Int): Int

  /**
   * Returns true if the setValue for the observation specified by {@param row} is missing, not available.
   * <p>
   * A missing setValue for the observation means taht the measurement
   * was not completed or the result of the measurement was not documented,
   * thus the setValue is not available for analysis.
   *
   * @param row position of the observation
   * @return true if the observation measurement is not specified
   */
  def isMissing(row: Int): Boolean

  /**
   * Set the setValue of the observation specified by {@param row} as missing, not available for analysis.
   *
   * @param row position of the observation.
   */
  def setMissing(row: Int)

  def addMissing

  def remove(row: Int)

  def removeRange(from: Int, to: Int)

  def clear

  def trimToSize

  def ensureCapacity(minCapacity: Int)

  def toValueArray: Array[Double]

  def toIndexArray: Array[Int]

  def toLabelArray: Array[String]

  def values: Values

  def indexes: Indexes

  def labels: Labels

  abstract class Values {
    def apply(row: Int): Double

    def update(row: Int, value: Double): Unit

    def ++(value: Double): Unit
  }

  abstract class Indexes {

    def apply(row: Int): Int

    def update(row: Int, value: Int): Unit

    def ++(value: Int): Unit
  }

  abstract class Labels {

    def apply(row: Int): String

    def update(row: Int, value: String): Unit

    def ++(value: String): Unit

    /**
     * Returns the term getDictionary used by the nominal values.
     * <p>
     * Term getDictionary contains all the nominal labels used by
     * observations and might contain also additional nominal labels.
     * Term getDictionary defines the domain of the definition for the nominal vector.
     * <p>
     * The term getDictionary contains nominal labels sorted in lexicografical order,
     * so binary search techniques may be used on this vector.
     * <p>
     * For other vector types like numerical ones this method returns nothing.
     *
     * @return term getDictionary defined by the nominal vector.
     */
    def dictionary: Array[String]

    def dictionary_=(dict: Array[String]): Unit

  }

  def instances: Array[VInst] = {
    val inst = new Array[VInst](rowCount)
    for (i <- 0 until rowCount)
      inst(i) = new VInst(i, this)
    inst
  }
}

/**
 * Base class for a vector which enforces to read-only name given at construction time.
 *
 * @author <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a>
 */
abstract class AbstractVector extends Vector {
  override def toString: String = {
    "Vector{ size='" + rowCount + "\'}"
  }

  def toValueArray: Array[Double] = {
    val list = new Array[Double](rowCount)
    var i: Int = 0
    while (i < rowCount) {
      list(i) = values(i)
      i += 1
    }
    list
  }

  def toIndexArray: Array[Int] = {
    val list = new Array[Int](rowCount)
    var i: Int = 0
    while (i < rowCount) {
      list(i) = indexes(i)
      i += 1
    }
    list
  }

  def toLabelArray: Array[String] = {
    val list = new Array[String](rowCount)
    var i: Int = 0
    while (i < rowCount) {
      list(i) = labels(i)
      i += 1
    }
    list
  }

}


