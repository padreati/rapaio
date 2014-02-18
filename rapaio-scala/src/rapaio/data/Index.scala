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

import java.util.Arrays
import rapaio.data.mapping.Mapping

/**
 * User: Aurelian Tutuianu <padreati@yahoo.com>
 */
class Index(protected var rows: Int,
            private val capacity: Int,
            private val fill: Int) extends AbstractFeature {

  require(capacity >= 0, "Illegal capacity: " + capacity)
  require(rows >= 0, "Illegal row count: " + rows)
  require(rows <= capacity, "Illegal row count" + rows + " less than capacity:" + capacity)

  var data: Array[Int] = new Array[Int](capacity)
  if (fill != 0) Arrays.fill(data, 0, rows, fill)

  def this() {
    this(0, 0, 0)
  }

  private def ensureCapacityInternal(minCapacity: Int) {
    var capacity = minCapacity
    if (data eq Index.EMPTY_DATA) {
      capacity = math.min(Index.DEFAULT_CAPACITY, minCapacity)
    }
    if (capacity - data.length > 0) grow(capacity)
  }

  private def hugeCapacity(minCapacity: Int): Int = {
    if (minCapacity < 0) throw new OutOfMemoryError
    return if (minCapacity > Index.MAX_ARRAY_SIZE) Integer.MAX_VALUE else Index.MAX_ARRAY_SIZE
  }

  def isNominal: Boolean = false

  def isNumeric: Boolean = true

  private def grow(minCapacity: Int) {
    val oldCapacity: Int = data.length
    var newCapacity: Int = oldCapacity + (oldCapacity >> 1)
    if (newCapacity - minCapacity < 0) newCapacity = minCapacity
    if (newCapacity - Index.MAX_ARRAY_SIZE > 0) newCapacity = hugeCapacity(minCapacity)
    data = Arrays.copyOf(data, newCapacity)
  }

  private def rangeCheck(index: Int) {
    if (index > rows || index < 0) throw new IndexOutOfBoundsException(outOfBoundsMsg(index))
  }

  private def outOfBoundsMsg(index: Int): String = "Index: " + index + ", Size: " + rows

  def isMappedVector: Boolean = false

  def source: Feature = this

  def mapping: Mapping = null

  def rowCount: Int = rows

  def rowId(row: Int): Int = row

  def remove(index: Int) {
    rangeCheck(index)
    val numMoved: Int = rows - index - 1
    if (numMoved > 0) System.arraycopy(data, index + 1, data, index, numMoved)
  }

  def removeRange(fromIndex: Int, toIndex: Int) {
    val numMoved: Int = rows - toIndex
    System.arraycopy(data, toIndex, data, fromIndex, numMoved)
    rows -= (toIndex - fromIndex)
  }

  def clear {
    rows = 0
  }

  def ensureCapacity(minCapacity: Int) {
    val minExpand: Int = if (!data.sameElements(Index.EMPTY_DATA)) 0 else Index.DEFAULT_CAPACITY
    if (minCapacity > minExpand && minCapacity - data.length > 0) grow(minCapacity)
  }

  def trimToSize {
    if (rows < data.length) {
      data = Arrays.copyOf(data, rows)
    }
  }

  override def toString: String = "Index[" + rowCount + "]"

  def missing = new Missing {
    override def apply(row: Int): Boolean = indexes(row) == Index.MISSING_VALUE

    override def update(row: Int, value: Boolean): Unit = indexes(row) = Index.MISSING_VALUE

    def ++(): Unit = indexes ++ Index.MISSING_VALUE
  }

  def values = new Values {
    override def update(i: Int, v: Double): Unit = data(i) = math.rint(v).toInt

    override def apply(i: Int): Double = data(i)

    override def ++(value: Double): Unit = {
      ensureCapacityInternal(rows + 1)
      data(rows) = math.rint(value).toInt
      rows += 1
    }
  }

  def indexes = new Indexes {
    override def apply(row: Int): Int = data(row)

    override def update(row: Int, value: Int): Unit = data(row) = value

    override def ++(value: Int): Unit = {
      ensureCapacityInternal(rows + 1)
      data(rows) = value
      rows += 1
    }
  }

  def labels = new Labels {
    override def apply(row: Int): String = ""

    override def update(row: Int, value: String): Unit = {
      throw new RuntimeException("Operation not available for getIndex vectors.")
    }

    override def ++(value: String): Unit = {
      throw new RuntimeException("Operation not available for getIndex vectors.")
    }

    def dictionary: Array[String] = {
      throw new RuntimeException("Operation not available for getIndex vectors.")
    }

    def dictionary_=(dict: Array[String]): Unit = {
      throw new RuntimeException("Operation not available for getIndex vectors.")
    }
  }
}

object Index {
  val MAX_ARRAY_SIZE = Integer.MAX_VALUE - 8
  val DEFAULT_CAPACITY = 10
  val EMPTY_DATA = Array[Int](DEFAULT_CAPACITY)
  val MISSING_VALUE: Int = Integer.MIN_VALUE

  def apply(values: Array[Int]): Index = {
    val index = new Index(values.length, values.length, 0)
    index.data = Arrays.copyOf(values, values.length)
    index.rows = values.length
    index
  }

  def apply(values: Array[Double]): Index = {
    val index = new Index(values.length, values.length, 0)
    for (i <- 0 until values.length)
      index.data(i) = math.rint(values(i)).toInt
    index.rows = values.length
    index
  }

  def apply(values: Int*): Index = {
    val index = new Index(values.length, values.length, 0)
    values.iterator.copyToArray(index.data)
    index
  }
}

