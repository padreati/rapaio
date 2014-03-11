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

import rapaio.core.SpecialMath._

/**
 * @author <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a>
 */
class StudentT(df: Double, mu: Double, sigma: Double) extends Distribution {

  def name: String = f"Student-T(df=${df}, mu=${mu}, sigma=${sigma}})"

  def pdf(t: Double): Double = {
    math.exp(lnGamma((df + 1) / 2) - lnGamma(df / 2) - math.log(df * math.Pi) / 2 -
      math.log(sigma) - (df + 1) / 2 * math.log(1 + math.pow((t - mu) / sigma, 2) / df))
  }

  def cdf(t: Double): Double = {
    val x: Double = df / (df + math.pow((t - mu) / sigma, 2))
    val p: Double = betaIncReg(x, df / 2, 0.5) / 2
    if (t > mu) 1 - p
    else p
  }

  def quantile(p: Double): Double = {
    require(p >= 0 && p <= 1, "Probability must be in the range [0,1]")

    var x: Double = invBetaIncReg(2 * Math.min(p, 1 - p), df / 2, 0.5)
    x = sigma * math.sqrt(df * (1 - x) / x)
    if (p >= 0.5) mu + x
    else mu - x
  }

  def min: Double = Double.NegativeInfinity

  def max: Double = Double.PositiveInfinity

  def mean: Double = mu

  def mode: Double = mu

  def skewness: Double = if (df <= 3) Double.NaN else 0

  def variance: Double = {
    if (df <= 1) Double.NaN
    else if (df == 2) Double.PositiveInfinity
    else df / (df - 2) * sigma * sigma
  }

  def kurtosis: Double = {
    if (df <= 2) Double.NaN
    else if (df <= 4) Double.PositiveInfinity
    else 6 / (df - 4)
  }
}