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


/**
 * Base class for a frame, which provides behavior for the utility
 * access methods based on row and column indexes.
 *
 * @author <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a>
 */
abstract class AbstractFrame extends Frame {
  def getValue(row: Int, colIndex: Int): Double = col(colIndex).values(row)

  def getValue(row: Int, colName: String): Double = col(colName).values(row)

  def setValue(row: Int, colIndex: Int, value: Double) = col(colIndex).values(row) = value

  def setValue(row: Int, colName: String, value: Double) = col(colName).values(row) = value

  def getIndex(row: Int, colIndex: Int): Int = col(colIndex).indexes(row)

  def getIndex(row: Int, colName: String): Int = col(colName).indexes(row)

  def setIndex(row: Int, colName: Int, value: Int) = col(colName).indexes(row) = value

  def setIndex(row: Int, colName: String, value: Int) = col(colName).indexes(row) = value

  def getLabel(row: Int, colIndex: Int): String = col(colIndex).labels(row)

  def getLabel(row: Int, colName: String): String = col(colName).labels(row)

  def setLabel(row: Int, colIndex: Int, value: String): Unit = col(colIndex).labels(row) = value

  def setLabel(row: Int, colName: String, value: String): Unit = col(colName).labels(row) = value

  def missing = new Missing {
    override def apply(row: Int, colIndex: Int): Boolean = col(colIndex).missing(row)

    override def apply(row: Int, colName: String): Boolean = col(colName).missing(row)

    override def apply(row: Int): Boolean = colNames.exists((colName) => col(colName).missing(row))

    override def update(row: Int, colIndex: Int, value: Boolean): Unit = col(colIndex).missing(row) = true

    override def update(row: Int, colName: String, value: Boolean): Unit = col(colName).missing(row) = true
  }
}
