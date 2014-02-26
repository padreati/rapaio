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

package rapaio.ml.base

import org.scalatest.{Matchers, FlatSpec}
import rapaio.data.{Value, Frame, Nominal}

/**
 * @author <a href="mailto:padreati@yahoo.com>Aurelian Tutuianu</a>
 */
class ModeClassifierSpec extends FlatSpec with Matchers {

  "mode classifier " should " work even only with target " in {
    val y = Nominal("a", "b", "?", "c", "g", "a", "a")
    val df = Frame.solid(y.rowCount, ("y", y))

    val c = new ModeClassifier()
    c.learn(df, "y")
    c.predict(df)

    c.prediction.labels.forall(label => label == "a") should be(right = true)
  }

  "mode classifier " should " work with target and numeric input " in {
    val x = Value(1, 3, 5, 2, 6, 7, 4)
    val y = Nominal("a", "b", "?", "c", "g", "a", "a")
    val df = Frame.solid(y.rowCount, ("x", x), ("y", y))

    val c = new ModeClassifier()
    c.learn(df, "y")
    c.predict(df)

    c.prediction.labels.forall(label => label == "a") should be(right = true)
  }

  "mode classifier " should " work with target and nominal input " in {
    val x = Nominal("1", "3", "5", "2", "6", "7", "4")
    val y = Nominal("a", "b", "?", "c", "g", "a", "a")
    val df = Frame.solid(y.rowCount, ("x", x), ("y", y))

    val c = new ModeClassifier()
    c.learn(df, "y")
    c.predict(df)

    c.prediction.labels.forall(label => label == "a") should be(right = true)
  }

  "mode classifier " should " mode if single mode " in {
    val y = Nominal("a", "b", "a", "b", "a", "b", "a")
    val df = Frame.solid(y.rowCount, ("y", y))

    val c = new ModeClassifier()
    c.learn(df, "y")
    c.predict(df)

    c.prediction.labels.forall(label => label == "a") should be(right = true)
  }

  "mode classifier " should " one random of multiple modes " in {
    val y = Nominal("a", "b", "a", "b", "a", "b", "a", "b", "c", "c")
    val df = Frame.solid(y.rowCount, ("y", y))

    val c = new ModeClassifier()
    c.learn(df, "y")
    c.predict(df)

    c.prediction.labels.forall(label => (label == "a") || (label == "b")) should be(right = true)
  }

}
