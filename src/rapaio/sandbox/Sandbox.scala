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

import rapaio.graphics._
import rapaio.datasets.Datasets


/**
 * User: Aurelian Tutuianu <padreati@yahoo.com>
 */
object Sandbox extends App {

  //  val df = Datasets.loadIrisDataset
  //
  //  df.col(1).values.transform(x=>(RandomSource.nextDouble-0.5)/10000.0)
  //
  //  points(
  //    df.col(0),
  //    df.col(1),
  //    col=df.col("class"), pch=1)
  //  draw()

  //  val n = Normal(sd = 10)
  //  plot(xLim = (-20, 20), yLim = (0, 1))
  //  function(n.pdf)
  //  function(n.cdf, col = 12)
  //  draw()

  val df = Datasets.loadPearsonHeightDataset

  boxplot(Array(df.col("Son"), df.col("Father")), Array("Son", "Father"))
  draw()
}

