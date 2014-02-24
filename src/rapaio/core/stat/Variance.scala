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

package rapaio.core.stat

import rapaio.data.Feature
import rapaio.workspace.Workspace.code
import rapaio.printer.Summarizable

/**
 * Compensated version of the algorithm for calculation of
 * sample variance of values from a numeric feature.
 * <p/>
 * <p/>
 * User: <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a>
 * Date: 9/7/13
 * Time: 12:26 PM
 */
class Variance(feature: Feature) extends Summarizable {

  require(feature.isNumeric, "Variance can be computed on numeric features only.")

  private final val _value: Double = {
    val mean: Double = new Mean(feature).value
    var n: Double = 0
    for (i <- 0 until feature.rowCount)
      if (!feature.missing(i)) {
        n += 1
      }

    if (n == 0) {
      Double.NaN
    }
    var sum2: Double = 0
    var sum3: Double = 0
    for (i <- 0 until feature.rowCount) {
      if (!feature.missing(i)) {
        sum2 += math.pow(feature.values(i) - mean, 2)
        sum3 += feature.values(i) - mean
      }
    }
    (sum2 - math.pow(sum3, 2) / n) / (n - 1)
  }

  def getValue: Double = _value

  override def summary(): Unit = code("variance\n%.10f".format(_value))

}