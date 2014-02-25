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

import rapaio.data.{Index, Feature, Frame}
import rapaio.printer.Summarizable

/**
 * User: Aurelian Tutuianu <paderati@yahoo.com>
 */
@deprecated("not fully implemented", "1.0")
class ROC extends Summarizable {
  private var score: Feature = null
  private var classes: Feature = null
  private var _data: Frame = null
  private var _auc: Double = 0.0

  private def compute(): Unit = {
    var p: Int = 0
    var n: Int = 0
    var prevtp: Double = 0
    var prevfp: Double = 0
    _auc = 0.0
    for (i <- 0 until classes.rowCount) {
      if (!classes.missing(i)) {
        if (classes.indexes(i) > 0) p += 1
        else n += 1
      }
    }

    var fp: Double = 0
    var tp: Double = 0
    _auc = 0
    //  val sort: Feature = sort(score, RowComparators.numericComparator(score, false))
    val sort: Feature = null
    var len: Int = 1
    var prev: Double = Double.MinValue
    for (i <- 0 until sort.rowCount) {
      if (!(sort.missing(i) || classes.missing(sort.rowId(i)))) {
        if (sort.values(i) != prev) {
          prev = sort.values(i)
          len += 1
        }
      }
    }
    //  data = Frames.newMatrixFrame(len, Array[String]("threshold", "fpr", "tpr", "acc"))
    // TODO newMatrixFrame
    _data = null
    prev = Double.PositiveInfinity
    var pos: Int = 0
    for (i <- 0 until sort.rowCount) {
      if (!(sort.missing(i) || classes.missing(sort.rowId(i)))) {
        if (sort.values(i) != prev) {
          _auc += math.abs(prevfp - fp) * math.abs(prevtp + tp) / 2.0
          val accValue: Double = (tp + n - fp) / (0.0 + n + p)
          _data.values(pos, "threshold") = prev
          _data.values(pos, "fpr") = fp / (1.0 * n)
          _data.values(pos, "tpr") = tp / (1.0 * p)
          _data.values(pos, "acc") = accValue
          prevfp = fp
          prevtp = tp
          prev = sort.values(i)
          pos += 1
        }
        if (classes.indexes(sort.rowId(i)) > 0) tp += 1
        else fp += 1
      }
    }

    _data.values(pos, "threshold") = prev
    _data.values(pos, "fpr") = 1.0
    _data.values(pos, "tpr") = 1.0
    _data.values(pos, "acc") = p / (0.0 + n + p)
    _auc += math.abs(n - prevfp) * (p + prevtp) / 2.0
    _auc /= (1.0 * p * n)
  }

  def data: Frame = _data

  def getAuc: Double = _auc

  override def buildSummary(sb: StringBuilder): Unit = ???

}

object ROC {

  def apply(score: Feature, actual: Feature, predict: Feature): ROC = {
    val roc = new ROC
    roc.score = score
    roc.classes = new Index(actual.rowCount, actual.rowCount, 0)
    for (i <- 0 until actual.rowCount) {
      if (actual.labels(i) == predict.labels(i)) {
        roc.classes.indexes(i) = 1
      }
      else {
        roc.classes.indexes(i) = 0
      }
    }
    roc.compute()
    roc
  }

  def apply(score: Feature, actual: Feature, index: Int): ROC = {
    apply(score, actual, actual.labels.dictionary(index))
  }

  def apply(score: Feature, actual: Feature, label: String): ROC = {
    val roc = new ROC()
    roc.score = score
    roc.classes = new Index(actual.rowCount, actual.rowCount, 0)
    for (i <- 0 until actual.rowCount) {
      if (actual.labels(i) == label) {
        roc.classes.indexes(i) = 1
      }
      else {
        roc.classes.indexes(i) = 0
      }
    }
    roc.compute()
    roc
  }


}