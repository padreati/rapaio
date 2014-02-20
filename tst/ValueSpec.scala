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

/**
 * Created with IntelliJ IDEA.
 * User: tincu
 * Date: 2/12/14
 * Time: 3:49 PM
 * To change this template use File | Settings | File Templates.
 */

import org.scalatest.{FlatSpec, Matchers}
import rapaio.data.Value

class ValueSpec extends FlatSpec with Matchers {
  "A value" should "contain the same double values when used with a fill parameter" in {
    val x = new Value(10, 10, 100.12)
    for (index <- 0 until x.rowCount - 1) {
      x.values(index) should be(100.12)
    }
  }

  "A value" should "only contain Double.NaN when a fill value is not used" in {
    val x = new Value(10)
    for (index <- 0 until x.rowCount - 1) {
      x.values(index) should not be (Double.NaN)
    }
  }
}
