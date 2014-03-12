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

package rapaio.core.correlation

import rapaio.core.stat.Mean
import rapaio.core.stat.Variance
import rapaio.data.Feature
import rapaio.printer.Printable
import rapaio.workspace.Workspace

/**
 *
 * Pearson product-moment correlation coefficient.
 * <p/>
 * See
 * http://en.wikipedia.org/wiki/Pearson_product-moment_correlation_coefficient
 * <p/>
 * User: <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a>
 */

class PearsonRCorrelation(names: Array[String], vectors: Array[Feature]) extends Printable {
  private val pearson = Array.fill(vectors.length, vectors.length)(0.0)

  private def compute(x: Feature, y: Feature): Double = {
    val xMean = Mean(x).value
    val yMean = Mean(y).value
    var sum = 0.0
    val len = math.max(x.rowCount, y.rowCount)
    val sdp: Double = math.sqrt(Variance(x).value) * math.sqrt(Variance(y).value)
    var count: Double = 0

    for (i <- 0 until len) {
      if (!x.missing(i) && !y.missing(i)) {
        sum += ((x.values(i) - xMean) * (y.values(i) - yMean))
        count += 1
      }
    }
    sum / (sdp * (count - 1))
  }

  def values: Array[Array[Double]] = pearson


  override def buildSummary(sb: StringBuilder): Unit = {
    if (vectors.length == 1) {
      summaryOne(sb)
    } else if (vectors.length == 2) {
      summaryTwo(sb)
    } else {
      summaryMore(sb)
    }
  }

  private def summaryOne(sb: StringBuilder) {
    sb.append(String.format("pearson[%s] - Pearson product-moment correlation coefficient\n", names(0)))
    sb.append("1\n")
    sb.append("pearson correlation is 1 for identical vectors")
  }

  private def summaryTwo(sb: StringBuilder) {
    sb.append(f"pearson[${names(0)}%s, ${names(1)}%s] - Pearson product-moment correlation coefficient\n")
    sb.append(f"${pearson(0)(1)}%.6f")
  }

  private def summaryMore(sb: StringBuilder) {
    sb.append(f"pearson[${names mkString ","}%s] - Pearson product-moment correlation coefficient\n")
    val table = Array.fill(vectors.length + 1, vectors.length + 1)("")

    for (i <- 1 until vectors.length + 1) {
      table(0)(i) = i + "."
      table(i)(0) = i + "." + names(i - 1)
      for (j <- 1 until vectors.length + 1) {
        table(i)(j) = f"${pearson(i - 1)(j - 1)}%.2f"
        if (i == j) {
          table(i)(j) = "x"
        }
      }
    }
    val width: Int = Workspace.printer.textWidth
    var start: Int = 0
    var end: Int = start
    val ws: Array[Int] = new Array[Int](table(0).length)
    for (i <- 0 until table.length) {
      for (j <- 0 until table(0).length) {
        ws(i) = math.max(ws(i), table(i)(j).length)
      }
    }
    while (start < vectors.length + 1) {
      var w: Int = 0
      while ((end < (table(0).length - 1)) && ws(end + 1) + w + 1 < width) {
        w += ws(end + 1) + 1
        end += 1
      }
      for (j <- 0 until table.length) {
        for (i <- start to end) {
          sb.append(String.format("%" + ws(i) + "s", table(i)(j))).append(" ")
        }
        sb.append("\n")
      }
      start = end + 1
    }
  }
}

object PearsonRCorrelation {
  //  def this(df: Frame) {
  //    this()
  //    this.names = df.getColNames
  //    this.vectors = new Array[Nothing](df.getColCount)
  //
  //    var i: Int = 0
  //    while (i < df.getColCount) {
  //      {
  //        vectors(i) = df.getCol(i)
  //      }
  //      ({
  //        i += 1;
  //        i - 1
  //      })
  //    }
  //    this.pearson = new Array[Array[Double]](vectors.length, vectors.length)
  //
  //    {
  //      var i: Int = 0
  //      while (i < vectors.length) {
  //        {
  //          pearson(i)(i) = 1
  //
  //          {
  //            var j: Int = i + 1
  //            while (j < vectors.length) {
  //              {
  //                pearson(i)(j) = compute(vectors(i), vectors(j))
  //                pearson(j)(i) = pearson(i)(j)
  //              }
  //              ({
  //                j += 1;
  //                j - 1
  //              })
  //            }
  //          }
  //        }
  //        ({
  //          i += 1;
  //          i - 1
  //        })
  //      }
  //    }
  //  }

  def apply(vectors: Array[Feature]): PearsonRCorrelation = {
    val names = new Array[String](vectors.length)
    for (i <- 1 to names.length) {
      names(i) = "V" + i
    }
    apply(vectors, names)
  }

  def apply(vectors: Array[Feature], names: Array[String]): PearsonRCorrelation = {
    val corr = new PearsonRCorrelation(names, vectors)

    for (i <- 0 until vectors.length) {
      corr.pearson(i)(i) = 1
      for (j <- i + 1 until vectors.length) {
        corr.pearson(i)(j) = corr.compute(vectors(i), vectors(j))
        corr.pearson(j)(i) = corr.pearson(i)(j)
      }
    }
    corr
  }

}