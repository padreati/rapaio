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
import scala.collection.mutable

/**
 * Nominal vector contains values for nominal or categorical observations.
 * <p>
 * The domain of the definition is called getDictionary and is
 * given at construction time.
 * <p>
 * This vector accepts two getValue representation: as labels and as indexes.
 * <p>
 * Label representation is the natural representation since in experiments
 * the nominal vectors are given as string values.
 * <p>
 * The getIndex representation is learn based on the canonical form of the
 * term getDictionary and is used often for performance reasons instead of
 * getLabel representation, where the actual getLabel getValue does not matter.
 *
 * @author Aurelian Tutuianu
 */
class Nominal(protected var rows: Int, private var dictionary: List[String]) extends AbstractVector {
  private var _dict = new mutable.MutableList[String]
  private var data = Array[Int](rows)
  private var reverse = new mutable.HashMap[String, Int]

  for (next <- dictionary) {
    if (!_dict.contains(next)) {
      _dict += next
    }
    reverse += (next -> _dict.size)
  }

  def this() {
    this(0, List.empty[String])
  }

  def this(size: Int, dict: Array[String]) {
    this(size, dict.toList)
  }

  def isNominal: Boolean = true

  def isNumeric: Boolean = false

  private def grow(minCapacity: Int) {
    var newCapacity = data.length + (data.length >> 1)
    if (newCapacity - minCapacity < 0) newCapacity = minCapacity
    data = Arrays.copyOf(data, newCapacity)
  }

  def isMappedVector: Boolean = false

  def source: Vector = this

  def mapping: Mapping = null

  def rowCount: Int = rows

  def rowId(row: Int): Int = row

  def isMissing(row: Int): Boolean = Nominal.missingIndex == indexes(row)

  def setMissing(row: Int) = indexes(row) = Nominal.missingIndex

  def addMissing: Unit = indexes ++ Nominal.missingIndex

  def remove(index: Int) {
    val numMoved: Int = rows - index - 1
    if (numMoved > 0) System.arraycopy(data, index + 1, data, index, numMoved)
  }

  def removeRange(fromIndex: Int, toIndex: Int) {
    val numMoved: Int = rows - toIndex
    System.arraycopy(data, toIndex, data, fromIndex, numMoved)
    val newSize: Int = rows - (toIndex - fromIndex)
    rows = newSize
  }

  def clear {
    rows = 0
  }

  def trimToSize {
    if (rows < data.length) {
      data = Arrays.copyOf(data, rows)
    }
  }

  def ensureCapacity(minCapacity: Int) {
    if (minCapacity - data.length > 0) grow(minCapacity)
  }

  override def toString: String = "Nominal[" + rowCount + "]"

  def values = new Values {
    def apply(row: Int): Double = data(row)

    def update(row: Int, value: Double) = data(row) = math.rint(value).toInt

    override def ++(value: Double): Unit = {
      val row = math.rint(value).toInt
      val label = _dict(data(row))
      ensureCapacity(rows + 1)
      if (!reverse.contains(label)) {
        _dict += label
        reverse.put(label, reverse.size)
      }
      data(rows) = reverse(label)
      rows += 1
    }
  }

  def indexes = new Indexes {
    override def apply(row: Int): Int = data(row)

    override def update(row: Int, value: Int): Unit = data(row) = value

    override def ++(value: Int): Unit = {
      val label = _dict(value)
      ensureCapacity(rows + 1)
      if (!reverse.contains(label)) {
        _dict += label
        reverse.put(label, reverse.size)
      }
      data(rows) = reverse(label)
      rows += 1
    }
  }

  def labels = new Labels {
    override def apply(row: Int): String = _dict(data(row))

    override def update(row: Int, value: String): Unit = {
      if (value equals Nominal.missingValue) {
        data(row) = Nominal.missingIndex
        return
      }
      if (!reverse.contains(value)) {
        _dict += value
        reverse += (value -> reverse.size)
      }
      data(row) = reverse(value)
    }

    override def ++(value: String): Unit = {
      ensureCapacity(rows + 1)
      if (!reverse.contains(value)) {
        _dict += value
        reverse.put(value, reverse.size)
      }
      data(rows) = reverse(value)
      rows += 1
    }

    override def dictionary_=(dict: Array[String]): Unit = {
      val oldDict = _dict;
      val oldReverse = reverse;

      _dict = new mutable.MutableList();
      reverse = new mutable.HashMap();
      _dict += "?";
      reverse += ("?" -> 0);

      for (i <- 0 until dict.length) {
        if (!reverse.contains(dict(i))) {
          _dict += dict(i);
          reverse += (dict(i) -> reverse.size);
        }
      }

      for (i <- 0 until rows) {
        if (!reverse.contains(oldDict(data(i)))) {
          _dict = oldDict;
          reverse = oldReverse;
          throw new IllegalArgumentException("new getDictionary does not contains all old labels");
        }
      }

      for (i <- 0 until rows) {
        data(i) = reverse(oldDict(data(i)));
      }
    }

    override def dictionary: Array[String] = _dict.toArray
  }
}

object Nominal {
  private val missingValue: String = "?"
  private val missingIndex: Int = 0
}

