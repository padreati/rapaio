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
import rapaio.graphics.colors.ColorPalette
import scala.collection.mutable

/**
 * @author tutuianu
 */
object AbstractFigure {
  protected final val TITLE_FONT: Font = new Font("Verdana", Font.BOLD, 18)
  protected final val MARKERS_FONT: Font = new Font("Verdana", Font.PLAIN, 13)
  protected final val LABELS_FONT: Font = new Font("Verdana", Font.BOLD, 16)
  protected final val DEFAULT_THICKER_MIN_SPACE: Double = 50.
  protected final val THICKER_PAD: Int = 7
  protected final val MARKER_PAD: Int = 15
  protected final val LABEL_PAD: Int = 30
  protected final val TITLE_PAD: Int = 40
  protected final val MINIMUM_PAD: Int = 20
}

abstract class AbstractFigure extends Figure {

  private var parent: AbstractFigure = null
  private var viewport: Rectangle = null
  private var leftThicker: Boolean = false
  private var bottomThicker: Boolean = false
  private var leftMarkers: Boolean = false
  private var bottomMarkers: Boolean = false
  private var range: Range = null
  private final val bottomMarkersMsg = new mutable.MutableList[String]
  private final val bottomMarkersPos = new mutable.MutableList[Double]
  private final val leftMarkersMsg = new mutable.MutableList[String]
  private final val leftMarkersPos = new mutable.MutableList[Double]
  private var title: String = null
  private var leftLabel: String = null
  private var bottomLabel: String = null
  private var thickerMinSpace: Double = AbstractFigure.DEFAULT_THICKER_MIN_SPACE
  private var lwd: Float = 1.2f
  private var sizeIndex: Vector = Value(Array(2.5))
  private var colorIndex: Vector = Index(Array(0))
  private var pchIndex: Vector = Index(Array(0))
  private var x1: Double = Double.NaN
  private var x2: Double = Double.NaN
  private var y1: Double = Double.NaN
  private var y2: Double = Double.NaN

  def getParent: AbstractFigure = parent

  def getBottomMarkersMsg: mutable.MutableList[String] = bottomMarkersMsg

  def getBottomMarkersPos: mutable.MutableList[Double] = bottomMarkersPos

  def getLeftMarkersMsg: mutable.MutableList[String] = leftMarkersMsg

  def getLeftMarkersPos: mutable.MutableList[Double] = leftMarkersPos

  def setParent(parent: AbstractFigure): Unit = this.parent = parent

  def getViewport: Rectangle = viewport

  def isLeftThicker: Boolean = leftThicker

  def setLeftThicker(leftThicker: Boolean): AbstractFigure = {
    this.leftThicker = leftThicker
    return this
  }

  def isBottomThicker: Boolean = bottomThicker

  def setBottomThicker(bottomThicker: Boolean): AbstractFigure = {
    this.bottomThicker = bottomThicker
    return this
  }

  def isLeftMarkers: Boolean = leftMarkers

  def setLeftMarkers(leftMarkers: Boolean): AbstractFigure = {
    this.leftMarkers = leftMarkers
    return this
  }

  def isBottomMarkers: Boolean = bottomMarkers

  def setBottomMarkers(bottomMarkers: Boolean): AbstractFigure = {
    this.bottomMarkers = bottomMarkers
    return this
  }

  def getThickerMinSpace: Double = {
    if (parent != null && thickerMinSpace == AbstractFigure.DEFAULT_THICKER_MIN_SPACE) {
      return parent.getThickerMinSpace
    }
    return thickerMinSpace
  }

  def setThickerMinSpace(minSpace: Double): AbstractFigure = {
    thickerMinSpace = minSpace
    return this
  }

  def buildRange: Range

  def getRange: Range = {
    if (range == null) {
      range = buildRange
    }
    return range
  }

  def getTitle: String = title

  def setTitle(title: String): AbstractFigure = {
    this.title = title
    return this
  }

  def getLeftLabel: String = leftLabel

  def setLeftLabel(leftLabel: String): AbstractFigure = {
    this.leftLabel = leftLabel
    return this
  }

  def getBottomLabel: String = bottomLabel

  def setBottomLabel(bottomLabel: String): AbstractFigure = {
    this.bottomLabel = bottomLabel
    return this
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
    return viewport.x + viewport.width * (x - range.getX1) / (range.getX2 - range.getX1)
  }

  def yscale(y: Double): Double = {
    return viewport.y + viewport.height * (1.-(y - range.getY1) / (range.getY2 - range.getY1))
  }

  private def isDefaultLwd: Boolean = lwd == 1.2

  def getLwd: Float = {
    if (parent != null && isDefaultLwd) {
      return parent.getLwd
    }
    return lwd
  }

  def setLwd(lwd: Float): AbstractFigure = {
    this.lwd = lwd
    return this
  }

  private def isDefaultSize: Boolean = sizeIndex.getRowCount == 1 && sizeIndex.getValue(0) == 2.5

  def getSizeIndex: Vector = {
    if (parent != null && isDefaultSize) {
      return parent.getSizeIndex
    }
    return sizeIndex
  }

  def setSizeIndex(sizeIndex: Vector): AbstractFigure = {
    this.sizeIndex = sizeIndex
    return this
  }

  def setSizeIndex(size: Double): AbstractFigure = {
    this.sizeIndex = Value(Array(size))
    return this
  }

  def getSize(row: Int): Double = {
    val index = getSizeIndex
    var pos = row
    if (pos >= index.getRowCount) {
      pos %= index.getRowCount
    }
    index.getValue(pos)
  }

  private def isDefaultColorIndex: Boolean = {
    return colorIndex.getRowCount == 1 && colorIndex.getIndex(0) == 0
  }

  def getColorIndex: Vector = {
    if (parent != null && isDefaultColorIndex) {
      return parent.getColorIndex
    }
    return colorIndex
  }

  def setColorIndex(colorIndex: Vector): AbstractFigure = {
    this.colorIndex = colorIndex
    return this
  }

  def setColorIndex(colorIndex: Int): AbstractFigure = {
    this.colorIndex = Index(Array(colorIndex))
    return this
  }

  def getColor(row: Int): Color = {
    if (parent != null && isDefaultColorIndex) {
      return parent.getColor(row)
    }
    val index: Vector = getColorIndex
    var pos = row
    if (pos >= index.getRowCount) {
      pos %= index.getRowCount
    }
    return ColorPalette.STANDARD.getColor(index.getIndex(pos))
  }

  private def isDefaultPchIndex: Boolean = {
    return pchIndex.getIndex(0) == 0 && pchIndex.getRowCount == 1
  }

  def getPchIndex: Vector = {
    if (parent != null && isDefaultPchIndex) {
      return parent.getPchIndex
    }
    return pchIndex
  }

  def setPchIndex(pchIndex: Vector): AbstractFigure = {
    this.pchIndex = pchIndex
    return this
  }

  def setPchIndex(pch: Int): AbstractFigure = {
    this.pchIndex = Index(Array(pch))
    return this
  }

  def getPch(row: Int): Int = {
    val index: Vector = getPchIndex
    var pos = row
    if (pos >= index.getRowCount) {
      pos %= index.getRowCount
    }
    return index.getIndex(pos)
  }

  def getXRangeStart: Double = {
    if (parent != null && x1 != x1) {
      return parent.getXRangeStart
    }
    return x1
  }

  def getXRangeEnd: Double = {
    if (parent != null && x2 != x2) {
      return parent.getXRangeEnd
    }
    return x2
  }

  def setXRange(start: Double, end: Double): AbstractFigure = {
    this.x1 = start
    this.x2 = end
    return this
  }

  def getYRangeStart: Double = {
    if (parent != null && y1 != y1) {
      return parent.getYRangeStart
    }
    return y1
  }

  def getYRangeEnd: Double = {
    if (parent != null && y2 != y2) {
      return parent.getYRangeEnd
    }
    return y2
  }

  def setYRange(start: Double, end: Double): AbstractFigure = {
    this.y1 = start
    this.y2 = end
    return this
  }

  def paint(g2d: Graphics2D, rect: Rectangle) {
    buildViewport(rect)
    range = buildRange
    g2d.setColor(ColorPalette.STANDARD.getColor(255))
    g2d.fill(rect)
    g2d.setBackground(ColorPalette.STANDARD.getColor(255))
    g2d.setColor(ColorPalette.STANDARD.getColor(0))
    if (title != null) {
      g2d.setFont(AbstractFigure.TITLE_FONT)
      val titleWidth: Double = g2d.getFontMetrics.getStringBounds(title, g2d).getWidth
      g2d.drawString(title, (rect.width - titleWidth).asInstanceOf[Int] / 2, AbstractFigure.TITLE_PAD)
    }
    buildLeftMarkers
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
    buildBottomMarkers
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

  protected def buildNumericBottomMarkers {
    bottomMarkersPos.clear
    bottomMarkersMsg.clear
    val xspots: Int = Math.floor(viewport.width / getThickerMinSpace).toInt
    val xspotwidth: Double = viewport.width / xspots
    for (i <- 0 to xspots) {
      bottomMarkersPos += i * xspotwidth
      bottomMarkersMsg += ("%." + range.getProperDecimalsX.toString + "f").format(range.getX1 + range.getWidth * i / xspots)
    }
  }

  protected def buildNumericLeftMarkers {
    leftMarkersPos.clear
    leftMarkersMsg.clear
    val yspots: Int = Math.floor(viewport.height / getThickerMinSpace).toInt
    val yspotwidth: Double = viewport.height / yspots
    for (i <- 0 to yspots) {
      leftMarkersPos += i * yspotwidth
      leftMarkersMsg += ("%." + range.getProperDecimalsY + "f").format(range.getY1 + range.getHeight * i / yspots)
    }
  }

  def buildLeftMarkers {
  }

  def buildBottomMarkers {
  }

}