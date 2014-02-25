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

import rapaio.printer.Summarizable
import rapaio.data.Feature

/**
 * User: <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a>
 */
class Mode extends Summarizable {

  private var _modes: Array[String] = _

  private def compute(feature: Feature, useMissing: Boolean): Mode = {
    require(!feature.isNominal, "Can't compute mode for other than nominal vectors")

    val freq: Array[Int] = new Array[Int](feature.labels.dictionary.length)
    for (i <- 0 until feature.rowCount) freq(feature.indexes(i)) += 1

    var max: Int = 0
    val start: Int = if (useMissing) 0 else 1
    for (i <- start until freq.length) {
      max = math.max(max, freq(i))
    }

    var count: Int = 0
    for (i <- start until freq.length) {
      if (freq(i) == max) {
        count += 1
      }
    }
    var pos: Int = 0
    _modes = new Array[String](count)
    for (i <- start until freq.length) {
      if (freq(i) == max) {
        _modes(pos) = feature.labels.dictionary(i)
        pos += 1
      }
    }

    this
  }

  def getModes: Array[String] = _modes

  override def buildSummary(sb: StringBuilder): Unit = {
    sb.append("mode\n[" + (_modes mkString ",") + "]\n")
  }
}

object Mode {

  def apply(feature: Feature, useMissing: Boolean = false): Mode = new Mode().compute(feature, useMissing)
}