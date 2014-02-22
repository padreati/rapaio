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

package rapaio.printer.idea

import rapaio.graphics.base.{ImageUtility, Figure}
import java.net.Socket
import rapaio.printer.Printer

/**
 * @author <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a>
 */
object IdeaPrinter {
  private val DefaultPort: Int = 56339
}

class IdeaPrinter extends Printer {
  private var _textWidth: Int = 80
  private var _graphicWidth: Int = 800
  private var _graphicHeight: Int = 600

  def preparePrinter() {}

  def closePrinter() {}

  def textWidth(): Int = _textWidth

  def textWidth_=(width: Int) = _textWidth = width

  def graphicHeight: Int = _graphicHeight

  def graphicHeight_=(height: Int) = _graphicHeight = height

  def graphicWidth: Int = _graphicWidth

  def graphicWidth_=(width: Int) = _graphicWidth = width

  def p(lines: String) = Console.println(lines)

  def code(lines: String) = p(lines)

  def eqn(equation: String) = p(equation)

  def error(message: String, throwable: Throwable) = Console.println(message + "\n.Error message: " + throwable.getMessage)

  def println() = Console.println()

  def heading(h: Int, lines: String) = {
    Console.print("=" * h)
    Console.print(lines)
    Console.print("=" * h)
    Console.println()
  }

  def print(message: String) = Console.print(message)

  def draw(figure: Figure) {
    draw(figure, graphicWidth, graphicHeight)
  }

  def draw(figure: Figure, width: Int, height: Int) {
    try {
      val s = new Socket("localhost", IdeaPrinter.DefaultPort)
      new ClassMarshaller().marshallConfig(s.getOutputStream)
      val cb: CommandBytes = new ClassMarshaller().unmarshall(s.getInputStream)
      val image = ImageUtility.buildImage(figure, cb.getGraphicalWidth, cb.getGraphicalHeight);
      new ClassMarshaller().marshallDraw(s.getOutputStream, image, cb.getGraphicalWidth, cb.getGraphicalHeight)
    }
    catch {
      case ex: Any => {
        ex.printStackTrace()
      }
    }
  }

}