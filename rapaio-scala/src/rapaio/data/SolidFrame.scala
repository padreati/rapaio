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
import scala.collection.mutable

/**
 * A frame which is not mapped, its values are contained in vectors.
 *
 * @author Aurelian Tutuianu
 */
class SolidFrame extends AbstractFrame {

  var _rows: Int = 0
  var _vectors: Array[Vector] = _
  var _names: Array[String] = _
  var _colIndex: mutable.HashMap[String, Int] = _

  //  def this(rows: Int, vectors: List[Vector], names: List[String]) {
  //    this()
  //    `this`(rows, vectors, names.toArray(Array[String]))
  //  }
  //
  //  def this(rows: Int, vectors: List[Vector], names: Array[String]) {
  //    this()
  //    {
  //      var i: Int = 0
  //      while (i < vectors.size) {
  //        {
  //          if (vectors.get(i).isMappedVector) throw new IllegalArgumentException("Not allowed mapped vectors in solid frame")
  //        }
  //        ({
  //          i += 1; i - 1
  //        })
  //      }
  //    }
  //    this.rows = rows
  //    this.vectors = new Array[Vector](vectors.size)
  //    this.colIndex = new HashMap[String, Integer]
  //    this.names = new Array[String](vectors.size)
  //    {
  //      var i: Int = 0
  //      while (i < vectors.size) {
  //        {
  //          this.vectors(i) = vectors.get(i)
  //          this.colIndex.put(names(i), i)
  //          this.names(i) = names(i)
  //        }
  //        ({
  //          i += 1; i - 1
  //        })
  //      }
  //    }
  //  }
  //
  //  def this(rows: Int, vectors: Array[Vector], names: Array[String]) {
  //    this()
  //    `this`(rows, Arrays.asList(vectors), names)
  //  }

  def rowCount: Int = _rows

  def colCount: Int = _vectors.length

  def rowId(row: Int): Int = _rows

  def isMappedFrame: Boolean = false

  def sourceFrame: Frame = this

  def mapping: Mapping = null

  def colNames: Array[String] = _names

  def colIndex(colName: String): Int = {
    if (!_colIndex.contains(colName))
      throw new IllegalArgumentException("Column name is invalid")
    else _colIndex(colName)
  }

  def col(colIndex: Int): Vector = {
    if (colIndex >= 0 && colIndex < _vectors.length) _vectors(colIndex)
    else throw new IllegalArgumentException("Invalid column index")
  }

  def col(name: String): Vector = col(colIndex(name))

  override def isMissing(row: Int, colIndex: Int): Boolean = col(colIndex).missing(row)

  override def isMissing(row: Int, colName: String): Boolean = col(colName).missing(row)
}