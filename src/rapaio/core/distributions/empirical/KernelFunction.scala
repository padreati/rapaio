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

package rapaio.core.distributions.empirical

import rapaio.core.distributions.{Normal, Distribution}

/**
 * User: <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a>
 */
trait KernelFunction {

  def pdf(x: Double, x0: Double, bandwidth: Double): Double

  def minValue(x0: Double, bandwidth: Double): Double

  def maxValue(x0: Double, bandwidth: Double): Double
}

object KernelFunctionUniform extends KernelFunction {
  def pdf(x: Double, x0: Double, bandwidth: Double): Double = {
    val value: Double = math.abs(x - x0) / bandwidth
    if (value <= 1) 0.5
    else 0.0
  }

  def minValue(x0: Double, bandwidth: Double): Double = x0 - bandwidth

  def maxValue(x0: Double, bandwidth: Double): Double = x0 + bandwidth
}

object KernelFunctionGaussian extends KernelFunction {
  def pdf(x: Double, x0: Double, bandwidth: Double): Double = normal.pdf((x - x0) / bandwidth)

  def minValue(x0: Double, bandwidth: Double): Double = x0 - 4 * bandwidth

  def maxValue(x0: Double, bandwidth: Double): Double = x0 + 4 * bandwidth

  private final val normal: Distribution = Normal()
}

object KernelFunctionBiWeight extends KernelFunction {
  def pdf(x: Double, x0: Double, bandwidth: Double): Double = {
    val value: Double = math.abs(x - x0) / bandwidth
    if (value <= 1) 15 * (1 - value * value) * (1 - value * value) / 16.0 else 0
  }

  def minValue(x0: Double, bandwidth: Double): Double = x0 - bandwidth

  def maxValue(x0: Double, bandwidth: Double): Double = x0 + bandwidth
}

object KernelFunctionTriWeight extends KernelFunction {
  def pdf(x: Double, x0: Double, bandwidth: Double): Double = {
    val value: Double = math.abs(x - x0) / bandwidth
    if (value <= 1) {
      val weight: Double = 1 - value * value
      35.0 * weight * weight * weight / 32.0
    } else 0.0
  }

  def minValue(x0: Double, bandwidth: Double): Double = x0 - bandwidth

  def maxValue(x0: Double, bandwidth: Double): Double = x0 + bandwidth
}

object KernelFunctionEpanechnikov extends KernelFunction {
  def pdf(x: Double, x0: Double, bandwidth: Double): Double = {
    val value: Double = math.abs(x - x0) / bandwidth
    if (value <= 1) 3.0 * (1 - value * value) / 4.0 else 0
  }

  def minValue(x0: Double, bandwidth: Double): Double = x0 - bandwidth

  def maxValue(x0: Double, bandwidth: Double): Double = x0 + bandwidth
}

object KernelFunctionTriangular extends KernelFunction {
  def pdf(x: Double, x0: Double, bandwidth: Double): Double = {
    val value: Double = math.abs(x - x0) / bandwidth
    if (value <= 1) 1 - value else 0
  }

  def minValue(x0: Double, bandwidth: Double): Double = x0 - bandwidth

  def maxValue(x0: Double, bandwidth: Double): Double = x0 + bandwidth
}

object KernelFunctionCosine extends KernelFunction {
  def pdf(x: Double, x0: Double, bandwidth: Double): Double = {
    val value: Double = math.abs(x - x0) / bandwidth
    if (value <= 1) {
      math.Pi * Math.cos(math.Pi * value / 2) / 4.0
    } else 0
  }

  def minValue(x0: Double, bandwidth: Double): Double = x0 - bandwidth

  def maxValue(x0: Double, bandwidth: Double): Double = x0 + bandwidth
}

object KernelFunctionTriCube extends KernelFunction {
  def pdf(x: Double, x0: Double, bandwidth: Double): Double = {
    val value: Double = math.abs(x - x0) / bandwidth
    if (value <= 1) {
      val weight: Double = 1 - value * value * value
      70.0 * weight * weight * weight / 81.0
    } else 0.0
  }

  def minValue(x0: Double, bandwidth: Double): Double = x0 - bandwidth

  def maxValue(x0: Double, bandwidth: Double): Double = x0 + bandwidth
}