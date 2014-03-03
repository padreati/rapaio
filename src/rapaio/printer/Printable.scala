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

import rapaio.workspace.Workspace

/**
 * Trait which is used to uniformly add auto-describing features to various
 * objects.
 *
 * Offers methods which works directly with printer
 * selected in [[rapaio.workspace.Workspace]].
 *
 * Various options for formatting the output are defined and used
 * from [[rapaio.]]
 * @author <a href="mailto:padreati@yahoo.com>Aurelian Tutuianu</a>
 */
trait Printable {

  final def summary(): Unit = {
    val sb = new StringBuilder
    buildSummary(sb)
    Workspace.printer().code(sb.toString())
  }

  def buildSummary(sb: StringBuilder): Unit

  /**
   * Show content of an object. By default is implemented using toString.
   */
  final def show(): Unit = {
    val sb = new StringBuilder
    buildShow(sb)
    Workspace.printer().code(sb.toString())
  }

  /**
   * Builds show content for an object.
   */
  def buildShow(sb: StringBuilder): Unit = {
    sb.append(this.toString)
  }
}
