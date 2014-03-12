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

package rapaio

import rapaio.graphics.plotc._
import rapaio.data.Feature
import rapaio.graphics.base._
import rapaio.core.distributions.Distribution
import rapaio.workspace.Workspace

/**
 * @author <a href="mailto:padreati@yahoo.com>Aurelian Tutuianu</a>
 */
package object graphics {

  private var lastFigure: Figure = null

  def plot(col: ColorOption = GraphicOptions.DefaultColor,
           pch: PchOption = GraphicOptions.DefaultPch,
           lwd: LwdOption = GraphicOptions.DefaultLwd,
           sz: SizeOption = GraphicOptions.DefaultSz,
           xLim: (Double, Double) = (Double.NaN, Double.NaN),
           yLim: (Double, Double) = (Double.NaN, Double.NaN),
           xLab: String = null,
           yLab: String = null) {

    val plot = new Plot()
    plot.options.col = col
    plot.options.pch = pch
    plot.options.lwd = lwd
    plot.options.sz = sz
    plot.options.xLim = xLim
    plot.options.yLim = yLim
    plot.leftLabel = yLab
    plot.bottomLabel = xLab

    lastFigure = plot
  }

  def histogram(x: Feature,
                bins: Int = 30,
                prob: Boolean = true,
                min: Double = Double.NaN,
                max: Double = Double.NaN,
                col: ColorOption = 7,
                xLab: String = null,
                yLab: String = null,
                main: String = null) {

    val p = preparePlot(main, xLab, yLab)

    val hist = new Histogram(x, bins, prob, min, max)
    hist.options.col = col
    hist.options.col = if (col != p.options.col) col else lastFigure.options.col

    p.add(hist)
  }

  def points(x: Feature = null,
             y: Feature,
             col: ColorOption = GraphicOptions.DefaultColor,
             pch: PchOption = GraphicOptions.DefaultPch,
             sz: SizeOption = GraphicOptions.DefaultSz,
             xLab: String = null,
             yLab: String = null,
             main: String = null) {

    val p = preparePlot(main, xLab, yLab)

    val points = new Points(x, y)
    points.options.col = parentCol(col)
    points.options.pch = parentPch(pch)
    points.options.sz = parentSz(sz)

    p.add(points)
  }

  def lines(x: Feature = null,
            y: Feature,
            col: ColorOption = GraphicOptions.DefaultColor,
            lwd: LwdOption = GraphicOptions.DefaultLwd,
            xLab: String = null,
            yLab: String = null,
            main: String = null) {

    val p = preparePlot(main, xLab, yLab)

    val lines = new Lines(x, y)
    lines.options.col = parentCol(col)
    lines.options.lwd = parentLwd(lwd)

    p.add(lines)
  }

  def function(f: Double => Double,
               points: Int = 1024,
               col: ColorOption = GraphicOptions.DefaultColor,
               lwd: LwdOption = GraphicOptions.DefaultLwd,
               main: String = null,
               xLab: String = null,
               yLab: String = null) = {

    val p = preparePlot(main, xLab, yLab)

    val fl = FunctionLine(f, points)
    fl.options.col = parentCol(col)
    fl.options.lwd = parentLwd(lwd)

    p.add(fl)
  }

  def density(x: Feature,
              main: String = null,
              xLab: String = null,
              yLab: String = null) {

    val p = preparePlot(main, xLab, yLab)
    val d = DensityLine(x)
    p.add(d)
  }

  def hl(x: Double,
         h: Boolean = true,
         lwd: LwdOption = GraphicOptions.DefaultLwd,
         col: ColorOption = GraphicOptions.DefaultColor,
         main: String = null,
         xLab: String = null,
         yLab: String = null) {

    val p = preparePlot(main, xLab, yLab)
    val line = new ABLine(0, x, true, false)
    line.options.col = parentCol(col)
    line.options.lwd = parentLwd(lwd)
    p.add(line)
  }

  def vl(x: Double,
         h: Boolean = true,
         lwd: LwdOption = GraphicOptions.DefaultLwd,
         col: ColorOption = GraphicOptions.DefaultColor,
         main: String = null,
         xLab: String = null,
         yLab: String = null) {

    val p = preparePlot(main, xLab, yLab)
    val line = new ABLine(x, 0, false, true)
    line.options.col = parentCol(col)
    line.options.lwd = parentLwd(lwd)
    p.add(line)
  }

  def abLine(a: Double, b: Double,
             lwd: LwdOption = GraphicOptions.DefaultLwd,
             col: ColorOption = GraphicOptions.DefaultColor,
             main: String = null,
             xLab: String = null,
             yLab: String = null) {

    val p = preparePlot(main, xLab, yLab)

    val line = new ABLine(a, b, false, false)
    line.options.col = parentCol(col)
    line.options.lwd = parentLwd(lwd)
    p.add(line)
  }


  def qqplot(feature: Feature,
             distribution: Distribution,
             xLab: String = null,
             yLab: String = null,
             main: String = null) {

    val qq = new QQPlot(feature, distribution)

    if (main != null) qq.title = main
    if (xLab != null) qq.bottomLabel = xLab
    if (yLab != null) qq.leftLabel = yLab

    lastFigure = qq
  }

  def boxplot(feature: Array[Feature],
              labels: Array[String]) {

    val bp = BoxPlot(feature, labels)
    lastFigure = bp
  }

  def draw(width: Int = -1, height: Int = -1) = Workspace.printer.draw(lastFigure, width, height)


  // utility methods 

  private def preparePlot(main: String, xLab: String, yLab: String): Plot = {
    if (lastFigure != null && lastFigure.isInstanceOf[Plot]) {
      lastFigure.asInstanceOf[Plot]
    } else {
      val plot = new Plot()
      if (xLab != null) plot.bottomLabel = xLab
      if (yLab != null) plot.leftLabel = yLab
      if (main != null) plot.title = main
      lastFigure = plot
      plot
    }
  }

  private def parentCol(col: ColorOption): ColorOption = {
    if (lastFigure.options.col != GraphicOptions.DefaultColor)
      lastFigure.options.col
    else col
  }

  private def parentLwd(lwd: LwdOption): LwdOption = {
    if (lastFigure.options.lwd != GraphicOptions.DefaultLwd)
      lastFigure.options.lwd
    else lwd
  }

  private def parentSz(sz: SizeOption): SizeOption = {
    if (lastFigure.options.sz != GraphicOptions.DefaultSz)
      lastFigure.options.sz
    else sz
  }

  private def parentPch(pch: PchOption): PchOption = {
    if (lastFigure.options.pch != GraphicOptions.DefaultPch)
      lastFigure.options.pch
    else
      pch
  }
}
