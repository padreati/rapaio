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

package rapaio.ml.boosting

import rapaio.ml.Classifier
import rapaio.data.{Value, Frame}
import rapaio.core.stat.Sum
import rapaio.ml.tree.DecisionStumpClassifier
import scala.annotation.tailrec

/**
 * @author <a href="mailto:padreati@yahoo.com>Aurelian Tutuianu</a>
 */
class AdaBoostSAMMEClassifier extends Classifier {

  // parameters

  // weak classifier instance used to create new weak classifiers
  var weak: DecisionStumpClassifier = new DecisionStumpClassifier()

  var learningRate: Double = 1.0

  // number of weak classifiers to build
  private var _t: Int = 10

  // list of alpha coefficients
  private var _a: List[Double] = Nil

  // lists of weak classifiers
  private var _h: List[Classifier] = Nil

  // current weights
  private var _w: Value = _

  // number of classes without missing values
  private var _k: Double = _

  def newInstance(): AdaBoostSAMMEClassifier = {
    val classifier = new AdaBoostSAMMEClassifier()
    classifier.weak = weak
    classifier._t = _t
    classifier
  }

  /**
   * Name of the classification algorithm used for informative messages
   * @return short name of the implemented classifier
   */
  override def name(): String = "AdaBoost.SAMME"

  /**
   * Algorithm name with the eventual parameter values used.
   * @return algorithm name and parameter values
   */
  override def description(): String =
    """
      |AdaBoostSAMME
      |A variant of AdaBoost.M1 adapted to handle classification
      |with more than two labels. In the case when K (number of
      |labels of the target feature equals 2, the SAMME algorithm
      |is equivalent with AdaBoost.M1
    """.stripMargin


  def times = _t

  def times_=(a: Int) = _t = a

  private def buildLearners(df: Frame, weights: Value, targetName: String): Unit = {

    val total: Double = Sum(_w).value
    _w.values.transform(x => x / total)

    var run = true
    for (i <- _h.length until _t if run) {
      val hh = weak.newInstance()
      hh.learn(df, _w.solidCopy(), targetName)
      hh.predict(df)
      hh.summary()
      val hp = hh.prediction
      var acc = 0.0
      var err = 0.0
      for (i <- 0 until df.rowCount) {
        if (hp.labels(i) != df.col(targetName).labels(i)) {
          err += _w.values(i)
        } else {
          acc += _w.values(i)
        }
      }
      val alpha = math.log((1.0 - err) / err) + math.log(_k - 1.0)
      if ((err == 0) || (err > (1.0 - (1.0 / _k)))) {
        if (_h == Nil) {
          _h = List(hh)
          _a = List(alpha)
        }
        run = false
      } else {
        _h = _h ::: List(hh)
        _a = _a ::: List(alpha)

        for (i <- 0 until _w.rowCount) {
          if (hp.indexes(i) != df.col(targetName).indexes(i)) {
            _w.values(i) = _w.values(i) * learningRate * (1.0 - err) * (_k - 1) / err
          }
        }
        val sum = Sum(_w).value
        _w.values.transform(x => x / sum)
      }
    }
  }

  override def learn(df: Frame, weights: Value, targetName: String) {
    _dictionary = df.col(targetName: String).labels.dictionary
    _k = _dictionary.length - 1
    _w = weights.solidCopy()

    _h = Nil
    _a = Nil

    buildLearners(df, weights, targetName)
  }

  override def learnFurther(df: Frame, weights: Value, targetName: String, classifier: Classifier) {

    if (classifier == null || !classifier.isInstanceOf[AdaBoostSAMMEClassifier]) {
      learn(df, weights, targetName)
    } else {
      _dictionary = df.col(targetName).labels.dictionary
      _k = _dictionary.length - 1
      val c = classifier.asInstanceOf[AdaBoostSAMMEClassifier]
      _h = c._h
      _a = c._a
      _w = if (c._w == null) weights.solidCopy() else c._w.solidCopy()

      buildLearners(df, weights, targetName)
    }
  }

  /**
   * Predicts class for one instance from the data set
   */
  override def predict(df: Frame, row: Int): (String, Array[Double]) = {
    val d = new Array[Double](_dictionary.length)

    @tailrec def eval(h: List[Classifier], a: List[Double]) {
      h match {
        case Nil => Unit
        case _ =>
          val p = h.head.predict(df, row)
          val index: Int = _dictionary.indexOf(p._1)
          d(index) += a.head
          eval(h.tail, a.tail)
      }
    }
    eval(_h, _a)

    var max: Double = 0
    var prediction: Int = 0
    for (j <- 0 until d.length) {
      if (d(j) > max) {
        prediction = j
        max = d(j)
      }
    }
    (_dictionary(prediction), d)
  }

  override def predictFurther(df: Frame, classifier: Classifier) {
    if (classifier == null || classifier.isInstanceOf[AdaBoostSAMMEClassifier]) {
      predict(df)
      return
    }
    val c = classifier.asInstanceOf[AdaBoostSAMMEClassifier]
    _prediction = c._prediction
    _distribution = c._distribution
    for (i <- c._h.size until math.min(_t, _h.size)) {
      _h(i).predict(df)
      for (j <- 0 until df.rowCount) {
        val index: Int = _h(i).prediction.indexes(j)
        _distribution.values(j, index) = _a(i)
      }

    }
    for (i <- 0 until _distribution.rowCount) {
      var max: Double = 0
      var prediction: Int = 0
      for (j <- 1 until _distribution.colCount) {
        if (_distribution.values(i, j) > max) {
          prediction = j
          max = _distribution.values(i, j)
        }
      }
      _prediction.indexes(i) = prediction
    }
  }


  override def buildSummary(sb: StringBuilder): Unit = {
    sb.append("AdaBoostSAMME [t=").append(_t).append("]\n")
    sb.append("weak learners built:").append(_h.length).append("\n")
  }

}

