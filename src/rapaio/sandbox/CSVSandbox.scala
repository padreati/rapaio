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
import rapaio.ml.tools.DensityVector

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

  DensityVector(df.col("Sex")).summary()
  DensityVector(df.col("Embarked")).summary()
}
