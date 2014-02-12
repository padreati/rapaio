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
 * Random access list of observed values (observations) for a specific variable.
 *
 * @author Aurelian Tutuianu
 */
abstract trait Vector extends Serializable {

  def isNominal: Boolean

  def isNumeric: Boolean

  def isMappedVector: Boolean

  def getSourceVector: Vector

  def getMapping: Mapping

  /**
   * Number of observations contained by the vector.
   *
   * @return size of vector
   */
  def getRowCount: Int

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
  def getRowId(row: Int): Int

  /**
   * Returns numeric setValue for the observation specified by {@code row}.
   * <p>
   * Returns valid values for numerical vector types, otherwise the method
   * returns unspeified values.
   *
   * @param row
   * @return numerical setValue
   */
  def getValue(row: Int): Double

  /**
   * Set numeric setValue for the observation specified by {@param row} to {@param setValue}.
   * <p>
   * Returns valid values for numerical vector types, otherwise the method
   * returns unspeified values.
   *
   * @param row   position of the observation
   * @param value numeric setValue from position { @param row}
   */
  def setValue(row: Int, value: Double)

  def addValue(value: Double)

  def addValue(row: Int, value: Double)

  /**
   * Returns getIndex setValue for the observation specified by {@param row}
   *
   * @param row position of the observation
   * @return getIndex setValue
   */
  def getIndex(row: Int): Int

  /**
   * Set getIndex setValue for the observation specified by {@param row}.
   *
   * @param row   position of the observation
   * @param value getIndex setValue for the observation
   */
  def setIndex(row: Int, value: Int)

  def addIndex(value: Int)

  def addIndex(row: Int, value: Int)

  /**
   * Returns nominal getLabel for the observation specified by {@param row}.
   *
   * @param row position of the observation
   * @return getLabel setValue for the observation
   */
  def getLabel(row: Int): String

  /**
   * Set nominal getLabel for the observation specified by {@param row}.
   *
   * @param row   position of the observation
   * @param value getLabel setValue of the observation
   */
  def setLabel(row: Int, value: String)

  def addLabel(value: String)

  def addLabel(row: Int, value: String)

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
  def getDictionary: Array[String]

  def setDictionary(dict: Array[String])

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
}

/**
 * Base class for a vector which enforces to read-only name given at construction time.
 *
 * @author <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a>
 */
abstract class AbstractVector extends Vector {
  override def toString: String = {
    return "Vector{ size='" + getRowCount + "\'}"
  }

  def toValueArray: Array[Double] = {
    val list = new Array[Double](getRowCount)
    var i: Int = 0
    while (i < getRowCount) {
      list(i) = getValue(i)
      i += 1;
    }
    return list
  }

  def toIndexArray: Array[Int] = {
    val list = new Array[Int](getRowCount)
    var i: Int = 0
    while (i < getRowCount) {
      list(i) = getIndex(i)
      i += 1;
    }
    return list
  }
}


