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

import rapaio.graphics.base._
import rapaio.graphics.plot._
import java.awt._
import scala.collection.mutable.MutableList
import rapaio.data.Feature

/**
 * @author <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a>
 */
class Plot extends AbstractFigure {

  private final val components = new MutableList[PlotComponent]

  bottomThicker = true
  bottomMarkers = true
  leftThicker = true
  leftMarkers = true

  private def mergeRange(a: Range, b: Range): Range = {
    if (a == null) b
    else {
      a.union(b)
      a
    }
  }

  def buildRange: Range = {
    var r: Range = components.map(pc => pc.getRange).fold(null)((a, b) => mergeRange(a, b))
    if (r == null) {
      r = new Range(0, 0, 1, 1)
    }
    if (x1 == x1 && x2 == x2) {
      r.x1 = x1
      r.x2 = x2
    }
    if (y1 == y1 && y2 == y2) {
      r.y1 = y1
      r.y2 = y2
    }
    if (r.y1 == r.y2) {
      r.y1 -= 0.5
      r.y2 += 0.5
    }
    if (r.x1 == r.x2) {
      r.x1 -= 0.5
      r.x2 += 0.5
    }
    r
  }

  private def add(pc: PlotComponent): Plot = {
    pc.parent = this
    pc.initialize
    components += pc
    this
  }

  def points(x: Feature = null,
             y: Feature,
             col: ColorOption = GraphicOptions.DEFAULT_COLOR,
             pch: PchOption = GraphicOptions.DEFAULT_PCH,
             sz: SizeOption = GraphicOptions.DEFAULT_SZ): Plot = {
    val points = new Points(x, y)
    points.options.col = if (col == GraphicOptions.DEFAULT_COLOR) options.col else col
    points.options.pch = if (pch == GraphicOptions.DEFAULT_PCH) options.pch else pch
    points.options.sz = if (sz == GraphicOptions.DEFAULT_SZ) options.sz else sz
    add(points)
  }

  def hist(x: Feature,
           bins: Int = 30,
           prob: Boolean = true,
           min: Double = Double.NaN,
           max: Double = Double.NaN,
           col: ColorOption = 7): Plot = {
    val hist = new Histogram(x, bins, prob, min, max)
    hist.options.col = if (col == GraphicOptions.DEFAULT_COLOR) options.col else col
    add(hist)
  }

  override def paint(g2d: Graphics2D, rect: Rectangle) {
    super.paint(g2d, rect)
    for (pc <- components) {
      pc.paint(g2d)
    }
  }

  override def buildLeftMarkers() {
    buildNumericLeftMarkers()
  }

  override def buildBottomMarkers() {
    buildNumericBottomMarkers()
  }

  override def setXRange(start: Double, end: Double): Plot = {
    super.setXRange(start, end)
    return this
  }

  override def setYRange(start: Double, end: Double): Plot = {
    super.setYRange(start, end)
    return this
  }
}

object Plot {
  def apply(col: ColorOption = GraphicOptions.DEFAULT_COLOR,
            pch: PchOption = GraphicOptions.DEFAULT_PCH,
            lwd: LwdOption = GraphicOptions.DEFAULT_LWD,
            sz: SizeOption = GraphicOptions.DEFAULT_SZ,
            xRange: (Double, Double) = (Double.NaN, Double.NaN),
            yRange: (Double, Double) = (Double.NaN, Double.NaN)): Plot = {
    val plot = new Plot()
    plot.options.col = col
    plot.options.pch = pch
    plot.options.lwd = lwd
    plot.options.sz = sz
    plot.setXRange(xRange._1, xRange._2)
    plot.setYRange(yRange._1, yRange._2)
    plot
  }
}