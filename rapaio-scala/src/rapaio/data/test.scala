package rapaio.data

import rapaio.core.stat.CoreStat

/**
 * User: Aurelian Tutuianu <padreati@yahoo.com>
 */
object test extends App {

  var x = DoubleFeature(1.0, 2.0, 3.0, 0.0)
  println(CoreStat.mean(x))
  println(x.rowCount)

  x.addValue(10.0, 20.0, 30, 50, 100, 1000, 10 ^ 5)

  println(CoreStat.mean(x))
  println(x.rowCount)

  val add = Array[Double](1, 2, 3)


  var xx = DoubleFeature(1, Double.NaN, 2)
  println(CoreStat.mean(xx))
  //  println(Array(1, 2) mkString())
}
