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
package rapaio.graphics.base

import java.awt._
import rapaio.data._
import scala.collection.mutable

/**
 * @author tutuianu
 */
object AbstractFigure {
  protected val TITLE_FONT = new Font("Verdana", Font.BOLD, 18)
  protected val MARKERS_FONT = new Font("Verdana", Font.PLAIN, 13)
  protected val LABELS_FONT = new Font("Verdana", Font.BOLD, 16)
  protected val DEFAULT_THICKER_MIN_SPACE = 50.0
  protected val THICKER_PAD = 7
  protected val MARKER_PAD = 15
  protected val LABEL_PAD = 30
  protected val TITLE_PAD = 40
  protected val MINIMUM_PAD = 20
}

abstract class AbstractFigure extends Figure {

  protected var viewport: Rectangle = null
  protected var range: Range = null
  protected val bottomMarkersMsg = new mutable.MutableList[String]
  protected val bottomMarkersPos = new mutable.MutableList[Double]
  protected val leftMarkersMsg = new mutable.MutableList[String]
  protected val leftMarkersPos = new mutable.MutableList[Double]
  protected var title: String = null
  protected var leftLabel: String = null
  protected var bottomLabel: String = null
  protected var thickerMinSpace: Double = AbstractFigure.DEFAULT_THICKER_MIN_SPACE
  protected var x1: Double = Double.NaN
  protected var x2: Double = Double.NaN
  protected var y1: Double = Double.NaN
  protected var y2: Double = Double.NaN

  protected var leftThicker: Boolean = false
  protected var bottomThicker: Boolean = false
  protected var leftMarkers: Boolean = false
  protected var bottomMarkers: Boolean = false

  protected var options: GraphicOptions = new GraphicOptions

  def buildRange: Range

  def getRange: Range = {
    if (range == null) {
      range = buildRange
    }
    range
  }

  def buildViewport(rectangle: Rectangle) {
    viewport = new Rectangle(rectangle)
    viewport.x += 2 * AbstractFigure.THICKER_PAD
    if (leftMarkers) {
      viewport.x += AbstractFigure.MARKER_PAD
    }
    if (leftLabel != null) {
      viewport.x += AbstractFigure.LABEL_PAD
    }
    viewport.x += AbstractFigure.MINIMUM_PAD
    if (title != null) {
      viewport.y += AbstractFigure.TITLE_PAD
    }
    viewport.y += AbstractFigure.MINIMUM_PAD
    viewport.width = rectangle.width - viewport.x - AbstractFigure.MINIMUM_PAD
    var height: Int = 0
    height += 2 * AbstractFigure.THICKER_PAD
    if (bottomMarkers) {
      height += AbstractFigure.MARKER_PAD
    }
    if (bottomLabel != null) {
      height += AbstractFigure.LABEL_PAD
    }
    height += AbstractFigure.MINIMUM_PAD
    viewport.height = rectangle.height - viewport.y - height
  }

  def xscale(x: Double): Double = {
    viewport.x + viewport.width * (x - range.x1) / (range.x2 - range.x1)
  }

  def yscale(y: Double): Double = {
    viewport.y + viewport.height * (1.-(y - range.y1) / (range.y2 - range.y1))
  }

  def setXRange(start: Double, end: Double): AbstractFigure = {
    this.x1 = start
    this.x2 = end
    this
  }

  def setYRange(start: Double, end: Double): AbstractFigure = {
    this.y1 = start
    this.y2 = end
    this
  }

  def paint(g2d: Graphics2D, rect: Rectangle) {
    buildViewport(rect)
    range = buildRange
    g2d.setColor(StandardColorPalette.color(255))
    g2d.fill(rect)
    g2d.setBackground(StandardColorPalette.color(255))
    g2d.setColor(StandardColorPalette.color(0))
    if (title != null) {
      g2d.setFont(AbstractFigure.TITLE_FONT)
      val titleWidth: Double = g2d.getFontMetrics.getStringBounds(title, g2d).getWidth
      g2d.drawString(title, (rect.width - titleWidth).asInstanceOf[Int] / 2, AbstractFigure.TITLE_PAD)
    }
    buildLeftMarkers()
    g2d.setFont(AbstractFigure.MARKERS_FONT)
    g2d.drawLine(viewport.x - AbstractFigure.THICKER_PAD, viewport.y, viewport.x - AbstractFigure.THICKER_PAD, viewport.y + viewport.height)
    for (i <- 0 until leftMarkersPos.size) {
      {
        if (leftThicker) {
          g2d.drawLine(viewport.x - 2 * AbstractFigure.THICKER_PAD, viewport.y + leftMarkersPos(i).toInt, viewport.x - AbstractFigure.THICKER_PAD, viewport.y + leftMarkersPos(i).toInt)
        }
        if (leftMarkers) {
          val xx: Int = viewport.x - 3 * AbstractFigure.THICKER_PAD
          val yy: Int = (viewport.y + viewport.height - leftMarkersPos(i) + g2d.getFontMetrics.getStringBounds(leftMarkersMsg(i), g2d).getWidth / 2).toInt
          g2d.translate(xx, yy)
          g2d.rotate(-Math.PI / 2)
          g2d.drawString(leftMarkersMsg(i), 0, 0)
          g2d.rotate(Math.PI / 2)
          g2d.translate(-xx, -yy)
        }
      }
    }

    if (leftLabel != null) {
      g2d.setFont(AbstractFigure.LABELS_FONT)
      val ywidth: Double = g2d.getFontMetrics.getStringBounds(leftLabel, g2d).getWidth
      val xx: Int = viewport.x - 5 * AbstractFigure.THICKER_PAD - AbstractFigure.MARKER_PAD
      val yy: Int = ((rect.height + ywidth) / 2).asInstanceOf[Int]
      g2d.translate(xx, yy)
      g2d.rotate(-Math.PI / 2)
      g2d.drawString(leftLabel, 0, 0)
      g2d.rotate(Math.PI / 2)
      g2d.translate(-xx, -yy)
    }
    buildBottomMarkers()
    g2d.setFont(AbstractFigure.MARKERS_FONT)
    g2d.drawLine(viewport.x, viewport.y + viewport.height + AbstractFigure.THICKER_PAD, viewport.x + viewport.width, viewport.y + viewport.height + AbstractFigure.THICKER_PAD)
    for (i <- 0 until bottomMarkersPos.size) {
      {
        if (bottomThicker) {
          g2d.drawLine(
            viewport.x + bottomMarkersPos(i).toInt,
            viewport.y + viewport.height + AbstractFigure.THICKER_PAD,
            viewport.x + bottomMarkersPos(i).toInt,
            viewport.y + viewport.height + 2 * AbstractFigure.THICKER_PAD)
        }
        if (bottomMarkers) {
          g2d.drawString(
            bottomMarkersMsg(i),
            viewport.x + bottomMarkersPos(i).toInt - g2d.getFontMetrics.getStringBounds(bottomMarkersMsg(i), g2d).getWidth.toInt / 2,
            viewport.y + viewport.height + 2 * AbstractFigure.THICKER_PAD + AbstractFigure.MARKER_PAD)
        }
      }
    }
    if (bottomLabel != null) {
      g2d.setFont(AbstractFigure.LABELS_FONT)
      val xwidth: Double = g2d.getFontMetrics.getStringBounds(bottomLabel, g2d).getWidth
      g2d.drawString(bottomLabel, ((rect.width - xwidth) / 2).asInstanceOf[Int], viewport.y + viewport.height + 2 * AbstractFigure.THICKER_PAD + AbstractFigure.MARKER_PAD + AbstractFigure.LABEL_PAD)
    }
  }

  protected def buildNumericBottomMarkers() {
    bottomMarkersPos.clear()
    bottomMarkersMsg.clear()
    val xspots: Int = math.floor(viewport.width / thickerMinSpace).toInt
    val xspotwidth: Double = viewport.width / xspots
    for (i <- 0 to xspots) {
      bottomMarkersPos += i * xspotwidth
      bottomMarkersMsg += ("%." + range.getProperDecimalsX.toString + "f").format(range.x1 + range.getWidth * i / xspots)
    }
  }

  protected def buildNumericLeftMarkers() {
    leftMarkersPos.clear()
    leftMarkersMsg.clear()
    val yspots: Int = math.floor(viewport.height / thickerMinSpace).toInt
    val yspotwidth: Double = viewport.height / yspots
    for (i <- 0 to yspots) {
      leftMarkersPos += i * yspotwidth
      leftMarkersMsg += ("%." + range.getProperDecimalsY + "f").format(range.y1 + range.getHeight * i / yspots)
    }
  }

  def buildLeftMarkers() {
  }

  def buildBottomMarkers() {
  }

}