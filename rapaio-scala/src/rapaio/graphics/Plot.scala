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

import rapaio.graphics.base.AbstractFigure
import rapaio.graphics.base.Range
import rapaio.graphics.plot.PlotComponent
import java.awt._
import scala.collection.mutable

/**
 * @author tutuianu
 */
class Plot extends AbstractFigure {

  private final val components = new mutable.MutableList[PlotComponent]

  setBottomThicker(true)
  setBottomMarkers(true)
  setLeftThicker(true)
  setLeftMarkers(true)

  def buildRange: Range = {
    var r: Range = null
    for (pc <- components) {
      val newrange: Range = pc.getRange
      if (newrange != null) {
        if (r == null) {
          r = newrange
        }
        else {
          r.union(newrange)
        }
      }
    }
    if (r == null) {
      r = new Range(0, 0, 1, 1)
    }
    if (getXRangeStart == getXRangeStart && getXRangeEnd == getXRangeEnd) {
      r.setX1(getXRangeStart)
      r.setX2(getXRangeEnd)
    }
    if (getYRangeStart == getYRangeStart && getYRangeEnd == getYRangeEnd) {
      r.setY1(getYRangeStart)
      r.setY2(getYRangeEnd)
    }
    if (r.getY1 == r.getY2) {
      r.setY1(r.getY1 - 0.5)
      r.setY2(r.getY2 + 0.5)
    }
    return r
  }

  def add(pc: PlotComponent): Plot = {
    pc.setParent(this)
    pc.initialize
    components += pc
    this
  }

  override def paint(g2d: Graphics2D, rect: Rectangle) {
    super.paint(g2d, rect)
    import scala.collection.JavaConversions._
    for (pc <- components) {
      pc.paint(g2d)
    }
  }

  override def buildLeftMarkers {
    buildNumericLeftMarkers
  }

  override def buildBottomMarkers {
    buildNumericBottomMarkers
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