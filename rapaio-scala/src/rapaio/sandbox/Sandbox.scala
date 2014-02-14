package rapaio.sandbox

import rapaio.data._
import rapaio.graphics.Plot
import rapaio.workspace.Workspace._

/**
 * User: Aurelian Tutuianu <padreati@yahoo.com>
 */
object Sandbox extends App {

  val N = 2000

  val x = Value(0, N, (i) => math.sin(i / math.E))
  val y = Value(0, N, (i) => i / 100.)

  draw(new Plot().points(x = x, y = y), 500, 500)
}
