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

import rapaio.graphics.base.{Figure, Range}
import java.awt._
import java.awt.geom.Rectangle2D

/**
 * @author <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a>
 */
class Legend(x: Double, y: Double, labels: Array[String], col: Array[Int]) extends PlotComponent {

  def paint(g2d: Graphics2D) {
    g2d.setFont(Figure.MARKERS_FONT)
    var minHeight = Double.MaxValue
    for (string <- labels) {
      minHeight = math.min(minHeight, g2d.getFontMetrics.getStringBounds(string, g2d).getHeight)
    }
    val size = g2d.getFontMetrics.getStringBounds("aa", g2d).getWidth
    val xx = parent.xScale(x)
    var yy = parent.yScale(y)
    for (i <- 0 until labels.length) {
      g2d.setColor(options.col(i))
      g2d.draw(new Rectangle2D.Double(xx, yy - minHeight / 3, size, 1))
      g2d.drawString(labels(i), (xx + size + size / 2).asInstanceOf[Int], yy.asInstanceOf[Int])
      yy += minHeight + 1
    }
  }

  def buildRange(): Range = null
}