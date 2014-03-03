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
import rapaio.printer.Printable

/**
 * Compensated version of the algorithm for calculation of
 * sample variance of values from a numeric feature.
 * <p/>
 * <p/>
 * User: <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a>
 * Date: 9/7/13
 * Time: 12:26 PM
 */
class Variance extends Printable {

  private var _value: Double = _

  def compute(feature: Feature): Variance = {

    require(feature.isNumeric, "Variance can be computed on numeric features only.")

    val mean: Double = Mean(feature).value
    val n: Double = feature.values.filter(x => !x.isNaN).length
    if (n == 0) {
      Double.NaN
    }
    var sum2: Double = 0
    var sum3: Double = 0
    feature.values.filter(x => !x.isNaN).foreach(x => {
      sum2 += math.pow(x - mean, 2)
      sum3 += x - mean
    })
    _value = (sum2 - math.pow(sum3, 2) / n) / (n - 1)
    this
  }

  def getValue: Double = _value

  override def buildSummary(sb: StringBuilder): Unit = sb.append("variance\n%.10f\n".format(_value))

}

object Variance {
  def apply(feature: Feature): Variance = new Variance().compute(feature)
}