package rapaio.workspace

import rapaio.graphics.base.Figure
import rapaio.printer.FigurePanel
import javax.swing.JDialog
import java.awt.BorderLayout

/**
 * @author <a href="email:padreati@yahoo.com>Aurelian Tutuianu</a>
 */
object Workspace {

  def p(xs: String*) {
    xs.foreach(x => print(x + " "))
    println
  }

  def draw(figure: Figure, width: Int = 500, height: Int = 500) {
    val figurePanel = new FigurePanel(figure)
    val frame = new JDialog
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
