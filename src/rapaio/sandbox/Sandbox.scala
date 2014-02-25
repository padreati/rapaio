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

import rapaio.data._

/**
 * User: Aurelian Tutuianu <padreati@yahoo.com>
 */
object Sandbox extends App {


  val x = Value(1, 10000, x => 1 / x.toDouble + math.sin(x))

  //  val y = Value(1, 10000, x => 1 / x.toDouble)

  //  println(y.toValueArray mkString ",")

  //  draw(Plot(xLim = (0, 1), yLim = (Double.NaN, 0.01)).points(x, y))

  //  Workspace.draw(Plot().hist(x.values.filter(x => x<1)))


}

