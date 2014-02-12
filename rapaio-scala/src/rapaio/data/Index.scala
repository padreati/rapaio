package rapaio.data

import java.util.Arrays
import rapaio.data.mapping.Mapping

/**
 * User: Aurelian Tutuianu <padreati@yahoo.com>
 */
class Index(private var rows: Int, private val capacity: Int, private val fill: Int)
  extends AbstractVector {


  require(capacity >= 0, "Illegal capacity: " + capacity)
  require(rows >= 0, "Illegal row count: " + rows)
  require(rows <= capacity, "Illegal row count" + rows + " less than capacity:" + capacity)

  var data: Array[Int] = new Array[Int](capacity)
  this.rows = rows
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

  private def outOfBoundsMsg(index: Int): String = {
    return "Index: " + index + ", Size: " + rows
  }

  def isMappedVector: Boolean = {
    return false
  }

  def getSourceVector: Vector = {
    return this
  }

  def getMapping: Mapping = {
    return null
  }

  def getRowCount: Int = {
    return rows
  }

  def getRowId(row: Int): Int = {
    return row
  }

  def getIndex(row: Int): Int = {
    return data(row)
  }

  def setIndex(row: Int, value: Int) {
    data(row) = value
  }

  def addIndex(value: Int) {
    ensureCapacityInternal(rows + 1)
    data(rows) = value
    rows += 1
  }

  def addIndex(index: Int, value: Int) {
    rangeCheck(index)
    ensureCapacityInternal(rows + 1)
    System.arraycopy(data, index, data, index + 1, rows - index)
    data(index) = value
    rows += 1
  }

  def getValue(row: Int): Double = {
    return getIndex(row)
  }

  def setValue(row: Int, value: Double) {
    setIndex(row, math.rint(value).asInstanceOf[Int])
  }

  def addValue(value: Double) {
    addIndex(math.rint(value).asInstanceOf[Int])
  }

  def addValue(row: Int, value: Double) {
    addIndex(row, Math.rint(value).asInstanceOf[Int])
  }

  def getLabel(row: Int): String = {
    return ""
  }

  def setLabel(row: Int, value: String) {
    throw new RuntimeException("Operation not available for getIndex vectors.")
  }

  def addLabel(value: String) {
    throw new RuntimeException("Operation not available for getIndex vectors.")
  }

  def addLabel(row: Int, value: String) {
    throw new RuntimeException("Operation not available for getIndex vectors.")
  }

  def getDictionary: Array[String] = {
    throw new RuntimeException("Operation not available for getIndex vectors.")
  }

  def setDictionary(dict: Array[String]) {
    throw new RuntimeException("Operation not available for getIndex vectors.")
  }

  def isMissing(row: Int): Boolean = {
    return getIndex(row) == Index.MISSING_VALUE
  }

  def setMissing(row: Int) {
    setIndex(row, Index.MISSING_VALUE)
  }

  def addMissing {
    addIndex(Index.MISSING_VALUE)
  }

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
    val minExpand: Int = if ((data ne Index.EMPTY_DATA)) 0 else Index.DEFAULT_CAPACITY
    if (minCapacity > minExpand && minCapacity - data.length > 0) grow(minCapacity)
  }

  def trimToSize {
    if (rows < data.length) {
      data = Arrays.copyOf(data, rows)
    }
  }

  override def toString: String = {
    return "Index[" + getRowCount + "]"
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
}

