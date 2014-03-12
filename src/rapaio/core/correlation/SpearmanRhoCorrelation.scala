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

//import rapaio.core.Summarizable
//import rapaio.data.Frame
//import rapaio.data.Numeric
//import rapaio.data.Vector
//import java.util.Arrays
//import rapaio.core.MathBase.max
//import rapaio.data.filters.BaseFilters.sort
//import rapaio.workspace.Workspace.code
//import rapaio.workspace.Workspace.getPrinter

/**
 * Spearman's rank correlation coefficient.
 * <p/>
 * You can compute coefficient for multiple vectors at the same time.
 * <p/>
 * See: http://en.wikipedia.org/wiki/Spearman%27s_rank_correlation_coefficient
 * <p/>
 * User: <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a>
 */
@Deprecated
class SpearmanRhoCorrelation

//extends Summarizable {
//  def this(vectors: Nothing*) {
//    this()
//    this.names = new Array[String](vectors.length)
//    {
//      var i: Int = 0
//      while (i < names.length) {
//        {
//          names(i) = "V" + i
//        }
//        ({
//          i += 1; i - 1
//        })
//      }
//    }
//    this.vectors = vectors
//    this.rho = compute
//  }
//
//  def this(df: Frame) {
//    this()
//    this.names = df.getColNames
//    this.vectors = new Array[Nothing](df.getColCount)
//    {
//      var i: Int = 0
//      while (i < df.getColCount) {
//        {
//          vectors(i) = df.getCol(i)
//        }
//        ({
//          i += 1; i - 1
//        })
//      }
//    }
//    this.rho = compute
//  }
//
//  private def compute: Array[Array[Double]] = {
//    val sorted: Array[Nothing] = new Array[Nothing](vectors.length)
//    val ranks: Array[Nothing] = new Array[Nothing](vectors.length)
//    {
//      var i: Int = 0
//      while (i < sorted.length) {
//        {
//          sorted(i) = sort(vectors(i))
//          ranks(i) = new Nothing(vectors(i).getRowCount)
//        }
//        ({
//          i += 1; i - 1
//        })
//      }
//    }
//    {
//      var i: Int = 0
//      while (i < sorted.length) {
//        {
//          var start: Int = 0
//          while (start < sorted(i).getRowCount) {
//            var end: Int = start
//            while (end < sorted(i).getRowCount - 1 && sorted(i).getValue(end) eq sorted(i).getValue(end + 1)) {
//              end += 1
//            }
//            val value: Double = 1 + (start + end) / 2.
//            {
//              var j: Int = start
//              while (j <= end) {
//                {
//                  ranks(i).setValue(sorted(i).getRowId(j), value)
//                }
//                ({
//                  j += 1; j - 1
//                })
//              }
//            }
//            start = end + 1
//          }
//        }
//        ({
//          i += 1; i - 1
//        })
//      }
//    }
//    return new PearsonRCorrelation(ranks).getValues
//  }
//
//  def getValues: Array[Array[Double]] = {
//    return rho
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
//    sb.append(String.format("spearman[%s] - Spearman's rank correlation coefficient\n", names(0)))
//    sb.append("1\n")
//    sb.append("spearman's rank correlation is 1 for identical vectors")
//    code(sb.toString)
//  }
//
//  private def summaryTwo {
//    val sb: StringBuilder = new StringBuilder
//    sb.append(String.format("spearman[%s, %s] - Spearman's rank correlation coefficient\n", names(0), names(1)))
//    sb.append(String.format("%.6f", rho(0)(1)))
//    code(sb.toString)
//  }
//
//  private def summaryMore {
//    val sb: StringBuilder = new StringBuilder
//    sb.append(String.format("spearman[%s] - Spearman's rank correlation coefficient\n", Arrays.deepToString(names)))
//    val table: Array[Array[String]] = new Array[Array[String]](vectors.length + 1, vectors.length + 1)
//    table(0)(0) = ""
//    {
//      var i: Int = 1
//      while (i < vectors.length + 1) {
//        {
//          table(0)(i) = i + "."
//          table(i)(0) = i + "." + names(i - 1)
//          {
//            var j: Int = 1
//            while (j < vectors.length + 1) {
//              {
//                table(i)(j) = String.format("%.6f", rho(i - 1)(j - 1))
//                if (i == j) {
//                  table(i)(j) = "x"
//                }
//              }
//              ({
//                j += 1; j - 1
//              })
//            }
//          }
//        }
//        ({
//          i += 1; i - 1
//        })
//      }
//    }
//    val width: Int = getPrinter.getTextWidth
//    var start: Int = 0
//    var end: Int = start
//    val ws: Array[Int] = new Array[Int](table(0).length)
//    {
//      var i: Int = 0
//      while (i < table.length) {
//        {
//          {
//            var j: Int = 0
//            while (j < table(0).length) {
//              {
//                ws(i) = max(ws(i), table(i)(j).length)
//              }
//              ({
//                j += 1; j - 1
//              })
//            }
//          }
//        }
//        ({
//          i += 1; i - 1
//        })
//      }
//    }
//    while (start < vectors.length + 1) {
//      var w: Int = 0
//      while ((end < (table(0).length - 1)) && ws(end + 1) + w + 1 < width) {
//        w += ws(end + 1) + 1
//        end += 1
//      }
//      {
//        var j: Int = 0
//        while (j < table.length) {
//          {
//            {
//              var i: Int = start
//              while (i <= end) {
//                {
//                  sb.append(String.format("%" + ws(i) + "s", table(i)(j))).append(" ")
//                }
//                ({
//                  i += 1; i - 1
//                })
//              }
//            }
//            sb.append("\n")
//          }
//          ({
//            j += 1; j - 1
//          })
//        }
//      }
//      start = end + 1
//    }
//    code(sb.toString)
//  }
//
//  private final val names: Array[String] = null
//  private final val vectors: Array[Nothing] = null
//  private final val rho: Array[Array[Double]] = null
//}