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

import rapaio.data.Feature
import rapaio.graphics.base.{StandardColorPalette, Range}
import java.awt.{BasicStroke, Graphics2D}
import java.awt.geom.Line2D

/**
 * @author <a href="mailto:padreati@yahoo.com>Aurelian Tutuianu</a>
 */
class Lines(private val x: Feature, private var y: Feature) extends PlotComponent {

  def buildRange(): Range = {
    if (math.min(x.rowCount, y.rowCount) == 0) null
    else {
      val range = new Range
      for (i <- 0 until math.min(x.rowCount, y.rowCount)) {
        if (!x.missing(i) && !y.missing(i)) range.union(x.values(i), y.values(i))
      }
      range
    }
  }

  def paint(g2d: Graphics2D) {
    g2d.setStroke(new BasicStroke(options.lwd(0)))
    g2d.setBackground(StandardColorPalette.color(255))

    for (i <- 1 until math.min(x.rowCount, y.rowCount)) {
      g2d.setColor(options.col(i))
      val x1 = parent.xScale(x.values(i - 1))
      val y1 = parent.yScale(y.values(i - 1))
      val x2 = parent.xScale(x.values(i))
      val y2 = parent.yScale(y.values(i))

      //TODO improve this crap to clip only parts of lines outside of the data range
      if (parent.range.contains(x.values(i - 1), y.values(i - 1))
        && parent.range.contains(x.values(i), y.values(i))) {
        g2d.draw(new Line2D.Double(x1, y1, x2, y2))
      }
    }
  }
}
