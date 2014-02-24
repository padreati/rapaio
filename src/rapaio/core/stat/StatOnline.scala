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


/**
 * Class which implements core online statistics. This class does not hold
 * values used for calculations, just the statistics itself and some additional
 * elements required for calculations.
 * <p>
 * This is an extension over an algorithm presented by John D Cook
 * http://www.johndcook.com/skewness_kurtosis.html.
 * Which itself it is an extension of a method presented by Donald Knuth's The Art of Programming.
 * Which itself it was proposed by B.P Welford.
 * Which itself .. joking. This is the end of the recursion.
 * <p>
 * This class provides online statistics for:
 * <ul>
 * <ui>min - minimum getValue</ui>
 * <ui>max - maximum getValue</ui>
 * <ui>mean - mean of the values</ui>
 * </ul>
 *
 * @author Aurelian Tutuianu
 */
class StatOnline {
  clean()
  private var n: Double = 0.0
  private var m1: Double = 0.0
  private var m2: Double = 0.0
  private var m3: Double = 0.0
  private var m4: Double = 0.0

  def clean() {
    n = 0
    m1 = 0
    m2 = 0
    m3 = 0
    m4 = 0
  }

  /**
   * For now implement this method using only positive values for times. It
   * may be later modified in order to support negative values for times, with
   * the new meaning that we "remove" elements from calculations and as a side
   * effect to decrease the getValue of N;
   *
   * @param x getValue to be used to update statistics
   */
  def update(x: Double): Unit = {
    var delta: Double = 0.0
    var delta_n: Double = 0.0
    var delta_n2: Double = 0.0
    var term1: Double = 0.0
    val n1: Long = Math.rint(n).asInstanceOf[Long]
    n += 1
    delta = x - m1
    delta_n = delta / n
    delta_n2 = delta_n * delta_n
    term1 = delta * delta_n * n1
    m1 += delta_n
    m4 += term1 * delta_n2 * (n * n - 3 * n + 3) + 6 * delta_n2 * m2 - 4 * delta_n * m3
    m3 += term1 * delta_n * (n - 2) - 3 * delta_n * m2
    m2 += term1
  }

  /**
   * @return the number of elements seen so far and used in calculation
   */
  def getN: Double = n

  /**
   * @return the minimum value seen so far
   */
  def min(): Double = min

  def max(): Double = max

  def mean(): Double = m1

  def variance(): Double = m2 / (n - 1.0)

  def sd(): Double = math.sqrt(variance())

  def skewness(): Double = math.sqrt(n) * m3 / math.pow(m2, 1.5)

  def kurtosis(): Double = n * m4 / (m2 * m2) - 3.0

  def append(a: StatOnline) {
    val combined: StatOnline = new StatOnline()
    val delta: Double = this.m1 - a.m1
    val delta2: Double = delta * delta
    val delta3: Double = delta * delta2
    val delta4: Double = delta2 * delta2
    combined.n = a.n + this.n
    combined.m1 = (a.n * a.m1 + this.n * this.m1) / combined.n
    combined.m2 = a.m2 + this.m2 + delta2 * a.n * this.n / combined.n
    combined.m3 = a.m3 + this.m3 + delta3 * a.n * this.n * (a.n - this.n) / (combined.n * combined.n)
    combined.m3 += 3.0 * delta * (a.n * this.m2 - this.n * a.m2) / combined.n
    combined.m4 = a.m4 + this.m4 + delta4 * a.n * this.n * (a.n * a.n - a.n * this.n + this.n * this.n) / (combined.n * combined.n * combined.n)
    combined.m4 += 6.0 * delta2 * (a.n * a.n * this.m2 + this.n * this.n * a.m2) / (combined.n * combined.n) + 4.0 * delta * (a.n * this.m3 - this.n * a.m3) / combined.n
    n = combined.n
    m1 = combined.m1
    m2 = combined.m2
    m3 = combined.m3
    m4 = combined.m4
  }

}