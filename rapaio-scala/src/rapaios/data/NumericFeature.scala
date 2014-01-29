package rapaios.data

import scala.Double
import scala.reflect.ClassTag

/**
 * User: Aurelian Tutuianu <padreati@yahoo.com>
 */
abstract class NumericFeature[T: ClassTag] extends TraversableOnce[T] {

  protected var data: Array[T] = new Array[T](0);
  protected var rows: Int = 0;
  var missingValue: T;

  private def ensureCapacityInternal(minCapacity: Int) {
    if (minCapacity - data.length > 0) {
      val oldCapacity = data.length
      var newCapacity = oldCapacity + (oldCapacity >> 1)
      if (newCapacity - minCapacity < 0) newCapacity = minCapacity
      val newData = new Array[T](newCapacity)
      Array.copy(data, 0, newData, 0, data.length)
      data = newData
    }
  }

  def rowCount = rows

  private def rangeCheck(index: Int) {
    assert((index < rows && index >= 0), "Index: " + index + ", Size: " + rows)
  }

  def apply(row: Int): T = {
    rangeCheck(row)
    data(row)
  }

  def update(row: Int, value: T*) {
    rangeCheck(row)
    rangeCheck(row + value.length - 1)
    for (i <- row until (row + value.length))
      data(i) = value(i)
  }

  def addValue(value: T*) {
    ensureCapacityInternal(rows + value.length)
    for (x <- value) {
      data(rows) = x
      rows += 1
    }
  }

  def addValue(row: Int, value: T) {
    rangeCheck(row)
    ensureCapacityInternal(rows + 1)
    System.arraycopy(data, row, data, row + 1, rows - row)
    data(row) = value
    rows += 1
  }

  def isMissing(row: Int): Boolean

  def setMissing(row: Int)

  def addMissing

  def remove(index: Int) {
    rangeCheck(index)
    val numMoved: Int = rows - index - 1
    if (numMoved > 0) System.arraycopy(data, index + 1, data, index, numMoved)
  }

  def removeRange(fromIndex: Int, toIndex: Int) {
    val numMoved: Int = rows - toIndex
    Array.copy(data, toIndex, data, fromIndex, numMoved)
    rows -= (toIndex - fromIndex)
  }

  def clear {
    rows = 0
  }

  def ensureCapacity(minCapacity: Int) {
    if (minCapacity > 0) {
      if (minCapacity - data.length > 0) ensureCapacityInternal(minCapacity)
    }
  }

  override def toString: String = "Numeric[" + rows + "]"

  def isTraversableAgain: Boolean = true

  def toStream: Stream[T] = data.toStream

  def foreach[U](f: (T) => U): Unit = data.foreach[U](f)

  def isEmpty: Boolean = rows == 0

  def hasDefiniteSize: Boolean = true

  def seq: TraversableOnce[T] = data.toSeq

  def forall(p: (T) => Boolean): Boolean = data.forall(p)

  def exists(p: (T) => Boolean): Boolean = data.exists(p)

  def find(p: (T) => Boolean): Option[T] = data.find(p)

  def copyToArray[B >: T](xs: Array[B], start: Int, len: Int): Unit = data.copyToArray(xs, start, len)

  def toTraversable: Traversable[T] = data.toTraversable

  def toIterator: Iterator[T] = data.toIterator
}

final class DoubleFeature extends NumericFeature[Double] {
  override var missingValue: Double = Double.NaN;

  def isMissing(row: Int): Boolean = apply(row) != apply(row)

  def setMissing(row: Int): Unit = update(row, missingValue)

  def addMissing: Unit = addValue(missingValue)

  def ^(pow: Double): DoubleFeature = {

    var resData = for {
      x <- data
    } yield math.pow(x, pow)
    DoubleFeature(resData)
  }
}


object DoubleFeature {

  def apply(xs: Array[Double]): DoubleFeature = {
    val ret = new DoubleFeature
    ret.data = xs.clone()
    ret.rows = ret.data.length
    ret
  }
}
