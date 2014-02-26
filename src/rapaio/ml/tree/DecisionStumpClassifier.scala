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

package rapaio.ml.tree

import rapaio.data._
import rapaio.ml.Classifier
import rapaio.core.stat.DensityMatrix
import rapaio.ml.base.MajorityClassifier

/**
 * User: Aurelian Tutuianu <paderati@yahoo.com>
 */
class DecisionStumpClassifier extends Classifier {

  var minWeight: Double = 2.0

  private var splitCol: String = _
  private var splitLabel: String = _
  private var splitValue: Double = .0
  private var splitGain: Double = .0
  private val leftClassifier: Classifier = new MajorityClassifier()
  private val rightClassifier: Classifier = new MajorityClassifier()
  private val defaultClassifier: Classifier = new MajorityClassifier()


  /**
   * Name of the classification algorithm used for informative messages
   * @return short name of the implemented classifier
   */
  override def name: String = "DecisionStump Classifier"

  /**
   * Algorithm name with the eventual parameter values used.
   * @return algorithm name and parameter values
   */
  override def description: String = "DecisionStump Classifier"

  def newInstance: Classifier = new DecisionStumpClassifier

  def learn(df: Frame, weights: Value, targetName: String) {
    splitGain = 0
    _dictionary = df.col(targetName).labels.dictionary
    _target = targetName
    df.colNames.foreach(testName => {
      if (targetName != testName) {
        df.col(testName).shortName match {
          case "nom" => evaluateNominal(df, weights, testName)
          case _ => evaluateNumeric(df, weights, testName)
        }
      }
    })
    this.defaultClassifier.learn(df, weights, targetName)

    if (Option(splitCol).isDefined) {
      val split =
        if (splitValue.isNaN) df.binarySplit((df: Frame, row: Int) => df.labels(row, splitCol) == splitLabel)
        else df.binarySplit((df: Frame, row: Int) => df.values(row, splitCol) <= splitValue)

      leftClassifier.learn(split._1, _target)
      rightClassifier.learn(split._2, _target)
    } else {
      leftClassifier.learn(df, _target)
      rightClassifier.learn(df, _target)
    }
  }

  private def evaluateNominal(df: Frame, weights: Value, test: String) {
    val dm = DensityMatrix(df, weights, test, _target)
    val testCol = df.col(test)
    val dict = testCol.labels.dictionary
    for (i <- 0 until dict.length) {
      val accuracy = dm.binaryAccuracy(i)
      if (splitGain < accuracy) {
        splitCol = test
        splitGain = accuracy
        splitLabel = dict(i)
        splitValue = Double.NaN
      }
    }
  }

  private def evaluateNumeric(df: Frame, weights: Value, test: String) {

    val sort = (0 until df.rowCount).toArray
    sort.sortWith((i, j) => df.values(i, test) < df.values(j, test))

    // build an initial density matrix required for computing accuracies
    val dm = DensityMatrix(DensityMatrix.NumericDefaultLabels, df.col(_target).labels.dictionary)
    sort.foreach(i => dm.update(2, df.indexes(i, _target), weights.values(i)))

    // test each split point to find the best numerical split
    var lastPos = sort(0)
    sort.foreach(i => {
      dm.moveOnRow(2, 1, df.indexes(i, _target), weights.values(i))
      if ((dm.countWithMinimum(useMissing = false, minWeight) == 2) &&
        i != sort(0) && df.values(lastPos, test) != df.values(i, test)) {

        lastPos = i
        val accuracy = dm.binaryAccuracy(1)
        if (splitGain < accuracy) {
          splitCol = test
          splitGain = accuracy
          splitLabel = null
          splitValue = df.values(i, test)
        }
      }
    })
  }

  def predict(df: Frame, row: Int): (String, Array[Double]) = {
    if (df.missing(row, splitCol)) {
      defaultClassifier.predict(df, row)
    }
    else {
      val classifier =
        if (splitValue.isNaN) {
          if (df.labels(row, splitCol) == splitLabel) leftClassifier else rightClassifier
        } else {
          if (df.values(row, splitCol) <= splitValue) leftClassifier else rightClassifier
        }

      classifier.predict(df, row)
    }
  }

  def buildSummary(sb: StringBuilder): Unit = ???
}