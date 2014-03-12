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
import java.awt._
import rapaio.graphics.plotc._
import scala.collection.mutable

/**
 * @author <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a>
 */
protected[graphics] class Plot extends Figure {

  private final val components = new mutable.MutableList[PlotComponent]

  bottomThicker = true
  bottomMarkers = true
  leftThicker = true
  leftMarkers = true

  private def mergeRange(a: Range, b: Range): Range = {
    if (a == null) b
    else if (b == null) a
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

  protected[graphics] def add(pc: PlotComponent) {
    pc.parent = this
    pc.initialize()
    components += pc
  }

  override def paint(g2d: Graphics2D, rect: Rectangle) {
    super.paint(g2d, rect)
    for (pc <- components) {
      pc.paint(g2d)
    }
  }

  protected override def buildLeftMarkers() {
    buildNumericLeftMarkers()
  }

  protected override def buildBottomMarkers() {
    buildNumericBottomMarkers()
  }
}

protected[graphics] object Plot {
  protected[graphics] def apply(col: ColorOption = GraphicOptions.DefaultColor,
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