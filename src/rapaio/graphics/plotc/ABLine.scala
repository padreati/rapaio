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

import rapaio.graphics.base.Range
import java.awt._
import java.awt.geom.Line2D

/**
 * @author Aurelian Tutuianu
 */
class ABLine(a: Double, b: Double, h: Boolean, v: Boolean) extends PlotComponent {

  def buildRange(): Range = {
    if (h) new Range(a, Double.NaN, a, Double.NaN)
    else if (v) new Range(Double.NaN, a, Double.NaN, a)
    else null
  }

  def paint(g2d: Graphics2D) {
    val range: Range = parent.range
    var x1: Double = 0
    var x2: Double = 0
    var y1: Double = 0
    var y2: Double = 0
    if (!h && !v) {
      var xx: Double = range.x1
      var yy: Double = a * xx + b
      if (range.contains(xx, yy)) {
        x1 = parent.xScale(xx).asInstanceOf[Int]
        y1 = parent.yScale(yy).asInstanceOf[Int]
      }
      else {
        y1 = parent.yScale(range.y1).asInstanceOf[Int]
        x1 = parent.xScale((range.y1 - b) / a).asInstanceOf[Int]
      }
      xx = range.x2
      yy = a * xx + b
      if (range.contains(xx, yy)) {
        x2 = parent.xScale(xx).asInstanceOf[Int]
        y2 = parent.yScale(yy).asInstanceOf[Int]
      }
      else {
        y2 = parent.yScale(range.y2).asInstanceOf[Int]
        x2 = parent.xScale((range.y2 - b) / a).asInstanceOf[Int]
      }
    }
    else {
      if (h) {
        x1 = parent.xScale(range.x1).asInstanceOf[Int]
        y1 = parent.yScale(b).asInstanceOf[Int]
        x2 = parent.xScale(range.x2).asInstanceOf[Int]
        y2 = parent.yScale(b).asInstanceOf[Int]
      }
      else {
        x1 = parent.xScale(a).asInstanceOf[Int]
        y1 = parent.yScale(range.y1).asInstanceOf[Int]
        x2 = parent.xScale(a).asInstanceOf[Int]
        y2 = parent.yScale(range.y2).asInstanceOf[Int]
      }
    }
    val oldStroke: Stroke = g2d.getStroke
    g2d.setColor(options.col(0))
    g2d.setStroke(new BasicStroke(options.lwd.values(0)))
    g2d.draw(new Line2D.Double(x1, y1, x2, y2))
    g2d.setStroke(oldStroke)
  }
}