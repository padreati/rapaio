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

package rapaio.ml.tools

import rapaio.data.{Value, Feature}
import rapaio.printer.Summarizable
import rapaio.workspace.Workspace
import rapaio.core.RandomSource
import scala.annotation.tailrec

/**
 * @author <a href="mailto:padreati@yahoo.com>Aurelian Tutuianu</a>
 */
class DensityVector extends Summarizable {

  private var targetLabels: Array[String] = _
  private var values: Array[Double] = _

  def mode(): (Int, String) = {
    @tailrec
    def modeNext(i: Int, n: Double, max: Double, lastIndex: Int, lastLabel: String): (Int, String) = {
      if (i >= values.length) {
        (lastIndex, lastLabel)
      }
      else if (values(i) < max) {
        modeNext(i + 1, n, max, lastIndex, lastLabel)
      }
      else if (values(i) == max) {
        if (RandomSource.next >= n / (n + 1)) {
          modeNext(i + 1, 1, values(i), i, targetLabels(i))
        } else {
          modeNext(i + 1, n + 1, max, lastIndex, lastLabel)
        }
      } else {
        modeNext(i + 1, 1, values(i), i, targetLabels(i))
      }
    }
    modeNext(1, 1, -1, -1, "")
  }

  override def summary(): Unit = {
    val total = values.sum

    // collect widths
    val widths = new Array[Int](4)
    widths(0) = values.length.toString.size
    widths(1) = "label".length
    widths(2) = "density".length
    widths(3) = "percent".length
    for (i <- 0 until targetLabels.length) {
      widths(1) = math.max(widths(1), targetLabels(i).length)
      widths(2) = math.max(widths(2), "%.6f ".format(values(i)).toString.length)
      widths(3) = math.max(widths(2), "%.6f ".format(values(i) / total).toString.length)
    }

    val sb = new StringBuilder()
    sb ++= "> density vector:\n"
    sb ++= ("%" + widths(0) + "s   ").format("")
    sb ++= ("%" + widths(1) + "s ").format("label")
    sb ++= ("%" + widths(2) + "s ").format("density")
    sb ++= ("%" + widths(3) + "s ").format("percent")
    sb ++= "\n"
    for (i <- 0 until targetLabels.length) {
      sb ++= ("[%" + widths(0) + "d] ").format(i)
      sb ++= ("%-" + widths(1) + "s ").format(targetLabels(i))
      sb ++= ("%" + widths(2) + ".6f ").format(values(i))
      sb ++= ("%" + widths(3) + ".6f").format(values(i) / total)
      sb ++= "\n"
    }
    Workspace.code(sb.toString())
  }
}

object DensityVector {

  def apply(feature: Feature): DensityVector = apply(feature, null)

  def apply(feature: Feature, weights: Value): DensityVector = {
    require(feature.isNominal, "Can build density vectors only for nominal features.")
    require(weights == null || feature.rowCount == weights.rowCount, "target feature and weight must have the same size")

    def weight(i: Int): Double = if (weights != null) weights.values(i) else 1.0

    val dv = apply(feature.labels.dictionary)
    for (i <- 0 until feature.rowCount) dv.values(feature.indexes(i)) += weight(i)
    dv
  }

  def apply(labels: Array[String]): DensityVector = {
    require(labels.length > 0, "Can't make a density vector with no labels")
    require(labels(0) == "?", "First label must be nominal missing string '?'")

    val dv = new DensityVector
    dv.targetLabels = new Array[String](labels.length)
    dv.values = new Array[Double](labels.length)
    labels.copyToArray(dv.targetLabels)
    dv
  }
}