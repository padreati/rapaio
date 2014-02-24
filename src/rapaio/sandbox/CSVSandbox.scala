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

import rapaio.io.CSV
import java.io.File
import rapaio.data.{Value, Nominal}
import rapaio.workspace.Workspace
import rapaio.graphics.Plot
import rapaio.core.stat.DensityVector

/**
 * @author <a href="email:padreati@yahoo.com>Aurelian Tutuianu</a>
 */
object CSVSandbox extends App {

  val df = CSV.read(
    file = new File("/home/ati/rapaio/rapaio-java/src/rapaio/datasets/titanic-train.csv"),
    header = true,
    typeHints = Map[String, String](("PassengerId", "idx"), ("Survived", "nom"))
  )

  df.colNames.foreach(colName => {
    print(colName + "[" + df.col(colName).shortName + "]: ")
    df.col(colName).shortName match {
      case "nom" => print(df.col(colName).toLabelArray.slice(0, 20) mkString ",")
      case "val" => print(df.col(colName).toValueArray.slice(0, 20) mkString ",")
      case "idx" => print(df.col(colName).toIndexArray.slice(0, 20) mkString ",")
    }
    println
  })

  def printMode(name: String): Unit = {
    println("mode: " + DensityVector(df.col(name)).mode())
  }

  //  DensityVector(df.col("Sex")).summary()
  //  printMode("Sex")
  //  DensityVector(df.col("Embarked")).summary()
  //  printMode("Embarked")
  //  DensityVector(df.col("Survived")).summary()
  //  printMode("Survived")
  //  DensityVector(df.col("Cabin")).summary()
  //  printMode("Cabin")

  val nominal = new Nominal()
  for (i <- 0 until 100) nominal.labels ++ (i % 10).toString
  val hit = Value(0, 11, _ => 0)
  for (i <- 0 until 10000) {
    nominal.labels.indexOf(DensityVector(nominal).mode()).foreach(x => hit.values(x) += 1)
  }
  Workspace.draw(Plot().hist(hit))
}
