package rapaio.sandbox

import rapaio.data._
import rapaio.graphics.Plot
import rapaio.workspace.Workspace._

/**
 * User: Aurelian Tutuianu <padreati@yahoo.com>
 */
object Sandbox extends App {

  val N = 50000
  val x = Value(to = N, f = (i) => math.sin(i / 4))
  val y = Value(to = N, f = (i) => i / 100.)
  draw(new Plot().points(x = x, y = y), 500, 500)
}
