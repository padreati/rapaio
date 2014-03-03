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
 * Compensated version of arithmetic mean of values from a [[Feature]].
 * <p>
 * User: <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a>
 * Date: 9/7/13
 * Time: 12:21 PM
 */
final class Mean extends Printable {
  private var _value: Double = _

  private def compute(feature: Feature): Mean = {
    var sum = 0.0
    var count = 0.0
    for (i <- 0 until feature.rowCount) {
      if (!feature.missing(i)) {
        sum += feature.values(i)
        count += 1
      }
    }
    if (count == 0) {
      _value = Double.NaN
    } else {
      sum /= count
      var t: Double = 0
      for (i <- 0 until feature.rowCount) {
        if (!feature.missing(i)) {
          t += feature.values(i) - sum
        }
      }
      sum += t / count
      _value = sum
    }
    this
  }

  def value: Double = _value

  override def buildSummary(sb: StringBuilder) {
    sb.append("> mean\n%.10f".format(_value))
  }
}

object Mean {
  def apply(feature: Feature): Mean = new Mean().compute(feature)
}