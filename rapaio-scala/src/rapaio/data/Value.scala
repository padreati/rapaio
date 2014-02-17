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
class Value(protected var rows: Int, private val capacity: Int, private val fill: Double) extends AbstractVector {
  require(capacity >= 0, "Illegal capacity: " + capacity)
  require(rowCount >= 0, "Illegal row count: " + this.rows)
  require(rows <= capacity, "Illegal row count" + rows + " less than capacity:" + capacity)

  private var data: Array[Double] = null
  this.data = new Array[Double](capacity)
  if (fill != 0) Arrays.fill(data, 0, rows, fill)

  def this() {
    this(0, 0, 0)
    data = Value.EMPTY_DATA
  }

  def this(rows: Int) {
    this(rows, rows, Double.NaN)
  }

  def this(rows: Int, capacity: Int) {
    this(rows, capacity, Double.NaN)
  }

  private def hugeCapacity(minCapacity: Int): Int = {
    if (minCapacity < 0) throw new OutOfMemoryError
    else if (minCapacity > Value.MAX_ARRAY_SIZE) Int.MaxValue else Value.MAX_ARRAY_SIZE
  }

  def isNominal: Boolean = false

  def isNumeric: Boolean = true

  private def ensureCapacityInternal(minCapacity: Int) {
    var capacity = minCapacity
    if (data eq Value.EMPTY_DATA) {
      capacity = math.max(Value.DEFAULT_CAPACITY, minCapacity)
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
    if (newCapacity - Value.MAX_ARRAY_SIZE > 0) newCapacity = hugeCapacity(minCapacity)
    data = Arrays.copyOf(data, newCapacity)
  }

  private def rangeCheck(index: Int) {
    if (index >= rows || index < 0) throw new IndexOutOfBoundsException("Index: " + index + ", Size: " + rows)
  }

  def isMappedVector: Boolean = false

  def source: Vector = this

  def mapping: Mapping = null

  def rowCount: Int = rows

  def rowId(row: Int): Int = row

  def getDictionary: Array[String] = {
    throw new RuntimeException("Operation not available for numeric vectors.")
  }

  def setDictionary(dict: Array[String]) {
    throw new RuntimeException("Operation not available for numeric vectors.")
  }

  def isMissing(row: Int): Boolean = {
    return values(row) != values(row)
  }

  def setMissing(row: Int) = values(row) = Value.missingValue

  def addMissing = values ++ Value.missingValue

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

  def clear = rows = 0

  def trimToSize = if (rows < data.length) data = Arrays.copyOf(data, rows)

  def ensureCapacity(minCapacity: Int) {
    val minExpand: Int = if ((data ne Value.EMPTY_DATA)) 0 else Value.DEFAULT_CAPACITY
    if (minCapacity > minExpand) {
      if (minCapacity - data.length > 0) grow(minCapacity)
    }
  }

  override def toString: String = "Value[" + rowCount + "]"

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

    override def update(row: Int, value: String): Unit = {
      throw new RuntimeException("Operation not available for numeric vectors.")
    }

    override def ++(value: String): Unit = {
      throw new RuntimeException("Operation not available for numeric vectors.")
    }
  }
}

object Value {
  private final val MAX_ARRAY_SIZE: Int = Integer.MAX_VALUE - 8
  private final val DEFAULT_CAPACITY: Int = 10
  private final val EMPTY_DATA: Array[Double] = Array[Double](DEFAULT_CAPACITY)
  private final val missingValue: Double = Double.NaN

  def apply(values: Array[Double]): Value = {
    val x = new Value(values.length)
    x.data = Arrays.copyOf(values, values.length)
    x.rows = values.length
    x
  }

  def apply(values: Array[Int]): Value = {
    val x = new Value(values.length)
    x.data = new Array[Double](values.length)
    for (i <- 0 until values.length) {
      x.data(i) = values(i)
    }
    x.rows = values.length
    x
  }

  def apply(from: Int = 0, to: Int, f: (Int) => Double): Value = {
    val value = new Value
    for (i <- from until to) {
      value.values ++ f(i)
    }
    value
  }
}

