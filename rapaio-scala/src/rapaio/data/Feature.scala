package rapaio.data

/**
 * User: Aurelian Tutuianu <padreati@yahoo.com>
 */
abstract class Feature {

  def rowCount: Int

  protected def addString(b: StringBuilder, start: String, sep: String, end: String): StringBuilder

  def mkString(start: String, sep: String, end: String): String =
    addString(new StringBuilder(), start, sep, end).toString

  def mkString(sep: String): String = mkString("", sep, "")

  def mkString: String = mkString("")
}

abstract class NumericFeature extends Feature {

  def forEach[U](f: Double => U)
}

object NumericFeature {
}

final class DoubleFeature extends NumericFeature {

  val missingValue = Double.NaN

  private var data: Array[Double] = new Array[Double](0)
  private var rows: Int = 0

  protected def addString(b: StringBuilder, start: String, sep: String, end: String): StringBuilder = {
    var first = true
    b append start
    for (x <- data) {
      if (first) {
        b append x
        first = false
      }
      else {
        b append sep
        b append x
      }
    }
    b append end
    b
  }

  private def ensureCapacityInternal(minCapacity: Int) {
    if (minCapacity - data.length > 0) {
      val oldCapacity = data.length
      var newCapacity = oldCapacity + (oldCapacity >> 1)
      if (newCapacity - minCapacity < 0) newCapacity = minCapacity
      val newData = new Array[Double](newCapacity)
      Array.copy(data, 0, newData, 0, data.length)
      data = newData
    }
  }

  override def rowCount = rows

  private def rangeCheck(index: Int) = assert(index < rows && index >= 0, "Index: " + index + ", Size: " + rows)

  def apply(row: Int): Double = {
    rangeCheck(row)
    data(row)
  }

  def update(row: Int, value: Double*) {
    rangeCheck(row)
    rangeCheck(row + value.length - 1)
    for (i <- row until (row + value.length))
      data(i) = value(i)
  }

  def forEach[U](f: Double => U) {
    var i = 0
    val len = rowCount
    while (i < len) {
      val v = this(i)
      if (!v.isNaN) f(v)
      i += 1
    }
  }

  def addValue(value: Double*) {
    ensureCapacityInternal(rows + value.length)
    for (x <- value) {
      data(rows) = x
      rows += 1
    }
  }

  def addValue(row: Int, value: Double) {
    rangeCheck(row)
    ensureCapacityInternal(rows + 1)
    System.arraycopy(data, row, data, row + 1, rows - row)
    data(row) = value
    rows += 1
  }

  def isMissing(row: Int): Boolean = apply(row).isNaN

  def setMissing(row: Int) = update(row, missingValue)

  def addMissing() = addValue(missingValue)

  def remove(index: Int) {
    rangeCheck(index)
    val numMoved: Int = rows - index - 1
    if (numMoved > 0) Array.copy(data, index + 1, data, index, numMoved)
  }

  def removeRange(fromIndex: Int, toIndex: Int) {
    val numMoved: Int = rows - toIndex
    Array.copy(data, toIndex, data, fromIndex, numMoved)
    rows -= (toIndex - fromIndex)
  }

  def clear() {
    rows = 0
  }

  def ensureCapacity(minCapacity: Int) {
    if (minCapacity > 0) {
      if (minCapacity - data.length > 0) ensureCapacityInternal(minCapacity)
    }
  }
}

object DoubleFeature {

  /*
  Builds a double feature from a list of double values
   */
  def apply(xs: Double*): DoubleFeature = {
    val x = new DoubleFeature
    x.ensureCapacity(xs.length)
    x.rows = 0
    for (v <- xs) {
      x.data(x.rows) = v
      x.rows += 1
    }
    x
  }

  /**
   * Builds a double feature from an Array[Double]
   * @param xs
   * @return
   */
  def apply(xs: Array[Double]): DoubleFeature = {
    val x = new DoubleFeature
    x.ensureCapacity(xs.length)
    Array.copy(xs, 0, x, 0, xs.length)
    x.rows = xs.length
    x
  }
}