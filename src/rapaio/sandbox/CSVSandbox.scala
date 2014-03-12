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

import rapaio.data.{Value, Index, Frame}
import rapaio.core.stat.ConfusionMatrix
import rapaio.ml.boosting.AdaBoostSAMMEClassifier
import rapaio.ml.tree.DecisionStumpClassifier
import rapaio.graphics._
import rapaio.io.CsvPersistence

/**
 * @author <a href="email:padreati@yahoo.com>Aurelian Tutuianu</a>
 */
object CSVSandbox extends App {

  var df = new CsvPersistence(
    header = true,
    typeHints = Map(("PassengerId", "idx"), ("Survived", "nom"), ("Pclass", "nom"),
      ("SibSp", "nom"), ("Parch", "nom")),
    naValues = Set("?", "", " ")).
    read("/home/ati/rapaio/rapaio-java/src/rapaio/datasets/titanic-train.csv")

  df = Frame.solid(df.rowCount,
    ("Sex", df.col("Sex")),
    ("Embarked", df.col("Embarked")),
    ("Pclass", df.col("Pclass")),
    ("Fare", df.col("Fare")),
    ("SibSp", df.col("SibSp")),
    ("Age", df.col("Age")),
    ("Parch", df.col("Parch")),
    ("Survived", df.col("Survived"))
  )

  df.summary()

  val c = new AdaBoostSAMMEClassifier()
  c.weak = new DecisionStumpClassifier() {
    minCount = 10
  }
  val index = new Index()
  val accuracy = new Value()

  for (runs <- 1 to 1000) {
    c.times = runs
    c.learnFurther(df, "Survived", c)
    c.predict(df)
    val acc = new ConfusionMatrix(df.col("Survived"), c.prediction).accuracy

    index.values ++ c.times
    accuracy.values ++ acc

    points(x = index, y = accuracy, pch = 1)
    draw()
    Console.print(".")
  }
}
