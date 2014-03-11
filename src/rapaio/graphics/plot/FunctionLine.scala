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

package rapaio.graphics.plot

import rapaio.data.Value
import rapaio.graphics.base.Range
import java.awt._
import java.awt.geom.Line2D

/**
 * @author <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a>
 */
class FunctionLine(f: Double => Double, points: Int) extends PlotComponent {

  def buildRange(): Range = null

  def paint(g2d: Graphics2D) {
    val x = new Value(points + 1)
    val y = new Value(points + 1)
    val xstep = (parent.range.x2 - parent.range.x1) / points
    for (i <- 0 until x.rowCount) {
      x.values(i) = parent.range.x1 + i * xstep
      y.values(i) = f(x.values(i))
    }
    for (i <- 1 until x.rowCount) {
      if (parent.range.contains(x.values(i - 1), y.values(i - 1)) && parent.range.contains(x.values(i), y.values(i))) {
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

object FunctionLine {
  def apply(f: Double => Double): FunctionLine = new FunctionLine(f, 1024)

  def apply(f: Double => Double, points: Int) = new FunctionLine(f, points)
}