/*
 * Apache License
 * Version 2.0, January 2004
 * http://www.apache.org/licenses/
 *
 *    Copyright 2013 Aurelian Tutuianu
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package rapaio.workspace

import rapaio.graphics.base.Figure
import rapaio.printer.FigurePanel
import javax.swing.JDialog
import java.awt.BorderLayout
import rapaio.server.LocalPrinter

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

  def drawPlugin(figure: Figure) {
    LocalPrinter.draw(figure)
  }
}
