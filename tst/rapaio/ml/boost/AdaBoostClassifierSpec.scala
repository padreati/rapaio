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

package rapaio.ml.boost

import rapaio.core.RandomSource
import rapaio.core.stat.{ConfusionMatrix, Sum}
import rapaio.data.{Nominal, Frame, Value}
import rapaio.ml.tree.DecisionStumpClassifier
import org.scalatest.{Matchers, FlatSpec}
import rapaio.ml.boosting.AdaBoostSAMMEClassifier

/**
 * @author <a href="mailto:padreati@yahoo.com>Aurelian Tutuianu</a>
 */
class AdaBoostClassifierSpec extends FlatSpec with Matchers {

  "AdaBoost " should " give an error less than or equal with 1/k for small weights " in {

    val from = Array[String]("a", "b", "c", "d")
    val N = 100

    for (i <- 0 until 2000) {
      val k = i % 2 + 2
      val x = Array.fill(N)(from(RandomSource.nextInt(from.length)))
      val y = Array.fill(N)(from(RandomSource.nextInt(k)))
      val w = Array.fill(N)(RandomSource.nextDouble)
      val sum = Sum(Value(w)).value
      w.transform(x => x / sum)

      val df = Frame.solid(N, ("x", Nominal(x)), ("y", Nominal(y)))
      val cc = new DecisionStumpClassifier()
      cc.minCount = 2

      val c = new AdaBoostSAMMEClassifier()
      c.weak = cc
      c.learn(df, Value(w), "y")
      c.predict(df)

      val acc = new ConfusionMatrix(Nominal(y), c.prediction).accuracy
      acc should be >= 1 / k.toDouble
    }
  }

}
