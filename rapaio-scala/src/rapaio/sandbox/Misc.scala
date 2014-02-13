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

  base.color = Color.GREEN
  println(base.color(10))

  println(child.color(9))
  child.color = 9
  println(child.color(9))

  println(child.color.colorArray mkString ",")

  val nice = new GraphicOptions(child)

  nice.color = Index(1, 2, 3, 4, 6)

  println(nice.color.colorArray mkString "")
}
