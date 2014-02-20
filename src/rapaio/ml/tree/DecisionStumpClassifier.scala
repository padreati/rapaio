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
import rapaio.ml.tools.DensityTable

/**
 * User: Aurelian Tutuianu <paderati@yahoo.com>
 */
class DecisionStumpClassifier extends Classifier {

  private var targetName: String = _
  private var dict: Array[String] = _
  private var splitCol: String = _
  private var splitLabel: String = _
  private var splitValue: Double = .0
  private var splitGain: Double = .0
  private var leftLabel: String = null
  private var rightLabel: String = null
  private var defaultLabel: String = null
  private var pred: Nominal = null
  private var distr: Frame = null


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
    dict = df.col(targetName).labels.dictionary
    val totals = new Array[Double](dict.length)
    for (i <- 0 until df.rowCount) {
      totals(df.col(targetName).indexes(i)) += weights.values(i)
    }
    df.colNames.foreach(testName => {
      if (targetName != testName) {
        df.col(testName).shortName match {
          case "nom" => evaluateNominal(df, weights, testName, totals)
          case _ => evaluateNumeric(df, weights, testName, totals)
        }
      }
    })
    this.defaultLabel = buildDefaultLabel(df, weights)
  }

  private def buildDefaultLabel(df: Frame, weights: Value): String = {
    val dt = DensityTable(df, targetName, targetName)

    //    val freq: Array[Double] = new Array[Double](classCol.getDictionary.length)
    //    var total: Int = 0 {
    //      var i: Int = 0
    //      while (i < df.getRowCount) {
    //        {
    //          freq(classCol.getIndex(i)) += weights.get(i)
    //          total += 1
    //        }
    //        ({
    //          i += 1;
    //          i - 1
    //        })
    //      }
    //    }
    //    if (total == 0) return dict(0)
    //    var max: Double = 0
    //    val sel: List[Integer] = new ArrayList[Integer] {
    //      var i: Int = 1
    //      while (i < dict.length) {
    //        {
    //          if (freq(i) > max) {
    //            max = freq(i)
    //            sel.clear
    //            sel.add(i)
    //            continue //todo: continue is not supported
    //          }
    //          if (freq(i) == max) {
    //            sel.add(i)
    //          }
    //        }
    //        ({
    //          i += 1;
    //          i - 1
    //        })
    //      }
    //    }
    //    return dict(sel.get(RandomSource.nextInt(sel.size)))
    ""
  }

  private def evaluateNominal(df: Frame, weights: Value, testName: String, totals: Array[Double]) {
    //    val dist: Array[Array[Double]] = new Array[Array[Double]](col.getDictionary.length, classCol.getDictionary.length) {
    //      var i: Int = 0
    //      while (i < df.getRowCount) {
    //        {
    //          dist(col.getIndex(i))(classCol.getIndex(i)) += weights.get(i)
    //        }
    //        ({
    //          i += 1;
    //          i - 1
    //        })
    //      }
    //    }
    //    {
    //      var i: Int = 1
    //      while (i < col.getDictionary.length) {
    //        {
    //          val metric: Double = computeGini(dist(0), dist(i), total)
    //          if (validNumber(metric) && metric > splitGain) {
    //            splitGain = metric
    //            splitCol = colName
    //            splitLabel = col.getDictionary(i)
    //            splitValue = Double.NaN
    //            val left: List[Integer] = new ArrayList[Integer]
    //            val right: List[Integer] = new ArrayList[Integer]
    //            var leftMax: Double = 0
    //            var rightMax: Double = 0 {
    //              var j: Int = 0
    //              while (j < dict.length) {
    //                {
    //                  if (dist(i)(j) > leftMax) {
    //                    leftMax = dist(i)(j)
    //                    left.clear
    //                    left.add(j)
    //                    continue //todo: continue is not supported
    //                  }
    //                  if (dist(i)(j) == leftMax) {
    //                    left.add(j)
    //                  }
    //                }
    //                ({
    //                  j += 1;
    //                  j - 1
    //                })
    //              }
    //            } {
    //              var j: Int = 0
    //              while (j < dict.length) {
    //                {
    //                  if (total(j) - dist(0)(j) - dist(i)(j) > rightMax) {
    //                    rightMax = total(j) - dist(0)(j) - dist(i)(j)
    //                    right.clear
    //                    right.add(j)
    //                    continue //todo: continue is not supported
    //                  }
    //                  if (total(j) - dist(0)(j) - dist(i)(j) == rightMax) {
    //                    right.add(j)
    //                  }
    //                }
    //                ({
    //                  j += 1;
    //                  j - 1
    //                })
    //              }
    //            }
    //            leftLabel = dict(left.get(RandomSource.nextInt(left.size)))
    //            rightLabel = dict(right.get(RandomSource.nextInt(right.size)))
    //          }
    //        }
    //        ({
    //          i += 1;
    //          i - 1
    //        })
    //      }
    //    }
  }

  private def evaluateNumeric(df: Frame, weights: Value, testName: String, totals: Array[Double]) {
    //    val p: Array[Array[Double]] = new Array[Array[Double]](2, classCol.getDictionary.length)
    //    val rowCounts: Array[Int] = new Array[Int](2)
    //    val sort: Nothing = BaseFilters.sort(Vectors.newSeq(0, df.getRowCount - 1, 1), RowComparators.numericComparator(col, true)) {
    //      var i: Int = 0
    //      while (i < df.getRowCount - 1) {
    //        {
    //          val row: Int = if (col.isMissing(sort.getIndex(i))) 0 else 1
    //          val index: Int = classCol.getIndex(sort.getIndex(i))
    //          p(row)(index) += weights.get(sort.getIndex(i))
    //          rowCounts(row) += 1
    //          if (row == 0) {
    //            continue //todo: continue is not supported
    //          }
    //          if (rowCounts(1) == 0) continue //todo: continue is not supported
    //          if (df.getRowCount - rowCounts(1) - rowCounts(0) eq 0) continue //todo: continue is not supported
    //          if (col.getValue(sort.getIndex(i)) < col.getValue(sort.getIndex(i + 1))) {
    //            val metric: Double = compute(p(0), p(1), total)
    //            if (!validNumber(metric)) continue //todo: continue is not supported
    //            if (validNumber(metric) && metric > splitGain) {
    //              splitGain = metric
    //              splitCol = colName
    //              splitLabel = null
    //              splitValue = col.getValue(sort.getIndex(i))
    //              val left: List[Integer] = new ArrayList[Integer]
    //              val right: List[Integer] = new ArrayList[Integer]
    //              var leftMax: Double = 0
    //              var rightMax: Double = 0 {
    //                var j: Int = 0
    //                while (j < dict.length) {
    //                  {
    //                    if (p(1)(j) > leftMax) {
    //                      leftMax = p(1)(j)
    //                      left.clear
    //                      left.add(j)
    //                      continue //todo: continue is not supported
    //                    }
    //                    if (p(1)(j) == leftMax) {
    //                      left.add(j)
    //                    }
    //                  }
    //                  ({
    //                    j += 1;
    //                    j - 1
    //                  })
    //                }
    //              } {
    //                var j: Int = 0
    //                while (j < dict.length) {
    //                  {
    //                    if (total(j) - p(0)(j) - p(1)(j) > rightMax) {
    //                      rightMax = total(j) - p(0)(j) - p(1)(j)
    //                      right.clear
    //                      right.add(j)
    //                      continue //todo: continue is not supported
    //                    }
    //                    if (total(j) - p(0)(j) - p(1)(j) == rightMax) {
    //                      right.add(j)
    //                    }
    //                  }
    //                  ({
    //                    j += 1;
    //                    j - 1
    //                  })
    //                }
    //              }
    //              leftLabel = dict(left.get(RandomSource.nextInt(left.size)))
    //              rightLabel = dict(right.get(RandomSource.nextInt(right.size)))
    //            }
    //          }
    //        }
    //        ({
    //          i += 1;
    //          i - 1
    //        })
    //      }
    //    }
  }

  def predict(df: Frame) {
    //    pred = new Nominal(df.getRowCount, dict)
    //    val it: Nothing = df.getIterator
    //    while (it.next) {
    //      if (splitCol == null || it.isMissing(splitCol)) {
    //        pred.setLabel(it.getRow, defaultLabel)
    //        continue //todo: continue is not supported
    //      }
    //      if (df.getCol(splitCol).getType.isNumeric) {
    //        if (it.getValue(splitCol) <= splitValue) {
    //          pred.setLabel(it.getRow, leftLabel)
    //        }
    //        else {
    //          pred.setLabel(it.getRow, rightLabel)
    //        }
    //      }
    //      else {
    //        if (splitLabel == it.getLabel(splitCol)) {
    //          pred.setLabel(it.getRow, leftLabel)
    //        }
    //        else {
    //          pred.setLabel(it.getRow, rightLabel)
    //        }
    //      }
    //    }
  }

  def getPrediction: Nominal = {
    return pred
  }

  def getDistribution: Frame = {
    return distr
  }

  def summary {
  }

  //
  //  private def compute(missing: Array[Double], pa: Array[Double], total: Array[Double]): Double = {
  //    return computeInfoGain(missing, pa, total)
  //  }
  //
  //  private def computeInfoGain(missing: Array[Double], pa: Array[Double], total: Array[Double]): Double = {
  //    var to: Double = 0
  //    var tl: Double = 0
  //    var tr: Double = 0 {
  //      var i: Int = 1
  //      while (i < total.length) {
  //        {
  //          val left: Double = pa(i)
  //          val right: Double = total(i) - pa(i) - missing(i)
  //          val orig: Double = total(i) - missing(i)
  //          tl += left
  //          tr += right
  //          to += orig
  //        }
  //        ({
  //          i += 1;
  //          i - 1
  //        })
  //      }
  //    }
  //    if (tl == 0 || tr == 0) return Double.NaN
  //    if (!validNumber(tl) || !validNumber(tr)) return Double.NaN
  //    var po: Double = 0
  //    var pl: Double = 0
  //    var pr: Double = 0 {
  //      var i: Int = 1
  //      while (i < total.length) {
  //        {
  //          val pleft: Double = pa(i) / tl
  //          val pright: Double = (total(i) - pa(i) - missing(i)) / tr
  //          val porig: Double = (total(i) - missing(i)) / to
  //          po -= if (porig == 0) 0 else log(porig) * porig
  //          pl -= if (pleft == 0) 0 else log(pleft) * pleft
  //          pr -= if (pright == 0) 0 else log(pright) * pright
  //        }
  //        ({
  //          i += 1;
  //          i - 1
  //        })
  //      }
  //    }
  //    return po - (tl * pl + tr * pr) / (tl + tr)
  //  }
  //
  //  private def computeGini(missing: Array[Double], pa: Array[Double], total: Array[Double]): Double = {
  //    var totalOrig: Double = 0
  //    var totalLeft: Double = 0
  //    var totalRight: Double = 0 {
  //      var i: Int = 1
  //      while (i < total.length) {
  //        {
  //          val left: Double = pa(i)
  //          val right: Double = total(i) - pa(i) - missing(i)
  //          val orig: Double = total(i) - missing(i)
  //          totalLeft += left
  //          totalRight += right
  //          totalOrig += orig
  //        }
  //        ({
  //          i += 1;
  //          i - 1
  //        })
  //      }
  //    }
  //    if (totalLeft == 0 || totalRight == 0) return Double.NaN
  //    if (!validNumber(totalLeft) || !validNumber(totalRight)) return Double.NaN
  //    var giniOrig: Double = 1
  //    var giniLeft: Double = 1
  //    var giniRight: Double = 1 {
  //      var i: Int = 1
  //      while (i < total.length) {
  //        {
  //          val pleft: Double = pa(i) / totalLeft
  //          val pright: Double = (total(i) - pa(i) - missing(i)) / totalRight
  //          val porig: Double = (total(i) - missing(i)) / totalOrig
  //          giniOrig -= porig * porig
  //          giniLeft -= pleft * pleft
  //          giniRight -= pright * pright
  //        }
  //        ({
  //          i += 1;
  //          i - 1
  //        })
  //      }
  //    }
  //    return giniOrig - (totalLeft * giniLeft + totalRight * giniRight) / (totalLeft + totalRight)
  //  }

}