package rapaio.sandbox

import rapaio.data._
import rapaio.graphics.Plot
import rapaio.workspace.Workspace._
import java.awt.Color

/**
 * User: Aurelian Tutuianu <padreati@yahoo.com>
 */
object Sandbox extends App {

  val N = 2000

  val x1 = Value(0, N, (i) => math.sin(i / math.E))
  val x2 = Value(0, N, (i) => math.sin(i * 2))
  val y = Value(0, N, (i) => i / 100.)

  draw(
    new Plot()
      .points(x = x1, y = y, col = Index(3, 4, 2, 1, 9, 15))
      .points(x = x2, y = y, col = Color.RED)
    , 500, 500)
}
