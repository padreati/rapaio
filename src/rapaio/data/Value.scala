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
class Value(protected var rows: Int,
            private val capacity: Int,
            private val fill: Double) extends Feature {
  require(capacity >= 0, "Illegal capacity: " + capacity)
  require(rowCount >= 0, "Illegal row count: " + this.rows)
  require(rows <= capacity, "Illegal row count" + rows + " less than capacity:" + capacity)

  private var data = new Array[Double](capacity)
  if (fill != 0) Arrays.fill(data, 0, rows, fill)

  override def shortName: String = Value.ShortName

  def this() {
    this(0, 0, 0)
    data = Value.EmptyData
  }

  def this(rows: Int) {
    this(rows, rows, Double.NaN)
  }

  def this(rows: Int, capacity: Int) {
    this(rows, capacity, Double.NaN)
  }

  private def hugeCapacity(minCapacity: Int): Int = {
    if (minCapacity < 0) throw new OutOfMemoryError
    else if (minCapacity > Value.MaxArraySize) Int.MaxValue else Value.MaxArraySize
  }

  def isNominal: Boolean = false

  def isNumeric: Boolean = true

  private def ensureCapacityInternal(minCapacity: Int) {
    var capacity = minCapacity
    if (data eq Value.EmptyData) {
      capacity = math.max(Value.DefaultCapacity, minCapacity)
    }
    if (capacity - data.length > 0) grow(capacity)
  }

  /**
   * Increases the capacity to ensure that it can hold at least the
   * number of elements specified by the minimum capacity argument.
   *
   * @param minCapacity the desired minimum capacity
   */
  private def grow(minCapacity: Int) {
    val oldCapacity: Int = data.length
    var newCapacity: Int = oldCapacity + (oldCapacity >> 1)
    if (newCapacity - minCapacity < 0) newCapacity = minCapacity
    if (newCapacity - Value.MaxArraySize > 0) newCapacity = hugeCapacity(minCapacity)
    data = Arrays.copyOf(data, newCapacity)
  }

  private def rangeCheck(index: Int) {
    if (index >= rows || index < 0) throw new IndexOutOfBoundsException("Index: " + index + ", Size: " + rows)
  }

  def isMappedFeature: Boolean = false

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

  def clear() = rows = 0

  def trimToSize() = {
    if (rows < data.length) {
      val copy = new Array[Double](rows)
      Array.copy(data, 0, copy, 0, rows)
      data = copy
    }
  }

  def ensureCapacity(minCapacity: Int) {
    val minExpand: Int = if (data != Value.EmptyData) 0 else Value.DefaultCapacity
    if (minCapacity > minExpand) {
      if (minCapacity - data.length > 0) grow(minCapacity)
    }
  }

  override def toString: String = "Value[" + rowCount + "]"

  var missing = new Missing {
    override def apply(row: Int): Boolean = values(row) != values(row)

    override def update(row: Int, value: Boolean): Unit = values(row) = Value.missingValue

    override def ++(): Unit = values ++ Value.missingValue
  }

  val values = new Values {
    override def update(i: Int, v: Double): Unit = data(i) = v

    override def apply(i: Int): Double = data(i)

    override def ++(value: Double): Unit = {
      ensureCapacityInternal(rows + 1)
      data(rows) = value
      rows += 1
    }
  }

  val indexes = new Indexes {

    override def apply(row: Int): Int = math.rint(data(row)).toInt

    override def update(row: Int, value: Int): Unit = data(row) = value

    override def ++(value: Int): Unit = {
      ensureCapacityInternal(rows + 1)
      data(rows) = value
      rows += 1
    }
  }

  val labels = new Labels {
    override def apply(row: Int): String = ""

    override def update(row: Int, value: String): Unit =
      sys.error("Operation not available for numeric vectors.")

    override def ++(value: String): Unit =
      sys.error("Operation not available for numeric vectors.")

    def dictionary: Array[String] =
      sys.error("Operation not available for getIndex vectors.")

    override def dictionary_=(dict: Array[String]): Unit =
      sys.error("Operation not available for getIndex vectors.")

    override def indexOf(label: String): Option[Int] =
      sys.error("Not implemented")
  }

  def solidCopy(): Value = {
    val result = new Value(rowCount, rowCount, 0)
    for (i <- 0 until rowCount) result.values(i) = values(i)
    result
  }
}

object Value {

  val ShortName = "val"
  private val MaxArraySize: Int = Integer.MAX_VALUE - 8
  private val DefaultCapacity: Int = 10
  private val EmptyData: Array[Double] = Array[Double](DefaultCapacity)
  private val missingValue: Double = Double.NaN


  def apply(values: Array[Double]): Value = {
    val x = new Value(values.length)
    Array.copy(values, 0, x.data, 0, values.length)
    x.rows = values.length
    x
  }

  def apply(values: Double*): Value = apply(values.toArray)

  def apply(values: Array[Int]): Value = {
    val x = new Value(values.length)
    x.data = new Array[Double](values.length)
    for (i <- 0 until values.length) {
      x.data(i) = values(i)
    }
    x.rows = values.length
    x
  }

  def apply(from: Int, to: Int, f: (Int) => Double): Value = {
    val value = new Value
    for (i <- from until to) {
      value.values ++ f(i)
    }
    value
  }
}

