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
class Nominal(protected var rows: Int, private var dictionary: List[String]) extends Feature {
  private var _dict = new mutable.MutableList[String]
  private var data = new Array[Int](rows)
  private var _reverse = new mutable.HashMap[String, Int]

  _dict += "?"
  _reverse += ("?" -> 0)
  for (next <- dictionary) {
    if (!_dict.contains(next)) {
      _dict += next
      _reverse += (next -> (_dict.size - 1))
    }
  }

  override def typeName: String = "nom"

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

  def isMappedFeature: Boolean = false

  def source: Feature = this

  def mapping: Mapping = null

  def rowCount: Int = rows

  def rowId(row: Int): Int = row

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

  def clear() {
    rows = 0
  }

  def trimToSize() {
    if (rows < data.length) {
      data = Arrays.copyOf(data, rows)
    }
  }

  def ensureCapacity(minCapacity: Int) {
    if (minCapacity - data.length > 0) grow(minCapacity)
  }

  override def toString: String = "Nominal[" + rowCount + "]"

  def missing = new Missing {
    def apply(row: Int): Boolean = Nominal.MissingIndex == indexes(row)

    def update(row: Int, value: Boolean): Unit = indexes(row) = Nominal.MissingIndex

    def ++(): Unit = indexes ++ Nominal.MissingIndex
  }

  def values = new Values {
    def apply(row: Int): Double = data(row)

    def update(row: Int, value: Double) = data(row) = math.rint(value).toInt

    override def ++(value: Double): Unit = {
      val row = math.rint(value).toInt
      val label = _dict(data(row))
      ensureCapacity(rows + 1)
      if (!_reverse.contains(label)) {
        _dict += label
        _reverse.put(label, _reverse.size)
      }
      data(rows) = _reverse(label)
      rows += 1
    }
  }

  def indexes = new Indexes {
    override def apply(row: Int): Int = data(row)

    override def update(row: Int, value: Int): Unit = data(row) = value

    override def ++(value: Int): Unit = {
      val label = _dict(value)
      ensureCapacity(rows + 1)
      if (!_reverse.contains(label)) {
        _dict += label
        _reverse.put(label, _reverse.size)
      }
      data(rows) = _reverse(label)
      rows += 1
    }
  }

  def labels = new Labels {
    override def apply(row: Int): String = _dict(data(row))

    override def update(row: Int, value: String): Unit = {
      if (value == Nominal.MissingValue) {
        data(row) = Nominal.MissingIndex
      } else {
        if (!_reverse.contains(value)) {
          _dict += value
          _reverse += (value -> _reverse.size)
        }
        data(row) = _reverse(value)
      }
    }

    override def ++(value: String): Unit = {
      ensureCapacity(rows + 1)
      if (!_reverse.contains(value)) {
        _dict += value
        _reverse.put(value, _reverse.size)
      }
      data(rows) = _reverse(value)
      rows += 1
    }

    override def dictionary_=(dict: Array[String]): Unit = {
      val oldDict = _dict;
      val oldReverse = _reverse;

      _dict = new mutable.MutableList();
      _reverse = new mutable.HashMap();
      _dict += "?";
      _reverse += ("?" -> 0);

      for (i <- 0 until dict.length) {
        if (!_reverse.contains(dict(i))) {
          _dict += dict(i);
          _reverse += (dict(i) -> _reverse.size);
        }
      }

      for (i <- 0 until rows) {
        if (!_reverse.contains(oldDict(data(i)))) {
          _dict = oldDict;
          _reverse = oldReverse;
          throw new IllegalArgumentException("new getDictionary does not contains all old labels");
        }
      }

      for (i <- 0 until rows) {
        data(i) = _reverse(oldDict(data(i)));
      }
    }

    override def dictionary: Array[String] = _dict.toArray

    def indexOf(label: String): Option[Int] = Option(_reverse(label))
  }

  override def buildSummary(sb: StringBuilder): Unit = ???
}

object Nominal {
  val MissingValue: String = "?"
  val MissingIndex: Int = 0

  def apply(xs: Array[String]): Nominal = {
    val nom = new Nominal()
    xs.foreach(label => nom.labels ++ label)
    nom
  }


  def apply(xs: String*): Nominal = {
    val nom = new Nominal()
    xs.foreach(label => nom.labels ++ label)
    nom
  }
}

