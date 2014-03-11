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
 * @author Aurelian Tutuianu
 */
object Normal {
  private def cdfMarsaglia(x: Double): Double = {
    var s: Double = x
    var t: Double = 0
    var b: Double = x
    val q: Double = x * x
    var i: Double = 1
    while (s != t) {
      t = s
      b *= q
      i += 2
      s = t + b / i
    }
    0.5 + s * math.exp(-.5 * q - 0.91893853320467274178)
  }
}

class Normal(mu: Double = 0.0, sd: Double = 1.0) extends Distribution {

  def name: String = "Normal"

  def pdf(x: Double): Double = 1 / math.sqrt(2 * math.Pi * variance) * math.exp(-math.pow(x - mu, 2) / (2 * variance))

  def cdf(x: Double): Double = cdf(x, mu, sd)

  private def cdf(x: Double, mu: Double, sd: Double): Double = {
    require(!x.isNaN && !x.isInfinite, "X is not a real number")
    Normal.cdfMarsaglia((x - mu) / sd)
  }

  def quantile(p: Double): Double = {
    require(p >= 0.0 && p <= 1.0, "Inverse of a probability requires a probablity in the range [0,1], not " + p)

    if (p == 0) {
      Double.NegativeInfinity
    } else if (p == 1) {
      Double.PositiveInfinity
    } else {
      val a = Array(-3.969683028665376e+01, 2.209460984245205e+02, -2.759285104469687e+02, 1.383577518672690e+02, -3.066479806614716e+01, 2.506628277459239e+00)
      val b = Array(-5.447609879822406e+01, 1.615858368580409e+02, -1.556989798598866e+02, 6.680131188771972e+01, -1.328068155288572e+01)
      val c = Array(-7.784894002430293e-03, -3.223964580411365e-01, -2.400758277161838e+00, -2.549732539343734e+00, 4.374664141464968e+00, 2.938163982698783e+00)
      val d = Array(7.784695709041462e-03, 3.224671290700398e-01, 2.445134137142996e+00, 3.754408661907416e+00)

      val p_low: Double = 0.02425
      val p_high: Double = 1 - p_low
      var result: Double =
        if (0 < p && p < p_low) {
          val q: Double = math.sqrt(-2 * math.log(p))
          (((((c(0) * q + c(1)) * q + c(2)) * q + c(3)) * q + c(4)) * q + c(5)) / ((((d(0) * q + d(1)) * q + d(2)) * q + d(3)) * q + 1)
        } else if (p_low <= p && p <= p_high) {
          val q: Double = p - 0.5
          val r: Double = q * q
          (((((a(0) * r + a(1)) * r + a(2)) * r + a(3)) * r + a(4)) * r + a(5)) * q / (((((b(0) * r + b(1)) * r + b(2)) * r + b(3)) * r + b(4)) * r + 1)
        }
        else {
          val q: Double = math.sqrt(-2 * math.log(1 - p))
          -(((((c(0) * q + c(1)) * q + c(2)) * q + c(3)) * q + c(4)) * q + c(5)) / ((((d(0) * q + d(1)) * q + d(2)) * q + d(3)) * q + 1)
        }
      val e = cdf(result, 0, 1) - p
      val u = e * math.sqrt(2 * math.Pi) * math.exp(result * result / 2)

      result = result - u / (1 + result * u / 2)
      result * sd + mu
    }
  }

  def min: Double = Double.MinValue

  def max: Double = Double.MaxValue

  def mean: Double = mu

  def mode: Double = mean

  def variance: Double = math.pow(sd, 2)

  def skewness: Double = 0.0

  def kurtosis: Double = 0.0
}