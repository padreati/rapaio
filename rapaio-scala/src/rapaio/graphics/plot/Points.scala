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

import rapaio.data._
import rapaio.graphics.base._
import java.awt._
import scala.collection.mutable

/**
 * @author tutuianu
 */
class Points(private val x: Vector, private var y: Vector) extends PlotComponent {

  private val xData = new mutable.MutableList[Double]
  private val yData = new mutable.MutableList[Double]

  for (i <- 0 until math.min(x.getRowCount, y.getRowCount)) {
    if (!x.isMissing(i) && !y.isMissing(i)) {
      xData += x.getValue(i)
      yData += y.getValue(i)
    }
  }

  def buildRange: Range = {
    if (xData.length == 0) null
    val range = new Range
    for ((x, y) <- (xData zip yData)) range.union(x, y)
    range
  }

  def paint(g2d: Graphics2D) {
    g2d.setBackground(StandardColorPalette.color(255))
    for (i <- 0 until x.getRowCount) {
      g2d.setColor(options.col(i))
      g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.75f))
      val xx: Int = parent.xscale(x.getValue(i)).toInt
      val yy: Int = parent.yscale(y.getValue(i)).toInt
      PchPalette.draw(g2d, xx, yy, options.sz(i), options.pch(i))
    }
  }
}