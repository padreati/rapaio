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

package rapaio.core.sample

import rapaio.core.RandomSource
import rapaio.core.RandomSource.nextDouble

/**
 * User: Aurelian Tutuianu <padreati@yahoo.com>
 */
object DiscreteSampling {

  private var prob: Array[Double] = null
  private var alias: Array[Int] = null

  /**
   * Discrete sampling with repetition.
   * Nothing special, just using the uniform discrete sampler offered by the system.
   */
  def sampleWR(m: Int, populationSize: Int): Array[Int] = Array.fill(m)(RandomSource.nextInt(populationSize))

  /**
   * Draw an uniform discrete sample without replacement.
   * <p/>
   * Implements reservoir sampling.
   *
   * @param sampleSize     sample size
   * @param populationSize population size
   * @return
   */
  def sampleWOR(sampleSize: Int, populationSize: Int): Array[Int] = {
    require(sampleSize <= populationSize, "Can't draw a sample without replacement bigger than population size.")

    val sample = (0 until sampleSize).toArray
    for (i <- sampleSize until populationSize) {
      val j = RandomSource.nextInt(i + 1)
      if (j < sampleSize) {
        sample(j) = i
      }
    }
    sample
  }

  /**
   * Draw m <= n weighted random samples, weight by probabilities
   * without replacement.
   * <p/>
   * Weighted random sampling without replacement.
   * Implements Efraimidis-Spirakis method.
   *
   * @param sampleSize number of samples
   * @param prob       vector of probabilities
   * @return vector with m indices in [0,weights.length-1]
   *         See http://link.springer.com/content/pdf/10.1007/978-0-387-30162-4_478.pdf
   */
  def sampleWeightedWOR(sampleSize: Int, prob: Array[Double]): Array[Int] = {
    validateWeighterWOR(prob, sampleSize)

    if (sampleSize == prob.length) {
      (0 until prob.length).toArray
    } else {
      val result: Array[Int] = new Array[Int](sampleSize)
      var len: Int = 1
      while (len <= sampleSize) {
        len *= 2
      }
      len = len * 2
      val heap: Array[Int] = new Array[Int](len)
      val k: Array[Double] = new Array[Double](sampleSize)

      for (i <- 0 until len) heap(i) = -1

      for (i <- 0 until sampleSize) {
        heap(i + len / 2) = i
        k(i) = math.pow(nextDouble, 1.0 / prob(i))
        result(i) = i
      }

      var i: Int = len / 2 - 1
      while (i > 0) {
        if (heap(i * 2) == -1) {
          heap(i) = -1
        } else if (heap(i * 2 + 1) == -1) {
          heap(i) = heap(i * 2)
        } else if (k(heap(i * 2)) < k(heap(i * 2 + 1))) {
          heap(i) = heap(i * 2)
        } else {
          heap(i) = heap(i * 2 + 1)
        }
        i -= 1
      }

      var pos: Int = sampleSize
      while (pos < prob.length) {
        val r = nextDouble
        val xw: Double = math.log(r) / math.log(k(heap(1)))
        var acc: Double = 0
        while (pos < prob.length) {
          if (acc + prob(pos) < xw) {
            acc += prob(pos)
            pos += 1
          }
        }
        if (pos < prob.length) {

          val tw: Double = math.pow(k(heap(1)), prob(pos))
          val r2: Double = nextDouble * (1.0 - tw) + tw
          val ki: Double = math.pow(r2, 1 / prob(pos))
          k(heap(1)) = ki
          result(heap(1)) = pos
          pos += 1

          var start: Int = heap(1) + len / 2
          while (start > 1) {
            start /= 2
            if (heap(start * 2 + 1) == -1) {
              heap(start) = heap(start * 2)
            } else if (k(heap(start * 2)) < k(heap(start * 2 + 1))) {
              heap(start) = heap(start * 2)
            } else {
              heap(start) = heap(start * 2 + 1)
            }
          }
        }
      }
      result
    }
  }

  private def validateWeighterWOR(p: Array[Double], m: Int) {
    if (m > p.length) {
      throw new IllegalArgumentException("required sample size is bigger than population size")
    }
    var total: Double = 0
    for (i <- 0 until p.length) {
      if (p(i) <= 0) {
        throw new IllegalArgumentException("weights must be strict positive.")
      }
      total += p(i)
    }

    if (total != 1.0) {
      for (i <- 0 until p.length) {
        p(i) /= total
      }
    }
  }

  /**
   * Generate discrete weighted random samples with replacement (same values might occur)
   * with building aliases according to the new probabilities.
   * <p/>
   * Implementation based on Vose alias-method algorithm
   */
  def sampleWeightedWR(m: Int, p: Array[Double] = null): Array[Int] = {
    if (p != null) {
      makeAliasWR(p)
    }
    val sample = Array.fill(m) {
      val column = RandomSource.nextInt(prob.length)
      if (RandomSource.nextDouble < prob(column)) column else alias(column)
    }
    sample
  }

  /**
   * Builds discrete random sampler without replacement
   *
   * @param p The list of probabilities.
   */
  private def makeAliasWR(p: Array[Double]) {
    if (p.length == 0) throw new IllegalArgumentException("Probability vector must be nonempty.")
    prob = Array[Double](p.length)
    Array.copy(p, 0, prob, 0, p.length)
    prob.transform(x => x * prob.length)

    alias = new Array[Int](p.length)
    val dq: Array[Int] = new Array[Int](p.length)
    var smallPos: Int = -1
    var largePos: Int = prob.length
    for (i <- 0 until prob.length) {
      if (prob(i) >= 1.0) {
        dq(largePos - 1) = i
        largePos -= 1
      }
      else {
        dq(smallPos + 1) = i
        smallPos += 1
      }
    }

    while (smallPos >= 0 && largePos <= p.length - 1) {
      val small: Int = dq(smallPos - 1)
      val large: Int = dq(largePos + 1)
      smallPos -= 1
      largePos += 1

      alias(small) = large
      prob(large) = prob(large) + prob(small) - 1.0
      if (prob(large) >= 1.0) {
        dq(largePos - 1) = large
        largePos -= 1
      }
      else {
        dq(smallPos + 1) = large
        smallPos += 1
      }
    }
    while (smallPos > 0) {
      prob(dq(smallPos - 1)) = 1.0
      smallPos -= 1
    }
    while (largePos < dq.length) {
      prob(dq(largePos)) = 1.0
      largePos += 1
    }
  }
}