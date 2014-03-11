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

package rapaio.sandbox

import rapaio.printer.TextTable


/**
 * User: Aurelian Tutuianu <padreati@yahoo.com>
 */
object Sandbox extends App {

  val table = new TextTable(3, 3)
  table.headers(0) = "column1"
  table.headers(1) = ""
  table.headers(2) = "column3"

  table.alignBody(0) = false
  table.data(0)(0) = "data 1"
  table.data(1)(0) = "data 23"
  table.data(2)(0) = "?"

  table.alignBody(1) = false
  table.data(0)(1) = ":"
  table.data(1)(1) = ":"
  table.data(2)(1) = ":"

  table.alignBody(2) = true
  table.data(0)(2) = "1.00"
  table.data(1)(2) = "23.00"
  table.data(2)(2) = "23242340.01"

  val sb = new StringBuilder
  table.print(sb, 100)
  println(sb.toString())

}

