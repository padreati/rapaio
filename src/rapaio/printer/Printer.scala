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

/**
 * User: Aurelian Tutuianu <padreati@yahoo.com>
 */
trait Printer {

  def textWidth(): Int

  def textWidth_=(width: Int)

  def graphicWidth: Int

  def graphicWidth_=(width: Int)

  def graphicHeight: Int

  def graphicHeight_=(height: Int)

  def print(message: String)

  def println()

  def error(message: String, throwable: Throwable)

  def preparePrinter()

  def closePrinter()

  def heading(h: Int, lines: String)

  def code(lines: String)

  def p(lines: String)

  def eqn(equation: String)

  def draw(figure: Figure, width: Int, height: Int)

  def draw(figure: Figure)
}
