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

import rapaio.printer.Printable
import rapaio.data.Feature
import rapaio.data.mapping.{Mapping, MappedFeature}

/**
 * Estimates quantiles from a numerical feature.
 * <p/>
 * The estimated quantiles implements R-8, SciPy-(1/3,1/3) version of estimating quantiles.
 * <p/>
 * For further reference see:
 * http://en.wikipedia.org/wiki/Quantile
 * <p/>
 * User: <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a>
 */
class Quantiles(feature: Feature, percentiles: Array[Double]) extends Printable {

  private val quantiles: Array[Double] = {
    if (feature.rowCount == 1) {
      val values: Array[Double] = new Array[Double](percentiles.length)
      for (i <- 0 until values.length) {
        values(i) = feature.values(0)
      }
      values
    }
    else {
      val map = (0 until feature.rowCount).sortWith((i, j) => feature.values(i) < feature.values(j))
      val sorted: Feature = MappedFeature(feature, Mapping(map.toList))
      var start: Int = 0
      while (start < sorted.rowCount && sorted.missing(start)) start += 1

      val values: Array[Double] = new Array[Double](percentiles.length)
      if (start == sorted.rowCount) {
        values
      }
      for (i <- 0 until percentiles.length) {
        {
          val N: Double = sorted.rowCount - start
          val h: Double = (N + 1.0 / 3.0) * percentiles(i) + 1.0 / 3.0
          val hfloor: Int = math.floor(h).asInstanceOf[Int]
          if (percentiles(i) < (2.0 / 3.0) / (N + 1.0 / 3.0)) {
            values(i) = sorted.values(start)
          } else if (percentiles(i) >= (N - 1.0 / 3.0) / (N + 1.0 / 3.0)) {
            values(i) = sorted.values(sorted.rowCount - 1)
          } else
            values(i) = sorted.values(start + hfloor - 1) +
              (h - hfloor) * (sorted.values(start + hfloor) -
                sorted.values(start + hfloor - 1))
        }
      }
      values
    }
  }

  def values: Array[Double] = quantiles

  override def buildSummary(sb: StringBuilder): Unit = {
    sb.append("quantiles - estimated quantiles\n")
    for (i <- 0 until quantiles.length) {
      sb.append("quantile[%f = %f\n".format(percentiles(i), quantiles(i)))
    }
  }
}