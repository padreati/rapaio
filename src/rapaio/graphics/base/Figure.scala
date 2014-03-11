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
import scala.collection.mutable

/**
 * @author tutuianu
 */
object Figure {
  val TITLE_FONT = new Font("Verdana", Font.BOLD, 18)
  val MARKERS_FONT = new Font("Verdana", Font.PLAIN, 13)
  val LABELS_FONT = new Font("Verdana", Font.BOLD, 16)
  val DefaultThickerMinSpace = 50.0
  val ThickerPad = 7
  val MarkerPad = 15
  val LABEL_PAD = 30
  val TITLE_PAD = 40
  val MINIMUM_PAD = 20
}

abstract class Figure {

  protected var view: Rectangle = null
  var range: Range = null
  protected val bottomMarkersMsg = new mutable.MutableList[String]
  protected val bottomMarkersPos = new mutable.MutableList[Double]
  protected val leftMarkersMsg = new mutable.MutableList[String]
  protected val leftMarkersPos = new mutable.MutableList[Double]
  var title: String = null
  var leftLabel: String = null
  var bottomLabel: String = null
  protected var thickerMinSpace = Figure.DefaultThickerMinSpace

  var leftThicker = false
  var bottomThicker = false
  var leftMarkers = false
  var bottomMarkers = false

  var options: GraphicOptions = new GraphicOptions

  def buildRange(): Range

  protected def buildViewport(rectangle: Rectangle) {
    view = new Rectangle(rectangle)
    view.x += 2 * Figure.ThickerPad
    if (leftMarkers) {
      view.x += Figure.MarkerPad
    }
    if (leftLabel != null) {
      view.x += Figure.LABEL_PAD
    }
    view.x += Figure.MINIMUM_PAD
    if (title != null) {
      view.y += Figure.TITLE_PAD
    }
    view.y += Figure.MINIMUM_PAD
    view.width = rectangle.width - view.x - Figure.MINIMUM_PAD
    var height: Int = 0
    height += 2 * Figure.ThickerPad
    if (bottomMarkers) {
      height += Figure.MarkerPad
    }
    if (bottomLabel != null) {
      height += Figure.LABEL_PAD
    }
    height += Figure.MINIMUM_PAD
    view.height = rectangle.height - view.y - height
  }

  def xScale(x: Double): Double = {
    view.x + view.width * (x - range.x1) / (range.x2 - range.x1)
  }

  def yScale(y: Double): Double = {
    view.y + view.height * (1.0 - (y - range.y1) / (range.y2 - range.y1))
  }

  def paint(g2d: Graphics2D, rect: Rectangle) {
    buildViewport(rect)
    range = null
    range = buildRange()
    g2d.setColor(StandardColorPalette.color(255))
    g2d.fill(rect)
    g2d.setBackground(StandardColorPalette.color(255))
    g2d.setColor(StandardColorPalette.color(0))
    if (title != null) {
      g2d.setFont(Figure.TITLE_FONT)
      val titleWidth: Double = g2d.getFontMetrics.getStringBounds(title, g2d).getWidth
      g2d.drawString(title, (rect.width - titleWidth).asInstanceOf[Int] / 2, Figure.TITLE_PAD)
    }
    buildLeftMarkers()
    g2d.setFont(Figure.MARKERS_FONT)
    g2d.drawLine(view.x - Figure.ThickerPad, view.y, view.x - Figure.ThickerPad, view.y + view.height)
    for (i <- 0 until leftMarkersPos.size) {
      {
        if (leftThicker) {
          g2d.drawLine(view.x - 2 * Figure.ThickerPad, view.y + leftMarkersPos(i).toInt, view.x - Figure.ThickerPad, view.y + leftMarkersPos(i).toInt)
        }
        if (leftMarkers) {
          val xx: Int = view.x - 3 * Figure.ThickerPad
          val yy: Int = (view.y + view.height - leftMarkersPos(i) + g2d.getFontMetrics.getStringBounds(leftMarkersMsg(i), g2d).getWidth / 2).toInt
          g2d.translate(xx, yy)
          g2d.rotate(-Math.PI / 2)
          g2d.drawString(leftMarkersMsg(i), 0, 0)
          g2d.rotate(Math.PI / 2)
          g2d.translate(-xx, -yy)
        }
      }
    }

    if (leftLabel != null) {
      g2d.setFont(Figure.LABELS_FONT)
      val ywidth: Double = g2d.getFontMetrics.getStringBounds(leftLabel, g2d).getWidth
      val xx: Int = view.x - 5 * Figure.ThickerPad - Figure.MarkerPad
      val yy: Int = ((rect.height + ywidth) / 2).asInstanceOf[Int]
      g2d.translate(xx, yy)
      g2d.rotate(-math.Pi / 2)
      g2d.drawString(leftLabel, 0, 0)
      g2d.rotate(math.Pi / 2)
      g2d.translate(-xx, -yy)
    }
    buildBottomMarkers()
    g2d.setFont(Figure.MARKERS_FONT)
    g2d.drawLine(view.x, view.y + view.height + Figure.ThickerPad, view.x + view.width, view.y + view.height + Figure.ThickerPad)
    for (i <- 0 until bottomMarkersPos.size) {
      {
        if (bottomThicker) {
          g2d.drawLine(
            view.x + bottomMarkersPos(i).toInt,
            view.y + view.height + Figure.ThickerPad,
            view.x + bottomMarkersPos(i).toInt,
            view.y + view.height + 2 * Figure.ThickerPad)
        }
        if (bottomMarkers) {
          g2d.drawString(
            bottomMarkersMsg(i),
            view.x + bottomMarkersPos(i).toInt - g2d.getFontMetrics.getStringBounds(bottomMarkersMsg(i), g2d).getWidth.toInt / 2,
            view.y + view.height + 2 * Figure.ThickerPad + Figure.MarkerPad)
        }
      }
    }
    if (bottomLabel != null) {
      g2d.setFont(Figure.LABELS_FONT)
      val xwidth: Double = g2d.getFontMetrics.getStringBounds(bottomLabel, g2d).getWidth
      g2d.drawString(bottomLabel, ((rect.width - xwidth) / 2).asInstanceOf[Int], view.y + view.height + 2 * Figure.ThickerPad + Figure.MarkerPad + Figure.LABEL_PAD)
    }
  }

  protected def buildNumericBottomMarkers() {
    bottomMarkersPos.clear()
    bottomMarkersMsg.clear()
    val xspots: Int = math.floor(view.width / thickerMinSpace).toInt
    val xspotwidth: Double = view.width / xspots
    for (i <- 0 to xspots) {
      bottomMarkersPos += i * xspotwidth
      bottomMarkersMsg += ("%." + range.properDecimalsX.toString + "f").format(range.x1 + range.width * i / xspots)
    }
  }

  protected def buildNumericLeftMarkers() {
    leftMarkersPos.clear()
    leftMarkersMsg.clear()
    val yspots: Int = math.floor(view.height / thickerMinSpace).toInt
    val yspotwidth: Double = view.height / yspots
    for (i <- 0 to yspots) {
      leftMarkersPos += i * yspotwidth
      leftMarkersMsg += ("%." + range.properDecimalsY + "f").format(range.y1 + range.height * i / yspots)
    }
  }

  protected def buildLeftMarkers() {
  }

  protected def buildBottomMarkers() {
  }

}