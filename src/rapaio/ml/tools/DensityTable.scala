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

import rapaio.data.Frame
import java.util.List
import rapaio.core
import java.util

/**
 * @author <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a>
 */
object DensityTable {
  val NUMERIC_DEFAULT_LABELS: Array[String] = Array[String]("?", "less-equals", "greater")

  def apply(testLabels: Array[String], targetLabels: Array[String]): DensityTable = {
    val dt = new DensityTable
    dt.testLabels = testLabels
    dt.targetLabels = targetLabels
    dt.values = Array.fill(testLabels.length, targetLabels.length)(0.0)
    dt
  }

  def apply(df: Frame, testColName: String, targetColName: String): DensityTable = {
    apply(df, null, testColName, targetColName)
  }

  def apply(df: Frame, weights: List[Double], testColName: String, targetColName: String): DensityTable = {
    require(df.col(targetColName).isNominal, "Target feature must be nominal")
    require(df.col(testColName).isNominal, "Test feature must be nominal")

    val dt = apply(df.col(testColName).labels.dictionary, df.col(targetColName).labels.dictionary)
    for (i <- 0 until df.rowCount) {
      dt.update(df.indexes(i, testColName), df.indexes(i, targetColName), if (weights != null) weights.get(i) else 1.0)
    }
    dt
  }
}

/**
 * Tool for computing various statistics like entropy, infoGain using a density table.
 *
 * A density table is a table build from two features, called test feature and target feature.
 *
 * Test feature can be nominal or numeric, target feature is always nominal.
 * The density table has the same number of columns like target feature, withe the same
 * names, containing in the first position the missing label column. The number of rows is
 * given by the number of labels from the test feature (if nominal), otherwise there are either
 * given as parameter or 2 by default, for numeric features.
 *
 * Each cell of the density table is able to contain the sum of weights of the observations
 * of a given data set, which has the index of the target feature label equals with column number, and
 * index of test feature label equals row number.
 *
 * After construction, fill or update operations, one can use density table to compute various results
 * needed by some classification algorithms.
 */
final class DensityTable {

  private var testLabels: Array[String] = _
  private var targetLabels: Array[String] = _
  private var values: Array[Array[Double]] = _

  /**
   * Reset all values from density table with 0
   */
  def reset = values.foreach(row => util.Arrays.fill(row, 0.0))

  def update(row: Int, col: Int, weight: Double) = values(row)(col) += weight

  def moveOnRow(row1: Int, row2: Int, col: Int, weight: Double) {
    update(row1, col, -weight)
    update(row2, col, weight)
  }

  def getEntropy: Double = getEntropy(false)

  def getEntropy(useMissing: Boolean): Double = {
    var entropy: Double = 0
    val totals: Array[Double] = Array[Double](targetLabels.length)
    for (i <- 0 until testLabels.length)
      for (j <- 0 until targetLabels.length)
        totals(j) += values(i)(j)
    var total: Double = 0
    for (i <- 0 until totals.length)
      total += totals(i)
    for (i <- 0 until totals.length) {
      if (totals(i) > 0) {
        entropy += -core.log2(totals(i) / total) * totals(i) / total
      }
    }

    val factor: Double = if (useMissing) {
      var missing: Double = 0
      for (i <- 0 until targetLabels.length) {
        missing += values(0)(i)
      }
      total / (missing + total)
    } else 1
    factor * entropy
  }

  def getInfoXGain: Double = getInfoXGain(false)

  def getInfoXGain(useMissing: Boolean): Double = {
    val totals: Array[Double] = new Array[Double](testLabels.length)
    for (i <- 0 until testLabels.length) {
      for (j <- 0 until targetLabels.length) {
        totals(i) += values(i)(j)
      }
    }
    var total: Double = 0
    for (i <- 0 until totals.length) {
      total += totals(i)
    }
    var gain: Double = 0
    for (i <- 0 until testLabels.length) {
      for (j <- 0 until targetLabels.length) {
        if (values(i)(j) > 0) gain += -core.log2(values(i)(j) / totals(i)) * values(i)(j) / total
      }
    }
    val factor: Double = if (!useMissing) 1
    else {
      var missing: Double = 0
      for (i <- 0 until targetLabels.length) missing += values(0)(i)
      total / (missing + total)
    }
    factor * gain
  }

  def getInfoGain: Double = getInfoGain(false)

  def getInfoGain(useMissing: Boolean): Double = {
    return getEntropy(useMissing) - getInfoXGain(useMissing)
  }

  def splitInfo: Double = splitInfo(false)

  def splitInfo(useMissing: Boolean): Double = {
    val start: Int = if (useMissing) 0 else 1
    val totals: Array[Double] = new Array[Double](testLabels.length)
    for (i <- start until testLabels.length) {
      for (j <- 0 until targetLabels.length) {
        totals(i) += values(i)(j)
      }
    }

    var total: Double = 0
    for (i <- 0 until totals.length) {
      total += totals(i)
    }

    var splitInfo: Double = 0
    for (i <- 0 until totals.length) {
      if (totals(i) > 0) {
        splitInfo += -core.log2(totals(i) / total) * totals(i) / total
      }
    }
    splitInfo
  }

  def gainRatio: Double = gainRatio(false)

  def gainRatio(useMissing: Boolean): Double = getInfoGain(useMissing) / splitInfo(useMissing)

  /**
   * Computes the number of columns which have totals equal or greater than {@param minWeight} minWeight,
   * considering missing values only if {@param isMissing} is true.
   *
   * @param useMissing if missing row information is used
   * @return number of columns which meet criteria
   */
  def countWithMinimum(useMissing: Boolean, minWeight: Double): Int = {
    val start: Int = if (useMissing) 0 else 1
    val totals: Array[Double] = new Array[Double](testLabels.length)
    for (i <- start until testLabels.length) {
      for (j <- 0 until targetLabels.length) {
        totals(i) += values(i)(j)
      }
    }
    totals.count(x => x >= minWeight)
  }
}