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

package rapaio.ml.base

import rapaio.ml.Classifier
import rapaio.data.{Value, Frame}
import rapaio.core.stat.DensityVector

/**
 * @author <a href="mailto:padreati@yahoo.com>Aurelian Tutuianu</a>
 */
class ModeClassifier extends Classifier {

  private var _predictedLabel: String = _
  private var _dfRowCount: Int = _

  /**
   * Predict classes for new data set instances
   *
   * @param df data set instances
   */
  override def predict(df: Frame, row: Int): (String, Array[Double]) = {
    val density = new Array[Double](_dictionary.length)
    density(_dictionary.indexOf(_predictedLabel)) = 1.0
    (_predictedLabel, density)
  }

  /**
   * Fit a classifier on instances specified by frame, with row weights
   * and target as classColName.
   *
   * @param df           data set instances
   * @param weights      row weights
   * @param targetName target column name
   */
  override def learn(df: Frame, weights: Value, targetName: String): Unit = {
    val dv = DensityVector(df.col(targetName), weights)
    _predictedLabel = dv.mode(useMissing = false)
    _dictionary = df.col(targetName).labels.dictionary
    _dfRowCount = df.rowCount
  }

  /**
   * Creates a new classifier instance with the same parameters as the original.
   * The fitted model and other artifacts are not replicated.
   *
   * @return new parametrized instance
   */
  override def newInstance(): Classifier = new ModeClassifier

  /**
   * Algorithm name with the eventual parameter values used.
   * @return algorithm name and parameter values
   */
  override def description(): String =
    """
      |ModelClassifier
    """.stripMargin

  /**
   * Name of the classification algorithm used for informative messages
   * @return short name of the implemented classifier
   */
  override def name(): String = "ModeClassifier"

  override def buildModelSummary(sb: StringBuilder): Unit = {
    sb.append("observations: " + _dfRowCount + "\n")
    sb.append("predicted label: " + _predictedLabel + "\n")
  }
}
