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

package rapaio.core.stat

import rapaio.data.{Value, Frame}
import rapaio.core.SpecialMath._
import java.util


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
final class DensityMatrix {

  private var testLabels: Array[String] = _
  private var targetLabels: Array[String] = _
  private var values: Array[Array[Double]] = _

  /**
   * Reset all values from density table with 0
   */
  def reset() = values.foreach(row => util.Arrays.fill(row, 0.0))

  /**
   * Adds to density table cell with row and col the given weight.
   *
   * @param row row of the cell
   * @param col column of the cell
   * @param weight weight which wil be added
   */
  def update(row: Int, col: Int, weight: Double) = values(row)(col) += weight

  /**
   * Moves a weight from row1 to row2 on given column. This is implemented
   * as to updates, one which adds -weight on row1, and second which add weight on row2.
   *
   * @param row1 initial row
   * @param row2 final row
   * @param col column of the cells
   * @param weight amount for update
   */
  def moveOnRow(row1: Int, row2: Int, col: Int, weight: Double) {
    update(row1, col, -weight)
    update(row2, col, weight)
  }

  /**
   * Computes accuracy on a density matrix for the case when a source
   * data set is split in two parts: first which have index equals with
   * given row on test columns and the rest of the frame
   *
   * @param row
   * @return accuracy computed for a binary split
   */
  def binaryAccuracy(row: Int): Double = {
    var left = 0.0
    var right = 0.0
    var leftMax = 0.0
    var rightMax = 0.0
    val totals = new Array[Double](targetLabels.length)
    for (i <- 0 until targetLabels.length; j <- 0 until testLabels.length) {
      if (j == row) {
        leftMax = math.max(leftMax, values(j)(i))
        left += values(j)(i)
      } else {
        totals(i) += values(j)(i)
      }
    }
    for (i <- 0 until targetLabels.length) {
      right += totals(i)
      rightMax = math.max(rightMax, totals(i))
    }

    def prob(part: Double, total: Double): Double = if (total == 0 || part == 0) 0 else part / total

    prob(leftMax + rightMax, left + right)
  }

  /**
   * Computes entropy on target feature only, without considering missing values.
   *
   * $\sum{x}$
   * @return computed entropy without considering missing values
   */
  def entropy(): Double = entropy(useMissing = false)

  /**
   * Computes entropy o target feature only with the possibility
   * to involve missing values.
   *
   * @param useMissing if true, missing values are considered an additional
   *
   * @return
   */
  def entropy(useMissing: Boolean): Double = {
    val totals: Array[Double] = Array[Double](targetLabels.length)
    for (i <- 1 until testLabels.length)
      for (j <- 1 until targetLabels.length)
        totals(j) += values(i)(j)
    val total: Double = totals.slice(1, targetLabels.length).sum
    val entropy: Double = totals.slice(1, totals.length).
      filter(x => x > 0).foldLeft(0.0)((b, a) => b - log2(a / total) * a / total)

    val factor: Double =
      if (useMissing) total / (values(0).slice(1, targetLabels.length).sum + total)
      else 1

    factor * entropy
  }

  def infoXGain: Double = infoXGain(useMissing = false)

  def infoXGain(useMissing: Boolean): Double = {
    val totals: Array[Double] = new Array[Double](testLabels.length)
    for (i <- 1 until testLabels.length) {
      for (j <- 1 until targetLabels.length) {
        totals(i) += values(i)(j)
      }
    }
    val total: Double = totals.slice(1, totals.length).sum
    var gain: Double = 0
    for (i <- 1 until testLabels.length) {
      for (j <- 1 until targetLabels.length) {
        if (values(i)(j) > 0) gain += -log2(values(i)(j) / totals(i)) * values(i)(j) / total
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

  def infoGain: Double = infoGain(useMissing = false)

  def infoGain(useMissing: Boolean): Double = entropy(useMissing) - infoXGain(useMissing)

  def splitInfo: Double = splitInfo(useMissing = false)

  def splitInfo(useMissing: Boolean): Double = {
    val start: Int = if (useMissing) 0 else 1
    val totals: Array[Double] = new Array[Double](testLabels.length)
    for (i <- start until testLabels.length) {
      for (j <- 0 until targetLabels.length) {
        totals(i) += values(i)(j)
      }
    }

    var total: Double = 0
    for (i <- start until totals.length) {
      total += totals(i)
    }

    var splitInfo: Double = 0
    for (i <- start until totals.length) {
      if (totals(i) > 0) {
        splitInfo += -log2(totals(i) / total) * totals(i) / total
      }
    }
    splitInfo
  }

  def gainRatio: Double = gainRatio(useMissing = false)

  def gainRatio(useMissing: Boolean): Double = infoGain(useMissing) / splitInfo(useMissing)

  /**
   * Computes the number of columns which have totals equal or greater than minWeight minWeight,
   * considering missing values only if isMissing is true.
   *
   * @param useMissing if missing row information is used
   * @return number of columns which meet criteria
   */
  def countWithMinimum(useMissing: Boolean, minWeight: Double): Int = {
    val start: Int = if (useMissing) 0 else 1
    val totals: Array[Double] = new Array[Double](testLabels.length)
    for (i <- start until testLabels.length)
      for (j <- 1 until targetLabels.length)
        totals(i) += values(i)(j)
    totals.count(x => x >= minWeight)
  }
}

/**
 * @author <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a>
 */
object DensityMatrix {
  /**
   * Labels which can be used when test feature is numeric.
   * One use case is finding the best binary split point for a numerical feature
   * in a decision tree classifier.
   */
  val NumericDefaultLabels: Array[String] = Array[String]("?", "less-equals", "greater")

  /**
   * Builds an empty density table having column and row names given as parameter
   * and the table size computed accordingly.
   * Be aware that the density table tool considers the first column and
   * first row as missing value.
   *
   * @param testLabels names for the test feature
   * @param targetLabels names for the target feature
   * @return an empty density feature with specified rows and columns
   */
  def apply(testLabels: Array[String], targetLabels: Array[String]): DensityMatrix = {
    val dt = new DensityMatrix
    dt.testLabels = testLabels
    dt.targetLabels = targetLabels
    dt.values = Array.fill(testLabels.length, targetLabels.length)(0.0)
    dt
  }

  /**
   * Builds a density table from two named nominal features of a given frame,
   * each observation having weight 1.
   *
   * @param df frame which contains the observations
   * @param testColName test feature which maps to rows
   * @param targetColName target feature which maps to columns
   * @return a density table filled with values from observations
   */
  def apply(df: Frame, testColName: String, targetColName: String): DensityMatrix = {
    apply(df, null, testColName, targetColName)
  }

  /**
   * Builds a density table from two named nominal features of a given frame,
   * each observation having weight specified by {@param weights}.
   *
   * @param df frame which contains observations
   * @param weights array with observation weights
   * @param testColName test feature which maps to rows
   * @param targetColName target feature which maps to columns
   * @return
   */
  def apply(df: Frame, weights: Value, testColName: String, targetColName: String): DensityMatrix = {
    require(df.col(targetColName).isNominal, "Target feature must be nominal")
    require(df.col(testColName).isNominal, "Test feature must be nominal")

    def virtualWeight(i: Int): Double = if (weights != null) weights.values(i) else 1.0

    val dt = apply(df.col(testColName).labels.dictionary, df.col(targetColName).labels.dictionary)
    for (i <- 0 until df.rowCount) {
      dt.update(df.indexes(i, testColName), df.indexes(i, targetColName), virtualWeight(i))
    }
    dt
  }
}
