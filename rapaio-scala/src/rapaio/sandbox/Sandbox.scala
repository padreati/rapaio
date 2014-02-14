package rapaio.sandbox

import rapaio.data._
import rapaio.graphics.Plot
import rapaio.workspace.Workspace._
import java.awt.Color
import scala.util.Random

/**
 * User: Aurelian Tutuianu <padreati@yahoo.com>
 */
object Sandbox extends App {

  val N = 1000

  val x1 = Value(0, N, (i) => math.sin(i / math.E))
  val x2 = Value(0, N, (i) => math.sin(i * 2))
  val y = Value(0, N, (i) => i / 100.)

  val f = () => (Value(0, 100, (i) => math.random))
  val rand = new Random()

  draw(
    new Plot()
      //      .points(x = x1, y = y, col = Index(3, 4, 2, 1, 9, 15), pch = 'p')
      //      .points(x = x2, y = y, col = Color.PINK, pch = '+')
      .points(x = f(), y = f(), col = rand.nextInt(20), pch = 'o')
      .points(x = f(), y = f(), col = rand.nextInt(20), pch = 'p')
      .points(x = f(), y = f(), col = rand.nextInt(20), pch = 's')
      .points(x = f(), y = f(), col = rand.nextInt(20), pch = '+')
      .points(x = f(), y = f(), col = rand.nextInt(20), pch = 'x')
    , 500, 500)
}
