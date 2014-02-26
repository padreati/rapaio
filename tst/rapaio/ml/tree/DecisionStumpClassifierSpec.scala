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

package rapaio.ml.tree

import org.scalatest.{Matchers, FlatSpec}
import rapaio.data.{Value, Frame, Nominal}

/**
 * @author <a href="mailto:padreati@yahoo.com>Aurelian Tutuianu</a>
 */
class DecisionStumpClassifierSpec extends FlatSpec with Matchers {

  "stump classifier " should " throw an IllegalArgumentException if no inputs specified " in {
    val y = Nominal("a", "b", "?", "c", "g", "a", "a")
    val df = Frame.solid(y.rowCount, ("y", y))

    val c = new DecisionStumpClassifier()
    a[IllegalArgumentException] should be thrownBy {
      c.learn(df, "y")
    }
  }

  it should " work with at least one nominal input variable " in {

    val x = Nominal("a", "b", "?", "c", "g", "a", "a")
    val y = Nominal("a", "b", "?", "c", "g", "a", "a")
    val df = Frame.solid(y.rowCount, ("x", x), ("y", y))

    val test = Frame.solid(1, ("x", Nominal("a")))

    val c = new DecisionStumpClassifier()
    c.learn(df, "y")
    c.predict(test)
    c.prediction.labels.forall(label => label == "a") should be(right = true)

    c.summary()
  }

  it should " work with at least one numeric input variable " in {

    val x = Value(1, 2, 2, 5, 10, 1, 1)
    val y = Nominal("a", "b", "?", "c", "g", "a", "a")
    val df = Frame.solid(y.rowCount, ("x", x), ("y", y))

    val test = Frame.solid(1, ("x", Nominal("a")))

    val c = new DecisionStumpClassifier()
    c.learn(df, "y")
    c.predict(test)
    c.prediction.labels.forall(label => label == "a") should be(right = true)


    c.summary()
  }
}
