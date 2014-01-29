package rapaio.correlation

import rapaio.data.NumericFeature

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

/**
 *
 * Pearson product-moment correlation coefficient.
 * <p/>
 * See
 * http://en.wikipedia.org/wiki/Pearson_product-moment_correlation_coefficient
 * <p/>
 * User: <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a>
 */
class PearsonCorrelation {

  private final val names: Array[String] = null
  private final val vectors: Array[NumericFeature] = null
  private final val pearson: Array[Array[Double]] = null


  //  private def compute(x: Nothing, y: Nothing): Double = {
  //    val xMean: Double = new Nothing(x).getValue
  //    val yMean: Double = new Nothing(y).getValue
  //    var sum: Double = 0
  //    val len: Int = max(x.rowCount, y.rowCount)
  //    val sdp: Double = sqrt(new Nothing(x).getValue) * sqrt(new Nothing(y).getValue)
  //    var count: Double = 0
  //
  //    {
  //      var i: Int = 0
  //      while (i < len) {
  //        {
  //          if (x.isMissing(i) || y.isMissing(i)) {
  //            continue //todo: continue is not supported
  //          }
  //          sum += ((x.value(i) - xMean) * (y.value(i) - yMean))
  //          count += 1
  //        }
  //        ({
  //          i += 1;
  //          i - 1
  //        })
  //      }
  //    }
  //    return sum / (sdp * (count - 1))
  //  }
  //
  //  def getValues: Array[Array[Double]] = {
  //    return pearson
  //  }
  //
  //  def summary {
  //    if (vectors.length == 1) {
  //      summaryOne
  //      return
  //    }
  //    if (vectors.length == 2) {
  //      summaryTwo
  //      return
  //    }
  //    summaryMore
  //  }
  //
  //  private def summaryOne {
  //    val sb: StringBuilder = new StringBuilder
  //    sb.append(String.format("pearson[%s] - Pearson product-moment correlation coefficient\n", names(0)))
  //    sb.append("1\n")
  //    sb.append("pearson correlation is 1 for identical vectors")
  //    code(sb.toString)
  //  }
  //
  //  private def summaryTwo {
  //    val sb: StringBuilder = new StringBuilder
  //    sb.append(String.format("pearson[%s, %s] - Pearson product-moment correlation coefficient\n", names(0), names(1)))
  //    sb.append(String.format("%.6f", pearson(0)(1)))
  //    code(sb.toString)
  //  }
  //
  //  private def summaryMore {
  //    val sb: StringBuilder = new StringBuilder
  //    sb.append(String.format("pearson[%s] - Pearson product-moment correlation coefficient\n", Arrays.deepToString(names)))
  //    val table: Array[Array[String]] = new Array[Array[String]](vectors.length + 1, vectors.length + 1)
  //    table(0)(0) = ""
  //
  //    {
  //      var i: Int = 1
  //      while (i < vectors.length + 1) {
  //        {
  //          table(0)(i) = i + "."
  //          table(i)(0) = i + "." + names(i - 1)
  //
  //          {
  //            var j: Int = 1
  //            while (j < vectors.length + 1) {
  //              {
  //                table(i)(j) = String.format("%.2f", pearson(i - 1)(j - 1))
  //                if (i == j) {
  //                  table(i)(j) = "x"
  //                }
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
  //    val width: Int = getPrinter.getTextWidth
  //    var start: Int = 0
  //    var end: Int = start
  //    val ws: Array[Int] = new Array[Int](table(0).length)
  //
  //    {
  //      var i: Int = 0
  //      while (i < table.length) {
  //        {
  //
  //          {
  //            var j: Int = 0
  //            while (j < table(0).length) {
  //              {
  //                ws(i) = max(ws(i), table(i)(j).length)
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
  //    while (start < vectors.length + 1) {
  //      var w: Int = 0
  //      while ((end < (table(0).length - 1)) && ws(end + 1) + w + 1 < width) {
  //        w += ws(end + 1) + 1
  //        end += 1
  //      }
  //
  //      {
  //        var j: Int = 0
  //        while (j < table.length) {
  //          {
  //
  //            {
  //              var i: Int = start
  //              while (i <= end) {
  //                {
  //                  sb.append(String.format("%" + ws(i) + "s", table(i)(j))).append(" ")
  //                }
  //                ({
  //                  i += 1;
  //                  i - 1
  //                })
  //              }
  //            }
  //            sb.append("\n")
  //          }
  //          ({
  //            j += 1;
  //            j - 1
  //          })
  //        }
  //      }
  //      start = end + 1
  //    }
  //    code(sb.toString)
  //  }
  //
  //}
  //
  //object PearsonCorrelation {
  //  def PearsonRCorrelation: Nothing = {
  //    this.names = new Array[String](vectors.length)
  //
  //    {
  //      var i: Int = 0
  //      while (i < names.length) {
  //        {
  //          names(i) = "V" + i
  //        }
  //        ({
  //          i += 1;
  //          i - 1
  //        })
  //      }
  //    }
  //    this.vectors = vectors
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
}