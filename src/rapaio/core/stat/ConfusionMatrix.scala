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

import rapaio.data.Feature
import rapaio.printer.Summarizable

/**
 * User: <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a>
 */
class ConfusionMatrix(private val _actual: Feature,
                      private val _predict: Feature,
                      private val _percents: Boolean = false) extends Summarizable {

  require(_actual.isNominal, "actual values vector must be nominal")
  require(_predict.isNominal, "predict values vector must be nominal")
  require(_actual.labels.dictionary.length == _predict.labels.dictionary.length, "actual and predict does not have the same nominal getDictionary")
  for (i <- 0 until _actual.labels.dictionary.length) {
    require(_actual.labels.dictionary(i) == _predict.labels.dictionary(i), "actual and predict does not have the same nominal getDictionary")
  }

  private val _dict: Array[String] = _actual.labels.dictionary
  private val _cmf: Array[Array[Int]] = Array.fill(_dict.length - 1, _dict.length - 1) {
    0
  }
  private var _acc: Double = 0.0
  private var _completeCases: Double = 0.0

  compute()

  private def compute() {
    for (i <- 0 until _actual.rowCount) {
      if (_actual.indexes(i) != 0 && _predict.indexes(i) != 0) {
        _completeCases += 1
        _cmf(_actual.indexes(i) - 1)(_predict.indexes(i) - 1) += 1
      }
    }
    for (i <- 0 until _actual.rowCount) {
      if (_actual.indexes(i) == _predict.indexes(i) && _actual.indexes(i) != 0) {
        _acc += 1
      }
    }
    if (_completeCases == 0) {
      _acc = 0
    }
    else {
      _acc = _acc / _completeCases
    }
  }

  def buildSummary(sb: StringBuilder): Unit = {
    addConfusionMatrix(sb)
    addDetails(sb)
  }

  private def addDetails(sb: StringBuilder) {
    sb.append("\nComplete cases %d from %d\n".format(Math.rint(_completeCases).toInt, _actual.rowCount))
    sb.append("Accuracy: %.4f\n".format(_acc))
  }

  private def addConfusionMatrix(sb: StringBuilder) {
    sb.append("Confusion rapaio.data.matrix\n")

    sb.append("\n")
    var maxwidth: Int = "Actual".length
    for (i <- 1 until _dict.length) {
      maxwidth = math.max(maxwidth, _dict(i).length)
      var total: Int = 0
      for (j <- 1 until _dict.length) {
        maxwidth = math.max(maxwidth, "%d".format(_cmf(i - 1)(j - 1)).length)
        total += _cmf(i - 1)(j - 1)
      }
      maxwidth = math.max(maxwidth, "%d".format(total).length)
    }

    sb.append(String.format("%" + maxwidth + "s", "")).append("|").append(" Predicted\n")
    sb.append(String.format("%" + maxwidth + "s", "Actual")).append("|")
    for (i <- 1 until _dict.length) {
      sb.append(String.format("%" + maxwidth + "s", _dict(i)))
      if (i != _dict.length - 1) {
        sb.append(" ")
      } else {
        sb.append("|")
      }
    }

    sb.append(String.format("%" + maxwidth + "s ", "Total"))
    sb.append("\n")
    for (i <- 1 until _dict.length + 1) {
      for (j <- 0 until maxwidth) {
        sb.append("-")
      }
      sb.append(" ")
    }
    for (j <- 0 until maxwidth) {
      sb.append("-")
    }
    sb.append(" ")
    sb.append("\n")

    for (i <- 1 until _dict.length) {
      sb.append(("%" + maxwidth + "s").format(_dict(i))).append("|")
      var total: Int = 0
      for (j <- 1 until _dict.length) {
        sb.append(("%" + maxwidth + "d").format(_cmf(i - 1)(j - 1)))
        if (j != _dict.length - 1) {
          sb.append(" ")
        }
        else {
          sb.append("|")
        }
        total += _cmf(i - 1)(j - 1)
      }
      sb.append(("%" + maxwidth + "d").format(total))
      sb.append("\n")
    }

    for (i <- 1 until _dict.length + 1) {
      for (j <- 0 until maxwidth) {
        sb.append("-")
      }
      sb.append(" ")
    }
    for (j <- 0 until maxwidth) {
      sb.append("-")
    }
    sb.append(" ")
    sb.append("\n")


    sb.append(("%" + maxwidth + "s").format("Total")).append("|")
    for (j <- 1 until _dict.length) {
      var total: Int = 0
      for (i <- 1 until _dict.length) {
        total += _cmf(i - 1)(j - 1)
      }

      sb.append(("%" + maxwidth + "d").format(total))
      if (j != _dict.length - 1) {
        sb.append(" ")
      } else {
        sb.append("|")
      }
    }
    sb.append(("%" + maxwidth + "d").format(math.rint(completeCases).asInstanceOf[Int]))
    sb.append("\n")

    // percents

    if (!_percents || completeCases == 0.0) return

    sb.append("\n")
    maxwidth = "Actual".length
    for (i <- 1 until _dict.length) {
      maxwidth = math.max(maxwidth, _dict(i).length)
      var total: Int = 0
      for (j <- 1 until _dict.length) {
        maxwidth = math.max(maxwidth, "%.3f".format(_cmf(i - 1)(j - 1) / _completeCases).length)
        total += _cmf(i - 1)(j - 1)
      }
      maxwidth = math.max(maxwidth, "%.3f".format(total / _completeCases).length)
    }

    sb.append(String.format("%" + maxwidth + "s", "")).append("|").append(" Predicted\n")
    sb.append(String.format("%" + maxwidth + "s", "Actual")).append("|")
    for (i <- 1 until _dict.length) {
      sb.append(String.format("%" + maxwidth + "s", _dict(i)))
      if (i != _dict.length - 1) {
        sb.append(" ")
      } else {
        sb.append("|")
      }
    }
    sb.append(String.format("%" + maxwidth + "s ", "Total"))
    sb.append("\n")

    for (i <- 1 until _dict.length + 1) {
      for (j <- 0 until maxwidth) {
        sb.append("-")
      }
      sb.append(" ")
    }
    for (j <- 0 until maxwidth) {
      sb.append("-")
    }
    sb.append(" ")
    sb.append("\n")

    for (i <- 1 until _dict.length) {
      sb.append(("%" + maxwidth + "s").format(_dict(i))).append("|")
      var total: Int = 0
      for (j <- 1 until _dict.length) {
        sb.append(" %.3f".format(_cmf(i - 1)(j - 1) / _completeCases))
        if (j != _dict.length - 1) {
          sb.append(" ")
        } else {
          sb.append("|")
        }
        total += _cmf(i - 1)(j - 1)
      }
      sb.append(" %.3f".format(total / _completeCases))
      sb.append("\n")
    }

    for (i <- 1 until _dict.length + 1) {
      for (j <- 0 until maxwidth) {
        sb.append("-")
      }
      sb.append(" ")
    }
    for (j <- 0 until maxwidth) {
      sb.append("-")
    }
    sb.append(" ")
    sb.append("\n")


    sb.append(("%" + maxwidth + "s").format("Total")).append("|")
    for (j <- 1 until _dict.length) {
      var total: Int = 0
      for (i <- 1 until _dict.length) {
        total += _cmf(i - 1)(j - 1)
      }
      sb.append(" %.3f".format(total / _completeCases))
      if (j != _dict.length - 1) {
        sb.append(" ")
      } else {
        sb.append("|")
      }
    }

    sb.append(" %.3f".format(1.0))
    sb.append("\n")
  }

  def accuracy: Double = _acc

  def completeCases: Int = Math.rint(_completeCases).toInt

  def matrix: Array[Array[Int]] = _cmf
}