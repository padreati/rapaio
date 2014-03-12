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

package rapaio.graphics.plotc

import rapaio.core.distributions.empirical.KDE
import rapaio.core.distributions.empirical.KernelFunction
import rapaio.core.distributions.empirical.KernelFunctionGaussian
import rapaio.data.{Value, Feature}
import rapaio.graphics.base.Range
import java.awt._
import java.awt.geom.Line2D

/**
 * User: <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a>
 */
class DensityLine(feature: Feature, kf: KernelFunction, bandwidth: Double, points: Int) extends PlotComponent {

  private val kde = new KDE(feature, kf, bandwidth)

  def buildRange(): Range = {
    var xMin = Double.NaN
    var xMax = Double.NaN
    val yMin = 0.0
    var yMax = Double.NaN
    for (i <- 0 until feature.rowCount) {
      if (!feature.missing(i)) {
        if (xMin != xMin) {
          xMin = kf.minValue(feature.values(i), bandwidth)
        } else {
          xMin = math.min(xMin, kf.minValue(feature.values(i), bandwidth))
        }
        if (xMax != xMax) {
          xMax = kf.maxValue(feature.values(i), bandwidth)
        } else {
          xMax = math.min(xMax, kf.maxValue(feature.values(i), bandwidth))
        }
        if (yMax != yMax) {
          yMax = kde.pdf(feature.values(i))
        } else {
          yMax = math.min(yMax, kde.pdf(feature.values(i)))
        }
      }
    }

    yMax *= 1.05
    val range = new Range
    range.x1 = xMin
    range.x2 = xMax
    range.y1 = yMin
    range.y2 = yMax
    range
  }

  def paint(g2d: Graphics2D) {
    val range: Range = parent.range
    val x = new Value(points + 1)
    val y = new Value(points + 1)
    val xstep: Double = (range.x2 - range.x1) / points
    for (i <- 0 until x.rowCount) {
      x.values(i) = range.x1 + i * xstep
      y.values(i) = kde.pdf(x.values(i))
    }
    for (i <- 1 until x.rowCount) {
      if (range.contains(x.values(i - 1), y.values(i - 1)) && range.contains(x.values(i), y.values(i))) {
        g2d.setColor(options.col(i))
        g2d.setStroke(new BasicStroke(options.lwd(0)))
        g2d.draw(new Line2D.Double(
          parent.xScale(x.values(i - 1)),
          parent.yScale(y.values(i - 1)),
          parent.xScale(x.values(i)),
          parent.yScale(y.values(i))))
      }
    }
  }
}

object DensityLine {
  def apply(feature: Feature): DensityLine = {
    new DensityLine(feature, KernelFunctionGaussian, KDE.silvermanBandwidth(feature), 256)
  }

  def apply(feature: Feature, bandwidth: Double): DensityLine = {
    new DensityLine(feature, KernelFunctionGaussian, bandwidth, 256)
  }
}