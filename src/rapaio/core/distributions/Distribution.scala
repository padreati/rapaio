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

import rapaio.core.RandomSource
import rapaio.data.Value

/**
 * @author Aurelian Tutuianu
 */
abstract class Distribution {

  /**
   * @return canonical name of the distribution
   */
  def name: String

  /**
   * @param x getValue for which it calculates log of probability
   * @return log of probability of x
   */
  def logPdf(x: Double): Double = {
    val xpdf: Double = pdf(x)
    if (xpdf <= 0.0) {
      return Double.NegativeInfinity
    }
    math.log(xpdf)
  }

  /**
   * Calculates probability mass function (pmf) of a discrete distribution or
   * probability density function (pdf) of a continuous distribution for given
   * getValue x
   *
   * @param x getValue for which it calculates
   * @return pmf / pdf of x
   */
  def pdf(x: Double): Double

  def cdf(x: Double): Double

  def quantile(p: Double): Double

  def pdfFunction: (Double) => Double = pdf

  def cdfFunction: (Double) => Double = cdf

  def min: Double

  def max: Double

  def sample(n: Int): Value = {
    val samples = new Value(n)
    samples.values.transform(_ => quantile(RandomSource.nextDouble()))
    samples
  }

  def mean: Double

  def mode: Double

  def variance: Double

  def skewness: Double

  def kurtosis: Double

  def sd: Double = math.sqrt(variance)
}