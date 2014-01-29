package rapaios.data

/**
 * User: Aurelian Tutuianu <padreati@yahoo.com>
 */
object test extends App {

  var x = new DoubleFeature

  x.addValue(10., 20., 30, 50, 100, 1000, 10 ^ 5)
  println(x.rowCount)
  println(x mkString ",")

  println(x ^ 3 mkString ", ")
}
