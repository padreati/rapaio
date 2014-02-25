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

import rapaio.data.{Feature, Frame}
import rapaio.workspace.Workspace
import rapaio.printer.Summarizable

/**
 * MAE is an error metric used usually on regressions,
 * which computes the mean absolute error (average of the absolute differences
 * between source values and target values).
 *
 * User: Aurelian Tutuianu <padreati@yahoo.com>
 */
class MAE extends Summarizable {

  private var _value: Double = _

  private def compute(_source: Array[Feature], _target: Array[Feature]): MAE = {
    require(_source.length == _target.length, "MAE (mean absolute error) requires " +
      "to have same number of features on source and target")


    var total: Double = 0
    var count: Double = 0
    for (i <- 0 until _source.length; j <- 0 until _source(i).rowCount) {
      count += 1
      total += math.abs(_source(i).values(j) - _target(i).values(j))
    }
    _value = total / count
    this
  }

  def value: Double = _value

  override def summary(): Unit = {
    Workspace.code("MAE: %.6f".format(_value))
  }
}

object MAE {

  def apply(dfSource: Frame, dfTarget: Frame): MAE = {

    def features(df: Frame): Array[Feature] = {
      val names = df.colNames.filter(colName => df.col(colName).isNumeric)
      val result = new Array[Feature](names.length)
      for (i <- 0 until names.length) result(i) = df.col(names(i))
      result
    }

    new MAE().compute(features(dfSource), features(dfTarget))
  }

  def apply(source: Feature, target: Feature): MAE = {
    new MAE().compute(Array[Feature](source), Array[Feature](target))
  }
}