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

import rapaio.data.mapping.MappedFeature

/**
 * @author <a href="email:padreati@yahoo.com>Aurelian Tutuianu</a>
 */
class VInst(private val _row: Int, private val _vector: Feature) {

  def rowId: Int = _vector.rowId(_row)

  def row: Int = _row

  def vector: Feature = _vector

  def value = _vector.values(_row)

  def value_(x: Double) = _vector.values(_row) = x

  def index = _vector.indexes(_row)

  def index_(x: Int) = _vector.indexes(_row) = x

  def label = _vector.labels(_row)

  def label_(x: String) = _vector.labels(_row) = x

  override def toString: String = {
    _vector match {
      case _vector: MappedFeature => toString(_vector.source)
      case _ => toString(_vector)
    }
  }

  private def toString(v: Feature): String = {
    v match {
      case v: Value => v.values(_row).toString
      case v: Index => v.indexes(_row).toString
      case v: Nominal => v.labels(_row).toString
      case _ => super.toString
    }
  }
}