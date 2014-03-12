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

import rapaio.printer.Printer
import rapaio.printer.idea.IdeaPrinter

/**
 * @author <a href="email:padreati@yahoo.com>Aurelian Tutuianu</a>
 */
object Workspace {
  var printer: Printer = new IdeaPrinter

  def print(message: String) = printer.print(message)

  def heading(h: Int, lines: String) = printer.heading(h, lines)

  def error(message: String, ex: Throwable) = printer.error(message, ex)

  def code(lines: String) = printer.code(lines)

  def p(lines: String) = printer.p(lines)
}
