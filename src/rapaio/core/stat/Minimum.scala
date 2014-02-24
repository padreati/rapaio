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
 * Finds the minimum value from a [[rapaio.data.Feature]] of values.
 * <p/>
 * Ignores missing elements.
 * <p/>
 * User: <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a>
 * Date: 9/7/13
 * Time: 12:36 PM
 */
class Minimum(feature: Feature) extends Summarizable {
  var min: Double = Double.MaxValue
  var valid: Boolean = false
  var i: Int = 0
  for (i <- 0 until feature.rowCount) {
    if (!feature.missing(i)) {
      valid = true
      min = math.min(min, feature.values(i))
    }
  }
  private val _value = if (valid) min else Double.NaN

  def value: Double = _value

  override def summary(): Unit = code("minimum\n%.10f".format(_value))
}