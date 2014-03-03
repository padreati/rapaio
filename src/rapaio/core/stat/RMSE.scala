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
import rapaio.printer.Printable

/**
 * User: Aurelian Tutuianu <paderati@yahoo.com>
 */
class RMSE(_source: Array[Feature], _target: Array[Feature]) extends Printable {

  private val value: Double = {
    var total: Double = 0.0
    var count: Double = 0.0
    for (i <- 0 until _source.size; j <- 0 until _source(i).rowCount) {
      count += 1
      total += math.pow(_source(i).values(j) - _target(i).values(j), 2)
    }
    math.sqrt(total / count)
  }

  def getValue: Double = value

  override def buildSummary(sb: StringBuilder): Unit = ???
}

object RMSE {
  def apply(dfSource: Frame, dfTarget: Frame): RMSE = {

    def features(df: Frame): Array[Feature] = {
      val names = df.colNames.filter(colName => df.col(colName).isNumeric)
      val result = new Array[Feature](names.length)
      for (i <- 0 until names.length) result(i) = df.col(names(i))
      result
    }

    new RMSE(features(dfSource), features(dfTarget))
  }

  def apply(source: Feature, target: Feature): RMSE = {
    new RMSE(Array[Feature](source), Array[Feature](target))
  }

}