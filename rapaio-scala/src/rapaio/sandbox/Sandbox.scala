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
import rapaio.graphics.Plot
import rapaio.workspace.Workspace._
import java.awt.Color
import scala.util.Random

/**
 * User: Aurelian Tutuianu <padreati@yahoo.com>
 */
object Sandbox extends App {

  val N = 1000000

  val x1 = Value(0, N, (i) => math.sin(i / math.E))
  val x2 = Value(0, N, (i) => math.sin(i * 2))
  val y = Value(0, N, (i) => i / 100.)

  val f = () => (Value(0, N, (i) => math.random))
  val rand = new Random()

  draw(
    Plot(col = Color.RED, lwd = 2)
      //      .points(x = f(), y = f(), col = rand.nextInt(20), pch = 'o')
      //      .points(x = f(), y = f(), col = rand.nextInt(20), pch = 'p')
      //      .points(x = f(), y = f(), col = rand.nextInt(20), pch = 's')
      .points(x = f(), y = f(), col = rand.nextInt(20), pch = '+')
    //      .points(x = f(), y = f(), col = rand.nextInt(20), pch = 'x')
    , 500, 500)
}
