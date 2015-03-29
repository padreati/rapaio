/*
 * Apache License
 * Version 2.0, January 2004
 * http://www.apache.org/licenses/
 *
 *    Copyright 2013 Aurelian Tutuianu
 *    Copyright 2014 Aurelian Tutuianu
 *    Copyright 2015 Aurelian Tutuianu
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

package rapaio.experiment.scala

import rapaio.WS
import rapaio.datasets.Datasets
import rapaio.graphics.Plot
import rapaio.graphics.plot.Histogram
import rapaio.printer.IdeaPrinter

object SandBox extends App {

  val df = Datasets.loadCarMpgDataset()
  df.summary()

  val mpg = df.getVar("mpg")
  WS.setPrinter(new IdeaPrinter())
  WS.draw(new Plot().add(new Histogram(mpg).bins(30)))

  import scala.collection.JavaConversions._

  println(df.spotList().map(s => s.value("mpg")).distinct.count((v) => true))
  println(df.spotList().map(s => s.value("mpg")).count((v) => true))
}
