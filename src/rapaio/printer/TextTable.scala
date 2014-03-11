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

package rapaio.printer


/**
 * Utility class used to model text formatted tabular data.
 *
 * @author <a href="mailto:padreati@yahoo.com>Aurelian Tutuianu</a>
 */
class TextTable(rows: Int, cols: Int, header: Boolean = true, footer: Boolean = false) {

  val alignHeaders = Array.fill(cols)(true)
  val alignBody = Array.fill(cols)(false)
  val headers = new Array[String](cols)
  val data = Array.fill(rows, cols)("")
  val colGroups = (0 until cols).toArray

  def print(sb: StringBuilder, textWidth: Int) {
    val widths = Array.fill(cols)(0)
    for (i <- 0 until cols) {
      for (j <- 0 until rows) {
        widths(i) = math.max(widths(i), data(j)(i).length)
      }
      widths(i) = math.max(widths(i), headers(i).length)
    }

    def groupFrom(start: Int): Int = {
      if (start < cols - 1 && colGroups(start) == colGroups(start + 1)) groupFrom(start + 1)
      else start + 1
    }

    def groupCount(start: Int, end: Int): Int = {
      def groupCount(pos: Int, count: Int): Int = {
        if (pos < end) groupCount(pos + 1, count + widths(pos))
        else count
      }
      groupCount(start, 0)
    }

    var last = 0
    while (last < cols) {
      val start = last
      var end = start
      var count = 0
      var continue = true
      while (end < cols && continue) {
        val next = groupFrom(end)
        val countNext = groupCount(end, next)
        if (start == end || count + countNext <= textWidth) {
          end = next
          count += countNext
        } else {
          continue = false
        }
      }
      printCols(sb, (start, end), widths)
      last = end
    }
  }

  private def printCols(sb: StringBuilder, range: (Int, Int), widths: Array[Int]) {
    for (col <- range._1 until range._2) {
      if (alignHeaders(col)) {
        sb.append(" " * (widths(col) - headers(col).length))
        sb.append(headers(col))
      } else {
        sb.append(headers(col))
        sb.append(" " * (widths(col) - headers(col).length))
      }
      sb.append("  ")
    }
    sb.append("\n")
    for (row <- 0 until rows) {
      for (col <- range._1 until range._2) {
        if (alignBody(col)) {
          sb.append(" " * (widths(col) - data(row)(col).length))
          sb.append(data(row)(col))
        } else {
          sb.append(data(row)(col))
          sb.append(" " * (widths(col) - data(row)(col).length))
        }
        sb.append("  ")
      }
      sb.append("\n")
    }
    sb.append("\n")
  }
}

