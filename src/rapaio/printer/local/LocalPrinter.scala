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

package rapaio.printer.local

import rapaio.graphics.base.Figure
import javax.swing.JDialog
import java.awt.BorderLayout
import rapaio.printer.{FigurePanel, Printer}

/**
 * User: Aurelian Tutuianu <padreati@yahoo.com>
 */
class LocalPrinter extends Printer {

  protected var _textWidth: Int = 160
  protected var _graphicWidth: Int = 800
  protected var _graphicHeight: Int = 600

  def textWidth(): Int = _textWidth

  def textWidth_=(width: Int) = _textWidth = width

  def graphicHeight: Int = _graphicHeight

  def graphicHeight_=(height: Int) = _graphicHeight = height

  def graphicWidth: Int = _graphicWidth

  def graphicWidth_=(width: Int) = _graphicWidth = width

  def p(lines: String) = Console.println(lines)

  def code(lines: String) = p(lines)

  def error(message: String, throwable: Throwable) = Console.println(message + "\n.Error message: " + throwable.getMessage)

  def heading(h: Int, lines: String) = {
    print("=" * h)
    print(lines)
    print("=" * h)
    println()
  }

  def print(message: String) = Console.print(message)

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
}