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

package rapaio.data

import rapaio.core.stat.{Quantiles, Mean, DensityVector}

/**
 * @author <a href="mailto:padreati@yahoo.com>Aurelian Tutuianu</a>
 */
object DataSummary {

  def featureSummary(header: String, feature: Feature): Array[String] = {
    feature.typeName match {
      case "nom" => featureSummaryNom(header, feature)
      case _ => featureSummaryVal(header, feature)
    }
  }

  def featureSummaryVal(s: String, feature: Feature): Array[String] = {
    val summary = Array.fill(7)(" ")

    val q = new Quantiles(feature, Array[Double](0.0, 0.25, 0.50, 0.75, 1.00)).values

    summary(0) = s
    summary(1) = f"   Min. : ${q(0)}%.6f"
    summary(2) = f"1st Qu. : ${q(1)}%.6f"
    summary(3) = f" Median : ${q(2)}%.6f"
    summary(4) = f"   Mean : ${Mean(feature).value}%.6f"
    summary(5) = f"2nd Qu. : ${q(3)}%.6f"
    summary(6) = f"   Max. : ${q(4)}%.6f"
    summary
  }


  private def featureSummaryNom(header: String, feature: Feature): Array[String] = {
    val summary = Array.fill(7)("")

    val dv = DensityVector(feature)
    var indexes: Array[Int] = (1 until feature.labels.dictionary.length).toArray
    indexes = indexes.sortWith((i, j) => dv.values(i) < dv.values(j))

    val naCount = feature.labels.filter(labels => labels != Nominal.MissingValue).count(_ => true)
    summary(0) = header
    var max: Int = naCount.toString.length
    for (i <- 0 until 5) {
      max = math.max(max, dv.values(indexes(0)).toInt.toString.length)
    }
    for (i <- 0 until math.min(5, indexes.length)) {
      val count = dv.values(indexes(i)).toInt
      val len = count.toString.length
      summary(i + 1) = feature.labels.dictionary(indexes(i)) + " : " + (" " * (max - len)) + count
    }
    summary(6) = "NAs : " + (" " * (max - naCount.toString.length)) + naCount
    summary
  }
}
