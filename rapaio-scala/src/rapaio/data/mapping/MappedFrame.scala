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

import rapaio.data.AbstractFrame
import rapaio.data.Frame
import rapaio.data.Feature
import scala.collection.mutable

/**
 * A frame which is learn on the base of another frame with
 * the row order and row selection specified by a
 * getMapping give at construction time.
 * <p>
 * This frame does not hold actual values, it delegate the behavior
 * to the wrapped frame, thus the wrapping affects only the getRowCount
 * selected anf the order of these getRowCount.
 *
 * @author <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a>
 */
class MappedFrame extends AbstractFrame {

  private var _mapping: Mapping = _
  private var _source: Frame = _
  private var _vectors: mutable.HashMap[Integer, Feature] = _

  //  def this(df: Frame, mapping: Mapping) {
  //    this()
  //    if (df.isMappedFrame) {
  //      throw new IllegalArgumentException("Not allowed mapped frames as source")
  //    }
  //    this.mapping = mapping
  //    this.source = df
  //  }

  def rowCount: Int = _mapping.size

  def colCount: Int = _source.colCount

  def rowId(row: Int): Int = _mapping(row)

  def isMappedFrame: Boolean = true

  def sourceFrame: Frame = _source

  def mapping: Mapping = _mapping

  def colNames: Array[String] = _source.colNames

  def colIndex(colName: String): Int = _source.colIndex(colName)

  def col(colIndex: Int): Feature = {
    if (!_vectors.contains(colIndex)) {
      _vectors(colIndex) = new MappedFeature(_source.col(colIndex), _mapping)
    }
    _vectors(colIndex)
  }

  def col(colName: String): Feature = col(colIndex(colName))
}