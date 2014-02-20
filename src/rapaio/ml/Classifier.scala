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

package rapaio.ml

import rapaio.data._

/**
 * Trait for all classifiers. For now the number of target features is 1.
 *
 * @author <a href="email:padreati@yahoo.com>Aurelian Tutuianu</a>
 */
trait Classifier {

  /**
   * Name of the classification algorithm used for informative messages
   * @return short name of the implemented classifier
   */
  def name: String

  /**
   * Algorithm name with the eventual parameter values used.
   * @return algorithm name and parameter values
   */
  def description: String

  /**
   * Creates a new classifier instance with the same parameters as the original.
   * The fitted model and other artifacts are not replicated.
   *
   * @return new parametrized instance
   */
  def newInstance: Classifier

  /**
   * Fit a classifier on instances specified by frame, with row weights
   * and target as classColName.
   *
   * @param df           data set instances
   * @param weights      row weights
   * @param targetName target column name
   */
  def learn(df: Frame, weights: Value, targetName: String)

  /**
   * Fit a classifier on instances specified by frame, with row weights
   * equal to 1 and target as classColName.
   *
   * @param df           data set instances
   * @param targetName target column name
   */
  def learn(df: Frame, targetName: String) {
    learn(df, new Value(df.rowCount, df.rowCount, 1.), targetName)
  }

  /**
   * Builds a new classifier using artifacts from a previous classifier.
   *
   * @param df           data set instances
   * @param weights      row weights
   * @param targetName target column name
   * @param classifier   previous classifier
   */
  def learnFurther(df: Frame, weights: Value, targetName: String, classifier: Classifier) {
    sys.error("learnFurther not implemented for " + name)
  }

  /**
   * Builds a new classifier using artifacts from a previous classifier.
   *
   * @param df           data set instances
   * @param targetName target column name
   * @param classifier   previous classifier
   */
  def learnFurther(df: Frame, targetName: String, classifier: Classifier) {
    learnFurther(df, new Value(df.rowCount, df.rowCount, 1.), targetName, classifier)
  }

  /**
   * Predict classes for new data set instances
   *
   * @param df data set instances
   */
  def predict(df: Frame): Unit

  /**
   * Predict further classes for new data set instances, using
   * as much as possible fitted artifacts from previous classifier.
   * <p/>
   * The frame df is supposed to be the same, otherwise
   * the result is unpredictable
   *
   * @param df data set instances
   */
  def predictFurther(df: Frame, classifier: Classifier): Unit = {
    sys.error("Predict further not implemented for " + name)
  }

  /**
   * Returns predicted classes
   *
   * @return nominal vector with predicted classes
   */
  def getPrediction: Nominal

  /**
   * Returns predicted class distribution.
   *
   * @return predicted class distribution (frame with one
   *         column for each target class, including the missing
   *         label column in first position)
   */
  def getDistribution: Frame
}
