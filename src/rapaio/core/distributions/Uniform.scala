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

package rapaio.core.distributions

/**
 * @author tutuianu
 */
class Uniform(a: Double, b: Double) extends Distribution {

  def name: String = "Continuous Uniform Distribution"

  def pdf(x: Double): Double = {
    if (x < a || x > b) 0.0
    else if (a == b) 0
    else 1 / (b - a)
  }

  def cdf(x: Double): Double = {
    if (x < a) 0.0
    else if (x > b) 1.0
    else (x - a) / (b - a)
  }

  def quantile(p: Double): Double = {
    require(p >= 0 && p <= 1, "probability getValue should lie in [0,1] interval")
    a + p * (b - a)
  }

  def min: Double = a

  def max: Double = b

  def mean: Double = a + (b - a) / 2.0

  def mode: Double = mean

  def variance: Double = math.pow(b - a, 2) / 12.0

  def skewness: Double = 0.0

  def kurtosis: Double = -6.0 / 5.0
}