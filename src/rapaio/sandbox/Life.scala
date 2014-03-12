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

import rapaio.io.CsvPersistence
import rapaio.datasets.Datasets
import rapaio.workspace.Workspace
import rapaio.printer.idea.IdeaPrinter
import rapaio.data.Value
import rapaio.graphics._
import java.awt.Color

/**
 * @author <a href="mailto:padreati@yahoo.com>Aurelian Tutuianu</a>
 */
object Life extends App {

  Workspace.printer = new IdeaPrinter

  val df = new CsvPersistence(
    header = true,
    separator = '\t',
    defaultTypeHint = "val",
    typeHints = Map("country" -> "nom"),
    naValues = Set("-")
  ).read(Datasets.getClass, "life.csv")

  df.summary()


  df.filter((df, row) => {
    df.values(row, "women") - df.values(row, "men") >= 8
  }).foreach((df, row) => println(f"${df.labels(row, 1)} --> ${df.values(row, 5) - df.values(row, 3)}%.1f"))

  val delta = new Value(df.rowCount)
  for (i <- 0 until df.rowCount) {
    delta.values(i) = df.values(i, "women") - df.values(i, "men")
  }

  histogram(delta, main = "test", xLab = "delta", prob = false)
  draw()

  points(df.col("men"), df.col("women"), col = Color.PINK, pch = 1, xLab = "men", yLab = "women")
  abLine(1, 0)
  draw()
}
