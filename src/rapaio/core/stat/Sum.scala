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
import rapaio.printer.Summarizable
import rapaio.workspace.Workspace.code

/**
 * Computes the sum of elements for value feature, ignoring missing values.
 * <p/>
 * User: <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a>
 */
class Sum(feature: Feature) extends Summarizable {

  private val _value: Double = {
    def sum(i: Int, _sum: Double): Double = {
      if (i >= feature.rowCount) _sum
      else if (feature.missing(i)) sum(i + 1, _sum)
      else sum(i + 1, _sum + feature.values(i))
    }
    sum(0, 0)
  }

  def value: Double = _value

  override def summary {
    code("sum\n%.10f\n".format(value))
  }
}