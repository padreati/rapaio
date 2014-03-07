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
class DUniform extends Distribution {
  var a: Double = _
  var b: Double = _

  def name: String = "Discrete Uniform Distribution "

  def pdf(x: Double): Double = {
    val rint: Double = math.rint(x)
    if (!x.isNaN && !x.isInfinite && x == rint)
      if (x < a || x > b) 0
      else 1 / (b - a + 1.0)
    else 0
  }

  def cdf(x: Double): Double = {
    if (x < a) 0
    else if (x > b) 1
    else (math.floor(x) - a + 1) / (b - a + 1)
  }

  def quantile(p: Double): Double = {
    require(p >= 0 && p <= 1, "Probability must be interface the range [0,1], not " + p)

    if (a == b && p == 1) a
    else (a + p * (b - a + 1)).asInstanceOf[Int]
  }

  def min: Double = a

  def max: Double = b

  def mean: Double = (b - a) / 2.0

  def mode: Double = mean

  def variance: Double = ((b - a + 1) * 2 - 1) / 12.0

  def skewness: Double = 0

  def kurtosis: Double = -6.*(math.pow(b - a, 2) + 1) / (5.0 * (math.pow(b - a, 2) - 1))
}

object DUniform {
  def apply(a: Int, b: Int): DUniform = {
    val d = new DUniform
    d.a = a
    d.b = b
    d
  }
}