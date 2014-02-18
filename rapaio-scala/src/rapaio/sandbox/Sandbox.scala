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
import rapaio.graphics._
import rapaio.workspace.Workspace._
import scala.util.Random
import rapaio.data.mapping.MappedVector
import rapaio.core._

/**
 * User: Aurelian Tutuianu <padreati@yahoo.com>
 */
object Sandbox extends App {

  val N = 1000
  val F = 2 * math.Pi / N.toDouble

  val x1 = Value(0, N, (i) => math.sin(i / math.E))
  val x2 = Value(0, N, (i) => math.sin(i * 2))
  val y = Value(0, N, (i) => i / 100.0)

  mean(x1).summary

  val f = () => Value(0, N, (i) => math.random)
  val rand = new Random()

  val m = Value(0, N, (i) => 10 * math.pow(math.sin(i / F), 3))
  val n = Value(0, N, (i) =>
    10 * math.cos(i / F)
      - 3 * math.cos(2 * i / F)
      - 2 * math.cos(3 * i / F)
      - math.cos(4 * i / F))

  m.values ++ 13
  n.values ++ 0

  println(m.instances.filter((inst: VInst) => inst.value < 5) mkString ",")

  val nom = new Nominal()
  nom.labels ++ "Ana"
  nom.labels ++ "are"
  nom.labels ++ "mere"
  nom.labels ++ "?"
  nom.labels ++ "something"

  val b = MappedVector(nom.instances)
  println(b.instances mkString ",")

  drawPlugin(
    Plot(lwd = 2)
      //      .points(x = f(), y = f(), col = rand.nextInt(20), pch = 'o')
      //      .points(x = f(), y = f(), col = rand.nextInt(20), pch = 'p')
      //      .points(x = f(), y = f(), col = rand.nextInt(20), pch = 's')
      //      .points(x = f(), y = f(), col = rand.nextInt(20), pch = '+')
      //      .points(x = f(), y = f(), col = rand.nextInt(20), pch = 'x')
      .hist(x = x1, prob = true, bins = 10)
  )

}

