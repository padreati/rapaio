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

import rapaio.core.stat.Quantiles
import rapaio.data.Feature
import rapaio.graphics.base.{Figure, Range}
import java.awt.{BasicStroke, Rectangle, Graphics2D}
import java.awt.geom.Line2D

/**
 * @author <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a>
 */
class BoxPlot(features: Array[Feature], labels: Array[String]) extends Figure {

  def initialize() {
    leftMarkers = true
    leftThicker = true
    bottomMarkers = true
    bottomThicker = true

    options.col = 0
  }

  def buildRange(): Range = {
    val range: Range = new Range
    range.union(0, Double.NaN)
    range.union(features.length, Double.NaN)
    features.foreach(f => f.values.foreach(x => if (!x.isNaN) range.union(Double.NaN, x)))
    range
  }

  override def buildLeftMarkers() {
    buildNumericLeftMarkers()
  }

  override def buildBottomMarkers() {
    bottomMarkersPos.clear()
    bottomMarkersMsg.clear()

    val xspotwidth: Double = view.width / features.length
    for (i <- 0 until features.length) {
      bottomMarkersPos += (i * xspotwidth + xspotwidth / 2)
      bottomMarkersMsg += labels(i)
    }
  }

  override def paint(g2d: Graphics2D, rect: Rectangle) {
    buildRange()
    super.paint(g2d, rect)
    for (i <- 0 until features.length) {
      val v = features(i)
      if (v.rowCount != 0) {
        val p = Array[Double](0.25, 0.5, 0.75)
        val q = Quantiles(v, p).values
        val iqr: Double = q(2) - q(0)
        val innerfence = 1.5 * iqr
        val outerfence = 3 * iqr
        val x1: Double = i + 0.5 - 0.3
        val x2: Double = i + 0.5
        val x3: Double = i + 0.5 + 0.3
        g2d.setColor(options.col(i))

        // median
        g2d.setStroke(new BasicStroke(options.lwd(0) * 2))
        g2d.draw(new Line2D.Double(xScale(x1), yScale(q(1)), xScale(x3), yScale(q(1))))

        // box
        g2d.setStroke(new BasicStroke(options.lwd(0)))
        g2d.draw(new Line2D.Double(xScale(x1), yScale(q(0)), xScale(x3), yScale(q(0))))
        g2d.draw(new Line2D.Double(xScale(x1), yScale(q(2)), xScale(x3), yScale(q(2))))
        g2d.draw(new Line2D.Double(xScale(x1), yScale(q(0)), xScale(x1), yScale(q(2))))
        g2d.draw(new Line2D.Double(xScale(x3), yScale(q(0)), xScale(x3), yScale(q(2))))

        // outliers
        var upperwhisker: Double = q(2)
        var lowerqhisker: Double = q(0)

        for (j <- 0 until v.rowCount) {
          val point = v.values(j)
          if ((point > q(2) + outerfence) || (point < q(0) - outerfence)) {
            //            val outlier: Nothing = null
            val width = (3 * options.sz(i)).asInstanceOf[Int]
            g2d.fillOval(xScale(x2).asInstanceOf[Int] - width / 2 - 1, yScale(point).asInstanceOf[Int] - width / 2 - 1, width, width)
          } else if ((point > q(2) + innerfence) || (point < q(0) - innerfence)) {
            // outlier
            val width: Int = (3.5 * options.sz(i)).asInstanceOf[Int]
            g2d.drawOval(xScale(x2).asInstanceOf[Int] - width / 2 - 1, yScale(point).asInstanceOf[Int] - width / 2 - 1, width, width)
          } else if ((point > upperwhisker) && (point < q(2) + innerfence)) {
            upperwhisker = math.max(upperwhisker, point)
          } else if ((point < lowerqhisker) && (point >= q(0) - innerfence)) {
            lowerqhisker = math.min(lowerqhisker, point)
          }
        }

        // whiskers
        g2d.draw(new Line2D.Double(xScale(x1), yScale(upperwhisker), xScale(x3), yScale(upperwhisker)))
        g2d.draw(new Line2D.Double(xScale(x1), yScale(lowerqhisker), xScale(x3), yScale(lowerqhisker)))
        g2d.setStroke(new BasicStroke(options.lwd(0), BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 1f, Array[Float](8), 0))
        g2d.draw(new Line2D.Double(xScale(x2), yScale(q(2)), xScale(x2), yScale(upperwhisker)))
        g2d.draw(new Line2D.Double(xScale(x2), yScale(q(0)), xScale(x2), yScale(lowerqhisker)))
      }
    }
  }

}

object BoxPlot {
  //  def this(numeric: Nothing, nominal: Nothing) {
  //    this()
  //    labels = nominal.getDictionary
  //    vectors = new Array[Nothing](labels.length)
  //    val count: Array[Int] = new Array[Int](labels.length) {
  //      var i: Int = 0
  //      while (i < numeric.getRowCount) {
  //        {
  //          count(nominal.getIndex(i)) += 1
  //        }
  //        ({
  //          i += 1;
  //          i - 1
  //        })
  //      }
  //    }
  //    {
  //      var i: Int = 0
  //      while (i < count.length) {
  //        {
  //          vectors(i) = new Nothing(count(i))
  //        }
  //        ({
  //          i += 1;
  //          i - 1
  //        })
  //      }
  //    }
  //    val pos: Array[Int] = new Array[Int](vectors.length) {
  //      var i: Int = 0
  //      while (i < nominal.getRowCount) {
  //        {
  //          vectors(nominal.getIndex(i)).setValue(pos(nominal.getIndex(i)), numeric.getValue(i))
  //          pos(nominal.getIndex(i)) += 1
  //        }
  //        ({
  //          i += 1;
  //          i - 1
  //        })
  //      }
  //    }
  //    initialize
  //  }
  //

  def apply(vectors: Array[Feature], labels: Array[String]): BoxPlot = {
    val bp = new BoxPlot(vectors, labels)
    bp.initialize()
    bp
  }

  //
  //  def this(df: Nothing, colRange: Nothing) {
  //    this()
  //    if (colRange == null) {
  //      var len: Int = 0 {
  //        var i: Int = 0
  //        while (i < df.getColCount) {
  //          {
  //            if (df.getCol(i).getType.isNumeric) {
  //              len += 1
  //            }
  //          }
  //          ({
  //            i += 1;
  //            i - 1
  //          })
  //        }
  //      }
  //      val indexes: Array[Int] = new Array[Int](len)
  //      len = 0 {
  //        var i: Int = 0
  //        while (i < df.getColCount) {
  //          {
  //            if (df.getCol(i).getType.isNumeric) {
  //              indexes(({
  //                len += 1;
  //                len - 1
  //              })) = i
  //            }
  //          }
  //          ({
  //            i += 1;
  //            i - 1
  //          })
  //        }
  //      }
  //      colRange = new Nothing(indexes)
  //    }
  //    val indexes: Nothing = colRange.parseColumnIndexes(df)
  //    vectors = new Array[Nothing](indexes.size)
  //    labels = new Array[String](indexes.size)
  //    var pos: Int = 0
  //    import scala.collection.JavaConversions._
  //    for (index <- indexes) {
  //      vectors(pos) = df.getCol(index)
  //      labels(pos) = df.getColNames(index)
  //      pos += 1
  //    }
  //    initialize
  //  }

}