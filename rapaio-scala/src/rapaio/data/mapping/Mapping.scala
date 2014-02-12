package rapaio.data.mapping

import scala.collection.mutable

/**
 * User: Aurelian Tutuianu <padreati@yahoo.com>
 */
class Mapping {

  private var mapping = new mutable.MutableList[Int]

  def size: Int = mapping.size

  def apply(pos: Int): Int = {
    require(mapping.size > pos, "Value at pos " + pos + " does not exists")
    mapping(pos)
  }

  def add(pos: Int) {
    mapping += pos
  }
}

object Mapping {
  def apply(list: List[Int]): Mapping = {
    val m = new Mapping
    m.mapping ++= list
    m
  }
}