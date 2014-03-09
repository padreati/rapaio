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
import rapaio.core.stat.{DensityVector, DensityMatrix}
import rapaio.ml.base.ModeClassifier

/**
 * User: Aurelian Tutuianu <paderati@yahoo.com>
 */
class DecisionStumpClassifier extends Classifier {

  var minCount: Int = 2

  private var _splitCol: String = null
  private var _splitLabel: String = _
  private var _splitValue: Double = .0
  private var _splitGain: Double = .0
  private val _leftClassifier: ModeClassifier = new ModeClassifier()
  private val _rightClassifier: ModeClassifier = new ModeClassifier()
  private val _defaultClassifier: ModeClassifier = new ModeClassifier()

  /**
   * Name of the classification algorithm used for informative messages
   * @return short name of the implemented classifier
   */
  override def name(): String = "DecisionStump Classifier"

  /**
   * Algorithm name with the eventual parameter values used.
   * @return algorithm name and parameter values
   */
  override def description(): String = "DecisionStump Classifier"

  def newInstance(): Classifier = {
    val c = new DecisionStumpClassifier()
    c.minCount = minCount
    c
  }

  def learn(df: Frame, weights: Value, targetName: String) {

    require(df.colCount > 1, "should have at least an input feature")
    collectLearningInfo(df, weights, targetName)

    _splitGain = 0

    df.colNames.foreach(testName => {
      if (targetName != testName) {
        df.col(testName).typeName match {
          case "nom" => evaluateNominal(df, weights, testName)
          case _ => evaluateNumeric(df, weights, testName)
        }
      }
    })

    if (_splitCol != null) {
      val missingSplit = df.weightedSplit(weights, (df: Frame, row: Int) => df.missing(row, _splitCol))
      val missing = missingSplit._1
      val complete = missingSplit._2

      val split = complete._1.weightedSplit(complete._2, (df: Frame, row: Int) =>
        if (df.col(_splitCol).isNominal) df.labels(row, _splitCol) == _splitLabel
        else df.values(row, _splitCol) <= _splitValue
      )

      _leftClassifier.learn(split._1._1, split._1._2, _target)
      _rightClassifier.learn(split._2._1, split._2._2, _target)
      _defaultClassifier.learn(missing._1, missing._2, _target)
    } else {
      _defaultClassifier.learn(df, weights, _target)
    }
  }

  private def evaluateNominal(df: Frame, weights: Value, test: String) {
    val testCol = df.col(test)
    val dict = testCol.labels.dictionary
    val dv = DensityVector(df.col(test))
    for (i <- 1 until dict.length) {
      if (dv.values(i) >= minCount && df.rowCount - dv.values(0) - dv.values(i) >= minCount) {
        val dm = DensityMatrix(df.col(test), df.col(_target), weights, dict(i))
        val gain = dm.infoGain(useMissing = false)
        if (_splitGain < gain) {
          _splitCol = test
          _splitGain = gain
          _splitLabel = dict(i)
          _splitValue = Double.NaN
        }
      }
    }
  }

  private def evaluateNumeric(df: Frame, weights: Value, test: String) {

    val sort = (0 until df.rowCount).sortWith((i, j) => {
      if (df.missing(i, test)) !df.missing(j, test)
      else if (df.missing(j, test)) false
      else df.values(i, test) < df.values(j, test)
    })

    // build an initial density matrix required for computing accuracies
    val dm = DensityMatrix(DensityMatrix.NumericDefaultLabels, df.col(_target).labels.dictionary)
    (0 until df.rowCount).foreach(i => dm.update(2, df.indexes(i, _target), weights.values(i)))

    val missingCount = df.col(test).values.count(x => x.isNaN)

    // test each split point to find the best numerical split
    for (i <- 0 until sort.length) {
      dm.moveOnRow(2, 1, df.indexes(sort(i), _target), weights.values(sort(i)))
      if ((i >= minCount + missingCount) && (i <= df.rowCount - minCount - 1) &&
        df.values(sort(i + 1), test) != df.values(sort(i), test)) {
        val gain = dm.infoGain(useMissing = false)
        if (_splitGain < gain) {
          _splitCol = test
          _splitGain = gain
          _splitLabel = null
          _splitValue = df.values(sort(i), test)
        }
      }
    }
  }

  def predict(df: Frame) {
    _prediction = new Nominal(df.rowCount, _dictionary)
    _distribution = Frame.matrix(df.rowCount, _dictionary)
    for (i <- 0 until df.rowCount) {
      if (_splitCol == null || df.missing(i, _splitCol)) {
        val label = _defaultClassifier.predictedLabel
        _prediction.labels(i) = label
        _distribution.values(i, _dictionary.indexOf(label)) = 1.0
      } else {
        val classifier =
          if (df.col(_splitCol).isNominal) {
            if (df.labels(i, _splitCol) == _splitLabel) _leftClassifier else _rightClassifier
          } else {
            if (df.values(i, _splitCol) <= _splitValue) _leftClassifier else _rightClassifier
          }
        val label = classifier.predictedLabel
        _prediction.labels(i) = label
        _distribution.values(i, _dictionary.indexOf(label)) = 1.0
      }
    }
  }


  override def buildParameterSummary(sb: StringBuilder): Unit = {
    sb.append("minCount=" + minCount + "\n")
  }

  override def buildModelSummary(sb: StringBuilder): Unit = {
    sb.append("\n")
    sb.append("splitCol: " + _splitCol + "\n")
    sb.append("splitLabel: " + _splitLabel + "\n")
    sb.append("splitValue: " + _splitValue + "\n")
    sb.append("splitGain: " + _splitGain + "\n")
    sb.append("observations: " + _rowCount + "\n")

    def subSummary(title: String, c: ModeClassifier): Unit = {
      sb.append("- " + title + " (" + c.name + "): predicted label -> " + c.predictedLabel + ", observations: " + c.learnedObservations + "\n")
    }
    subSummary("left", _leftClassifier)
    subSummary("right", _rightClassifier)
    subSummary("default", _defaultClassifier)
  }
}