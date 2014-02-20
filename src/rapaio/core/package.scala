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

package rapaio

import rapaio.core.stat.Mean
import rapaio.data.Feature

/**
 * @author <a href="email:padreati@yahoo.com>Aurelian Tutuianu</a>
 */
package object core {

  private val MathLog2 = math.log(2.0)

  def mean(v: Feature): Mean = new Mean(v)

  /**
   * Returns the base 2 logarithm of a double value
   *
   * @param x the number from which we take base 2 logarithm
   * @return the base 2 logarithm of input getValue
   */
  def log2(x: Double): Double = {
    return math.log(x) / MathLog2
  }
}
