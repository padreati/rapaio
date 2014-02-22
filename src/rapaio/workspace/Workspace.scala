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
import rapaio.printer.{IdeaPrinter, Printer}

/**
 * @author <a href="email:padreati@yahoo.com>Aurelian Tutuianu</a>
 */
object Workspace {
  private var _printer: Printer = new IdeaPrinter

  def printer(): Printer = _printer

  def printer_=(printer: Printer) = _printer = printer

  def p(xs: String*) {
    xs.foreach(x => print(x + " "))
    println
  }

  def preparePrinter() = _printer.preparePrinter

  def closePrinter() = _printer.closePrinter

  def print(message: String) = _printer.print(message)

  def println() = _printer.println

  def println(message: String) = {
    _printer.print(message)
    _printer.println
  }

  def printf(message: String, args: AnyRef*) = _printer.print(String.format(message, args))

  def printfln(message: String, args: AnyRef*) {
    _printer.print(String.format(message, args))
    _printer.println
  }

  def heading(h: Int, lines: String) {
    _printer.heading(h, lines)
  }

  def error(message: String, ex: Throwable) {
    _printer.error(message, ex)
  }

  def code(lines: String) {
    _printer.code(lines)
  }

  def p(lines: String) {
    _printer.p(lines)
  }

  def eqn(equation: String) {
    _printer.eqn(equation)
  }

  def draw(figure: Figure, width: Int, height: Int) {
    _printer.draw(figure, width, height)
  }

  def draw(figure: Figure) {
    printer.draw(figure)
  }
}
