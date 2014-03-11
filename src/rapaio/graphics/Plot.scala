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
class Plot extends Figure {

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

  def buildRange(): Range = {
    components.foreach(pc => {
      pc.range = pc.buildRange()
    })
    var r: Range = components.map(pc => pc.range).fold(null)((a, b) => mergeRange(a, b))
    if (r == null) {
      r = new Range(0, 0, 1, 1)
    }
    val x1 = options.xLim._1
    val x2 = options.xLim._2
    val y1 = options.yLim._1
    val y2 = options.yLim._2
    if (x1 == x1) {
      r.x1 = x1
    }
    if (x2 == x2) {
      r.x2 = x2
    }
    if (y1 == y1) {
      r.y1 = y1
    }
    if (y2 == y2) {
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
    pc.initialize()
    components += pc
    this
  }

  def vLine(x: Double,
            lwd: LwdOption = GraphicOptions.DefaultLwd,
            col: ColorOption = GraphicOptions.DefaultColor): Plot = {
    val line = new ABLine(x, 0, h = false, v = true)
    line.options.col = if (col == GraphicOptions.DefaultColor) options.col else col
    line.options.lwd = if (lwd == GraphicOptions.DefaultLwd) options.lwd else lwd
    add(line)
  }

  def hLine(y: Double,
            lwd: LwdOption = GraphicOptions.DefaultLwd,
            col: ColorOption = GraphicOptions.DefaultColor): Plot = {
    val line = new ABLine(0, y, h = true, v = false)
    line.options.col = if (col == GraphicOptions.DefaultColor) options.col else col
    line.options.lwd = if (lwd == GraphicOptions.DefaultLwd) options.lwd else lwd
    add(line)
  }

  def abLine(a: Double, b: Double,
             lwd: LwdOption = GraphicOptions.DefaultLwd,
             col: ColorOption = GraphicOptions.DefaultColor): Plot = {
    val line = new ABLine(a, b, false, false)
    line.options.col = if (col == GraphicOptions.DefaultColor) options.col else col
    line.options.lwd = if (lwd == GraphicOptions.DefaultLwd) options.lwd else lwd
    add(line)
  }

  def lines(x: Feature = null,
            y: Feature,
            col: ColorOption = GraphicOptions.DefaultColor,
            lwd: LwdOption = GraphicOptions.DefaultLwd): Plot = {
    val lines = new Lines(x, y)
    lines.options.col = if (col == GraphicOptions.DefaultColor) options.col else col
    lines.options.lwd = if (lwd == GraphicOptions.DefaultLwd) options.lwd else lwd
    add(lines)
  }


  def points(x: Feature = null,
             y: Feature,
             col: ColorOption = GraphicOptions.DefaultColor,
             pch: PchOption = GraphicOptions.DefaultPch,
             sz: SizeOption = GraphicOptions.DefaultSz): Plot = {
    val points = new Points(x, y)
    points.options.col = if (col == GraphicOptions.DefaultColor) options.col else col
    points.options.pch = if (pch == GraphicOptions.DefaultPch) options.pch else pch
    points.options.sz = if (sz == GraphicOptions.DefaultSz) options.sz else sz
    add(points)
  }

  def hist(x: Feature,
           bins: Int = 30,
           prob: Boolean = true,
           min: Double = Double.NaN,
           max: Double = Double.NaN,
           col: ColorOption = 7,
           xLab: String = null,
           yLab: String = null): Plot = {
    val hist = new Histogram(x, bins, prob, min, max)
    hist.options.col = if (col == GraphicOptions.DefaultColor) options.col else col
    if (xLab != null) bottomLabel = xLab
    if (yLab != null) leftLabel = yLab
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
}

object Plot {
  def apply(col: ColorOption = GraphicOptions.DefaultColor,
            pch: PchOption = GraphicOptions.DefaultPch,
            lwd: LwdOption = GraphicOptions.DefaultLwd,
            sz: SizeOption = GraphicOptions.DefaultSz,
            xLim: (Double, Double) = (Double.NaN, Double.NaN),
            yLim: (Double, Double) = (Double.NaN, Double.NaN),
            xLab: String = null,
            yLab: String = null): Plot = {
    val plot = new Plot()
    plot.options.col = col
    plot.options.pch = pch
    plot.options.lwd = lwd
    plot.options.sz = sz
    plot.options.xLim = xLim
    plot.options.yLim = yLim
    plot.leftLabel = yLab
    plot.bottomLabel = xLab
    plot
  }
}