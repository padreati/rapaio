package rapaio.sandbox

import rapaio.data._
import rapaio.core.stat.Mean
import rapaio.graphics.base.Figure
import rapaio.printer.FigurePanel
import javax.swing.JDialog
import java.awt.BorderLayout
import rapaio.graphics.Plot
import rapaio.graphics.plot.Points

/**
 * User: Aurelian Tutuianu <padreati@yahoo.com>
 */
object Sandbox extends App {

  val x = Index(Array(1, 2, 3))

  val y = Index(x.toIndexArray)
  val z = Value(x.toValueArray)
  z.addValue(10.17)

  println(x.toIndexArray mkString ",")
  println(y.toValueArray mkString ",")
  println(z.toValueArray mkString ",")
  println(z.toIndexArray mkString ",")

  println(new Mean(z).getValue)


  draw(new Plot().add(new Points(
    new Value {
      for (i <- 1 to 100) addValue(math.random)
    },
    new Value {
      for (i <- 1 to 100) addValue(math.random)
    }
  )), 500, 500)

  draw(new Plot().add(new Points(
    new Value {
      for (i <- 1 to 100) addValue(math.random)
    },
    new Value {
      for (i <- 1 to 100) addValue(math.random)
    }
  )), 500, 500)

  draw(new Plot().add(new Points(
    new Value {
      for (i <- 1 to 100) addValue(math.random)
    },
    new Value {
      for (i <- 1 to 100) addValue(math.random)
    }
  )), 500, 500)


  def draw(figure: Figure, width: Int, height: Int) {
    val figurePanel: FigurePanel = new FigurePanel(figure)
    val frame: JDialog = new JDialog
    frame.setContentPane(figurePanel)
    frame.setVisible(true)
    frame.setDefaultCloseOperation(2)
    frame.setLayout(new BorderLayout)
    frame.setAutoRequestFocus(true)
    frame.setSize(width, height)
    while (true) {
      try {
        Thread.sleep(10)
      }
      catch {
        case ex: InterruptedException => {
        }
      }
      if (!frame.isVisible) {
        return
      }
    }
  }
}
