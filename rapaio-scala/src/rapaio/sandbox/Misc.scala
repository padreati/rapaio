package rapaio.sandbox

import rapaio.data._
import rapaio.graphics.base._
import java.awt.Color

/**
 * User: Aurelian Tutuianu <padreati@yahoo.com>
 */
object Misc extends App {

  val base = new GraphicOptions
  val child = new GraphicOptions(base)

  base.col = Color.GREEN
  println(base.col(10))

  println(child.col(9))
  child.col = 9
  println(child.col(9))

  println(child.col.values mkString ",")

  val nice = new GraphicOptions(child)

  nice.col = Index(1, 2, 3, 4, 6)

  println(nice.col.values mkString "")
}
