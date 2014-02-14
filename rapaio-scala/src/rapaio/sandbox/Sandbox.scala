package rapaio.sandbox

import rapaio.data._
import rapaio.core.stat.Mean
import rapaio.graphics.base.Figure
import rapaio.printer.FigurePanel
import javax.swing.JDialog
import java.awt._
import rapaio.graphics.Plot
import rapaio.graphics.plot.Points
import rapaio.workspace.Workspace._
import scala.annotation.tailrec

/**
 * User: Aurelian Tutuianu <padreati@yahoo.com>
 */
object Sandbox extends App {

  val N = 5000
  draw(new Plot() {
    add(new Points(
      x = new Value {
        for (i <- 1 to N) this.addValue(math.sin(i / math.E))
      },
      y = new Value {
        for (i <- 1 to N) this.addValue(i / 10)
      }
    ) {
      this.options.col = Color.BLACK
    })
    this.options.col = Color.BLUE
  }, 500, 500)
}
