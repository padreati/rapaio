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
  override def name(): String = "AdaBoostSAMME"

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

  def learn(df: Frame, weights: Value, targetName: String) {
    _dictionary = df.col(targetName: String).labels.dictionary
    _k = _dictionary.length - 1
    _w = weights.solidCopy()

    val total: Double = Sum(_w).value
    _w.values.transform(x => x / total)

    var run = true
    for (i <- 0 until _t if run) {
      val hh = weak.newInstance()
      hh.learn(df, _w.solidCopy(), targetName)
      hh.predict(df)
      val hp = hh.prediction
      var err = 0.0
      for (i <- 0 until df.rowCount) {
        if (hp.labels(i) != df.col(targetName).labels(i)) {
          err += _w.values(i)
        }
      }

      hh.summary()
      val alpha = math.log((1.0 - err) / err) + math.log(_k - 1.0)
      if ((err == 0) || (err > (1.0 - 1.0 / _k))) {
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
            _w.values(i) = _w.values(i) / (_k * err)
          } else {
            _w.values(i) = _w.values(i) / (_k * (1.0 - err))
          }
        }
      }
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

  //  override def learnFurther(df: Frame, weights: List[Double], classColName: String, classifier: AdaBoostSAMMEClassifier) {
  //    if (classifier == null) {
  //      learn(df, weights, classColName)
  //      return
  //    }
  //    dict = df.getCol(classColName).getDictionary
  //    k = dict.length - 1
  //    h = new ArrayList[Classifier[_]](classifier.h)
  //    a = new ArrayList[Double](classifier.a)
  //    if (t == classifier.t) {
  //      return
  //    }
  //    w = classifier.w {
  //      var i: Int = h.size
  //      while (i < t) {
  //        {
  //          val hh: Classifier[_] = weak.newInstance
  //          hh.learn(df, new ArrayList[Double](w), classColName)
  //          hh.predict(df)
  //          val hpred: Nominal = hh.getPrediction
  //          var err: Double = 0 {
  //            var j: Int = 0
  //            while (j < df.getRowCount) {
  //              {
  //                if (hpred.getIndex(j) != df.getCol(classColName).getIndex(j)) {
  //                  err += w.get(j)
  //                }
  //              }
  //              ({
  //                j += 1;
  //                j - 1
  //              })
  //            }
  //          }
  //          val alpha: Double = log((1.- err) / err) + log(k - 1)
  //          if (err == 0) {
  //            if (h.isEmpty) {
  //              h.add(hh)
  //              a.add(alpha)
  //            }
  //            break //todo: break is not supported
  //          }
  //          if (err > (1 - 1 / k)) {
  //            i -= 1
  //            continue //todo: continue is not supported
  //          }
  //          h.add(hh)
  //          a.add(alpha) {
  //            var j: Int = 0
  //            while (j < w.size) {
  //              {
  //                if (hpred.getIndex(j) != df.getCol(classColName).getIndex(j)) {
  //                  w.set(j, w.get(j) * (k - 1) / (k * err))
  //                }
  //                else {
  //                  w.set(j, w.get(j) / (k * (1.- err)))
  //                }
  //              }
  //              ({
  //                j += 1;
  //                j - 1
  //              })
  //            }
  //          }
  //        }
  //        ({
  //          i += 1;
  //          i - 1
  //        })
  //      }
  //    }
  //  }

  //  override def predictFurther(df: Frame, classifier: AdaBoostSAMMEClassifier) {
  //    if (classifier == null) {
  //      predict(df)
  //      return
  //    }
  //    pred = classifier.pred
  //    dist = classifier.dist {
  //      var i: Int = classifier.h.size
  //      while (i < min(t, h.size)) {
  //        {
  //          h.get(i).predict(df) {
  //            var j: Int = 0
  //            while (j < df.getRowCount) {
  //              {
  //                val index: Int = h.get(i).getPrediction.getIndex(j)
  //                dist.setValue(j, index, dist.getValue(j, index) + a.get(i))
  //              }
  //              ({
  //                j += 1;
  //                j - 1
  //              })
  //            }
  //          }
  //        }
  //        ({
  //          i += 1;
  //          i - 1
  //        })
  //      }
  //    } {
  //      var i: Int = 0
  //      while (i < dist.getRowCount) {
  //        {
  //          var max: Double = 0
  //          var prediction: Int = 0 {
  //            var j: Int = 1
  //            while (j < dist.getColCount) {
  //              {
  //                if (dist.getValue(i, j) > max) {
  //                  prediction = j
  //                  max = dist.getValue(i, j)
  //                }
  //              }
  //              ({
  //                j += 1;
  //                j - 1
  //              })
  //            }
  //          }
  //          pred.setIndex(i, prediction)
  //        }
  //        ({
  //          i += 1;
  //          i - 1
  //        })
  //      }
  //    }
  //  }


  override def buildSummary(sb: StringBuilder): Unit = {
    sb.append("AdaBoostSAMME [t=").append(_t).append("]\n")
    sb.append("weak learners built:").append(_h.length).append("\n")
  }

}

