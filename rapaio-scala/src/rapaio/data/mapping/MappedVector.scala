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

package rapaio.data.mapping

import rapaio.data.AbstractVector
import rapaio.data.Vector

/**
 * A vector which is learn on the base of another vector and the row selection
 * and order is specified by a getMapping give at construction time.
 * <p/>
 * This vector does not hold actual values, it delegate the behavior to the
 * wrapped vector, thus the wrapping affects only the getRowCount selected anf the
 * order of these getRowCount.
 *
 * @author <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a>
 */
class MappedVector(private val _source: Vector, private val _mapping: Mapping) extends AbstractVector {

  require(!_source.isMappedVector, "Now allowed mapped vector as source")

  def isNominal: Boolean = _source.isNominal

  def isNumeric: Boolean = _source.isNumeric

  def rowCount: Int = _mapping.size

  def isMappedVector: Boolean = true

  def sourceVector: Vector = _source

  def mapping: Mapping = _mapping

  def rowId(row: Int): Int = _source.rowId(_mapping(row))

  def getValue(row: Int): Double = _source.getValue(_mapping(row))

  def setValue(row: Int, value: Double) {
    _source.setValue(mapping(row), value)
  }

  def addValue(value: Double) {
    throw new IllegalArgumentException("operation not available on mapped vectors")
  }

  def addValue(row: Int, value: Double) {
    throw new IllegalArgumentException("operation not available on mapped vectors")
  }

  def getIndex(row: Int): Int = {
    return _source.getIndex(mapping(row))
  }

  def setIndex(row: Int, value: Int) {
    _source.setIndex(mapping(row), value)
  }

  def addIndex(value: Int) {
    throw new IllegalArgumentException("operation not available on mapped vectors")
  }

  def addIndex(row: Int, value: Int) {
    throw new IllegalArgumentException("operation not available on mapped vectors")
  }

  def getLabel(row: Int): String = {
    _source.getLabel(mapping(row))
  }

  def setLabel(row: Int, value: String) {
    _source.setLabel(mapping(row), value)
  }

  def addLabel(value: String) {
    throw new IllegalArgumentException("operation not available on mapped vectors")
  }

  def addLabel(row: Int, value: String) {
    throw new IllegalArgumentException("operation not available on mapped vectors")
  }

  def getDictionary: Array[String] = {
    _source.getDictionary
  }

  def setDictionary(dict: Array[String]) {
    _source.setDictionary(dict)
  }

  def isMissing(row: Int): Boolean = {
    _source.isMissing(mapping(row))
  }

  def setMissing(row: Int) {
    _source.setMissing(mapping(row))
  }

  def addMissing {
    throw new IllegalArgumentException("operation not available on mapped vectors")
  }

  def remove(row: Int) {
    throw new IllegalArgumentException("operation not available on mapped vectors")
  }

  def removeRange(from: Int, to: Int) {
    throw new IllegalArgumentException("operation not available on mapped vectors")
  }

  def clear {
    throw new IllegalArgumentException("operation not available on mapped vectors")
  }

  def trimToSize {
    throw new IllegalArgumentException("operation not available on mapped vectors")
  }

  def ensureCapacity(minCapacity: Int) {
    throw new IllegalArgumentException("operation not available on mapped vectors")
  }
}