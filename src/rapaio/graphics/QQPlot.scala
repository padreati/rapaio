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

package rapaio.graphics

import rapaio.core.distributions.Distribution
import rapaio.data.{Feature, Value}
import rapaio.data.mapping.{Mapping, MappedFeature}
import rapaio.graphics.plotc.Points

/**
 * @author <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a>
 */
class QQPlot(sample: Feature, distribution: Distribution) extends Plot {
  leftLabel = "StatSampling Quantiles"
  bottomLabel = "Theoretical Quantiles"

  val order = (0 until sample.rowCount).sortWith((i, j) => {
    if (sample.missing(i)) !sample.missing(j)
    else if (sample.missing(j)) false
    else sample.values(i) < sample.values(j)
  }).map(i => sample.rowId(i))

  val x = MappedFeature(sample.source, Mapping(order.toList))

  val y = new Value(x.rowCount)
  for (i <- 0 until x.rowCount) {
    val p = (i + 1) / (x.rowCount + 1.0)
    y.values(i) = distribution.quantile(p)
  }
  add(new Points(x, y, col = 0))
}