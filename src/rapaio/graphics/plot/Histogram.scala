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
import rapaio.graphics.base._
import java.awt._
import rapaio.graphics.base.Range

/**
 * @author <a href="email:padreati@yahoo.com>Aurelian Tutuianu</a>
 */
class Histogram(private val v: Feature,
                private val bins: Int = 30,
                private val prob: Boolean = true,
                private var min: Double = Double.NaN,
                private var max: Double = Double.NaN) extends PlotComponent {

  protected var freqTable: Array[Double] = new Array[Double](0)

  override def initialize() {
    parent.leftLabel = if (prob) "density" else "frequency"
    parent.leftThicker = true
    parent.leftMarkers = true
    parent.bottomThicker = true
    parent.bottomMarkers = true
  }

  private def rebuild {
    if (min != min) {
      for (i <- 0 until v.rowCount) {
        if (!v.missing(i)) {
          min = if (min != min) v.values(i)
          else math.min(min, v.values(i))
        }
      }
    }
    if (max != max) {
      for (i <- 0 until v.rowCount) {
        if (!v.missing(i)) {
          max = if (max != max) v.values(i)
          else math.max(max, v.values(i))
        }
      }
    }
    val step = (max - min) / (1.* bins)
    freqTable = new Array[Double](bins)
    var total: Double = 0
    for (i <- 0 until v.rowCount) {
      if (!v.missing(i)) {
        total += 1
        if (!(v.values(i) < min || v.values(i) > max)) {
          var index = ((v.values(i) - min) / step).toInt
          index = if (index == freqTable.length) index - 1 else index
          freqTable(index) += 1
        }
      }
    }

    if (prob && (total != 0))
      for (i <- 0 until freqTable.length)
        freqTable(i) /= (total * step)

    if (options.col.equals(Color.BLACK)) {
      options.col = 7
    }
  }

  def buildRange: Range = {
    rebuild
    val range: Range = new Range
    if (options.xLim._1 != options.xLim._1) {
      range.union(min, Double.NaN)
      range.union(max, Double.NaN)
    } else {
      range.union(options.xLim._1, Double.NaN)
      range.union(options.xLim._2, Double.NaN)
    }
    if (options.yLim._1 != options.yLim._1) {
      for (i <- 0 until freqTable.length) {
        range.union(Double.NaN, freqTable(i))
      }
      range.union(Double.NaN, 0)
    } else {
      range.union(Double.NaN, options.yLim._1)
      range.union(Double.NaN, options.yLim._2)
    }
    return range
  }

  def paint(g2d: Graphics2D) {
    rebuild
    g2d.setColor(StandardColorPalette.color(0))
    for (i <- 0 until freqTable.length) {
      val d = freqTable(i)
      val mind: Double = math.min(d, parent.range.y2)
      if (parent.range.contains(binStart(i), 0)) {
        g2d.setColor(StandardColorPalette.color(0))
        var x = Array[Int](
          parent.xScale(binStart(i)).toInt,
          parent.xScale(binStart(i)).toInt,
          parent.xScale(binStart(i + 1)).toInt,
          parent.xScale(binStart(i + 1)).toInt,
          parent.xScale(binStart(i)).toInt)
        var y = Array[Int](
          parent.yScale(0).toInt,
          parent.yScale(mind).toInt,
          parent.yScale(mind).toInt,
          parent.yScale(0).toInt,
          parent.yScale(0).toInt)
        g2d.drawPolyline(x, y, 5)
        if (d != 0) {
          x = Array[Int](
            parent.xScale(binStart(i)).toInt + 1,
            parent.xScale(binStart(i)).toInt + 1,
            parent.xScale(binStart(i + 1)).toInt,
            parent.xScale(binStart(i + 1)).toInt,
            parent.xScale(binStart(i)).toInt + 1)
          y = Array[Int](
            parent.yScale(0).toInt,
            parent.yScale(mind).toInt + (if (d == mind) 1 else -2),
            parent.yScale(mind).toInt + (if (d == mind) 1 else -2),
            parent.yScale(0).toInt,
            parent.yScale(0).toInt)
          g2d.setColor(options.col(i))
          g2d.fillPolygon(x, y, 5)
        }
      }
    }
  }

  private def binStart(i: Int): Double = {
    val value = min
    val fraction = (max - min) / (1.* bins)
    return value + fraction * (i)
  }
}
