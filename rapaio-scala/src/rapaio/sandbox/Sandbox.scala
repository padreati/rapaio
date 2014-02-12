package rapaio.sandbox

import rapaio.data._

/**
 * User: Aurelian Tutuianu <padreati@yahoo.com>
 */
object Sandbox extends App {

  val x = Index(Array(1, 2, 3))

  val y = Index(x.toIndexArray)
  val z = Value(x.toValueArray)

  println(x.toIndexArray mkString ",")
  println(y.toValueArray mkString ",")
  println(z.toValueArray mkString ",")
  println(z.toIndexArray mkString ",")
}
