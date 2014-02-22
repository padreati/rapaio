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

package rapaio.printer

import rapaio.graphics.base.Figure
import javax.swing.JDialog
import java.awt.BorderLayout

/**
 * User: Aurelian Tutuianu <padreati@yahoo.com>
 */
class LocalPrinter extends Printer {
  def textWidth: Int = {
    return 0
  }

  def textWidth_$eq(width: Int) {
  }

  def graphicWidth: Int = {
    return 0
  }

  def graphicWidth_$eq(width: Int) {
  }

  def graphicHeight: Int = {
    return 0
  }

  def graphicHeight_$eq(height: Int) {
  }

  def print(message: String) {
  }

  def println {
  }

  def error(message: String, throwable: Throwable) {
  }

  def preparePrinter {
  }

  def closePrinter {
  }

  def heading(h: Int, lines: String) {
  }

  def code(lines: String) {
  }

  def p(lines: String) {
  }

  def eqn(equation: String) {
  }

  def draw(figure: Figure, width: Int, height: Int) {
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

  def draw(figure: Figure) {
    draw(figure, 500, 500)
  }
}